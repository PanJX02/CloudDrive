package com.panjx.clouddrive.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.panjx.clouddrive.TokenManager
import com.panjx.clouddrive.core.modle.response.TokenInfo
import com.panjx.clouddrive.core.network.datasource.MyRetrofitDatasource
import com.panjx.clouddrive.core.network.di.NetworkModule
import com.panjx.clouddrive.data.UserPreferences
import kotlinx.coroutines.flow.first

/**
 * 用于定期检查和刷新访问令牌的Worker
 */
class TokenRefreshWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    
    companion object {
        const val TAG = "TokenRefreshWorker"
        
        // token刷新的阈值，改为在过期前30秒刷新
        const val DEFAULT_REFRESH_THRESHOLD_MS = 60 * 1000L
    }
    
    override suspend fun doWork(): Result {
        Log.d(TAG, "开始执行后台token检查任务")
        
        try {
            val userPreferences = UserPreferences(applicationContext)
            
            // 检查用户是否已登录
            val isLoggedIn = userPreferences.isLoggedIn.first()
            if (!isLoggedIn) {
                Log.d(TAG, "用户未登录，无需刷新token")
                return Result.success()
            }
            
            // 获取当前token
            val accessToken = userPreferences.token.first()
            if (accessToken.isEmpty()) {
                Log.d(TAG, "accessToken为空，无需刷新")
                return Result.success()
            }
            
            // 初始化TokenManager
            val baseClient = NetworkModule.providesBaseOkHttpClient(userPreferences)
            val baseDataSource = MyRetrofitDatasource(userPreferences, baseClient)
            val tokenManager = TokenManager(userPreferences, baseDataSource)
            
            // 获取token详细信息用于日志
            val tokenInfo = TokenInfo.parseJwt(accessToken)
            if (tokenInfo != null) {
                val expirationTime = tokenInfo.getExpirationTimeMillis()
                val currentTime = System.currentTimeMillis()
                val timeUntilExpiry = expirationTime - currentTime
                Log.d(TAG, "当前token剩余有效时间: ${timeUntilExpiry/1000}秒，将在${expirationTime}过期")
            }
            
            // 先检查token是否已过期
            Log.d(TAG, "检查token是否已过期")
            if (tokenManager.isAccessTokenExpired()) {
                Log.d(TAG, "token已过期，尝试强制刷新")
                // 强制刷新token
                var refreshResult = false
                tokenManager.forceRefreshToken(
                    onSuccess = {
                        Log.d(TAG, "过期的token刷新成功")
                        refreshResult = true
                    },
                    onError = { error ->
                        Log.e(TAG, "刷新过期token失败: $error")
                        refreshResult = false
                    }
                )
                
                // 等待刷新结果
                Log.d(TAG, "等待2秒获取刷新结果")
                Thread.sleep(2000) // 给异步操作一点时间完成
                
                return if (refreshResult) {
                    Log.d(TAG, "token刷新成功，任务完成")
                    Result.success()
                } else {
                    // 如果刷新失败，我们希望稍后重试
                    Log.d(TAG, "token刷新失败，安排重试")
                    Result.retry()
                }
            }
            
            // 检查并刷新token
            Log.d(TAG, "检查token是否即将过期（${DEFAULT_REFRESH_THRESHOLD_MS/1000}秒内）")
            var refreshResult = false
            tokenManager.checkAndRefreshTokenIfNeeded(
                thresholdMillis = DEFAULT_REFRESH_THRESHOLD_MS,
                onSuccess = {
                    Log.d(TAG, "Token检查成功，token有效或已成功刷新")
                    refreshResult = true
                },
                onError = { error ->
                    Log.e(TAG, "Token刷新失败: $error")
                    refreshResult = false
                }
            )
            
            // 等待刷新结果
            Log.d(TAG, "等待2秒获取刷新结果")
            Thread.sleep(2000) // 给异步操作一点时间完成
            
            return if (refreshResult) {
                Log.d(TAG, "Token检查/刷新任务完成")
                Result.success()
            } else {
                // 如果刷新失败，我们希望稍后重试
                Log.d(TAG, "Token检查/刷新失败，安排重试")
                Result.retry()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Token刷新工作出错", e)
            return Result.failure()
        }
    }
} 