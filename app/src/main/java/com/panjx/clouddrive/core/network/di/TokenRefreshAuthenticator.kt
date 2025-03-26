package com.panjx.clouddrive.core.network.di

import android.util.Log
import com.panjx.clouddrive.core.network.datasource.MyRetrofitDatasource
import com.panjx.clouddrive.data.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.util.concurrent.atomic.AtomicBoolean

class TokenRefreshAuthenticator(
    private val userPreferences: UserPreferences,
    private val dataSource: MyRetrofitDatasource
) : Authenticator {
    
    // 防止多个请求同时刷新token
    private val isRefreshing = AtomicBoolean(false)
    
    // 上次刷新token的时间
    private var lastRefreshTime = 0L
    
    // 刷新冷却时间（毫秒），防止频繁刷新
    private val REFRESH_COOLDOWN_MS = 2000L
    
    override fun authenticate(route: Route?, response: Response): Request? {
        // 检查是否是401（未授权）错误
        if (response.code != 401) return null
        
        // 获取请求路径，用于日志
        val requestPath = response.request.url.encodedPath
        Log.d("TokenRefreshAuthenticator", "收到401响应: $requestPath，尝试刷新token")
        
        // 获取当前token
        val currentToken = runBlocking { userPreferences.token.first() }
        
        // 检查响应头中的Authorization，确保我们没有已经尝试过的token
        val requestToken = response.request.header("Authorization")?.replace("Bearer ", "")
        if (requestToken != null && requestToken != currentToken) {
            // 使用当前存储的token重试请求（可能其他请求已经刷新了token）
            Log.d("TokenRefreshAuthenticator", "使用新token重试请求: $requestPath")
            return response.request.newBuilder()
                .header("Authorization", "Bearer $currentToken")
                .build()
        }
        
        // 防止频繁刷新
        val now = System.currentTimeMillis()
        if (now - lastRefreshTime < REFRESH_COOLDOWN_MS) {
            Log.d("TokenRefreshAuthenticator", "刷新操作太频繁，使用当前token重试: $requestPath")
            return response.request.newBuilder()
                .header("Authorization", "Bearer $currentToken")
                .build()
        }
        
        // 检查refreshToken是否存在
        val refreshToken = runBlocking { userPreferences.refreshToken.first() }
        if (refreshToken.isEmpty()) {
            Log.d("TokenRefreshAuthenticator", "refreshToken为空，无法刷新")
            // 清除登录状态，因为我们无法刷新
            runBlocking { userPreferences.clearLoginState() }
            return null
        }
        
        // 如果另一个请求正在刷新token，等待一段时间后使用当前token重试
        if (isRefreshing.get()) {
            Log.d("TokenRefreshAuthenticator", "另一个请求正在刷新token，稍后重试: $requestPath")
            // 短暂等待，让正在进行的刷新完成
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                // 忽略中断
            }
            
            // 获取可能已更新的token
            val newToken = runBlocking { userPreferences.token.first() }
            return response.request.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
        }
        
        // 尝试刷新token
        if (isRefreshing.compareAndSet(false, true)) {
            try {
                Log.d("TokenRefreshAuthenticator", "开始刷新token: $requestPath")
                lastRefreshTime = now
                
                // 通过refreshToken获取新的token
                val refreshResponse = runBlocking { dataSource.refreshToken() }
                
                if (refreshResponse.code == 1 && refreshResponse.data != null) {
                    val newLoginData = refreshResponse.data
                    
                    // 如果refreshToken为null，使用当前refreshToken继续使用
                    val newRefreshToken = newLoginData.refreshToken ?: refreshToken
                    
                    // 保存新的token
                    runBlocking { 
                        userPreferences.setLoggedIn(
                            isLoggedIn = true,
                            username = runBlocking { userPreferences.username.first() },
                            accessToken = newLoginData.accessToken,
                            refreshToken = newRefreshToken
                        )
                    }
                    
                    Log.d("TokenRefreshAuthenticator", "token刷新成功，使用新token重试请求: $requestPath")
                    
                    // 使用新的token重试原始请求
                    return response.request.newBuilder()
                        .header("Authorization", "Bearer ${newLoginData.accessToken}")
                        .build()
                } else {
                    Log.d("TokenRefreshAuthenticator", "token刷新失败：${refreshResponse.message}")
                    // 刷新失败，清除登录状态
                    runBlocking { userPreferences.clearLoginState() }
                    return null
                }
            } catch (e: Exception) {
                Log.e("TokenRefreshAuthenticator", "token刷新异常: ${e.message}")
                runBlocking { userPreferences.clearLoginState() }
                return null
            } finally {
                isRefreshing.set(false)
            }
        } else {
            Log.d("TokenRefreshAuthenticator", "其他请求已经在刷新token，跳过刷新")
            return null
        }
    }
} 