package com.panjx.clouddrive

import android.util.Log
import com.panjx.clouddrive.core.modle.response.TokenInfo
import com.panjx.clouddrive.core.network.datasource.MyRetrofitDatasource
import com.panjx.clouddrive.data.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TokenManager(
    private val userPreferences: UserPreferences,
    private val dataSource: MyRetrofitDatasource
) {
    companion object {
        private const val TAG = "TokenManager"
    }
    
    /**
     * 检查accessToken是否已过期或即将过期，如果是则尝试刷新
     * @param thresholdMillis 过期阈值（毫秒），默认为5分钟
     * @param onSuccess 刷新成功回调
     * @param onError 刷新失败回调
     */
    fun checkAndRefreshTokenIfNeeded(
        thresholdMillis: Long = 5 * 60 * 1000, // 默认5分钟
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val accessToken = userPreferences.token.first()
                if (accessToken.isEmpty()) {
                    Log.d(TAG, "accessToken为空，无需刷新")
                    onError("未登录状态")
                    return@launch
                }
                
                // 解析token获取过期时间
                val tokenInfo = TokenInfo.parseJwt(accessToken)
                if (tokenInfo == null) {
                    Log.e(TAG, "无法解析token")
                    onError("无法解析token")
                    return@launch
                }
                
                // 检查token是否已过期或即将过期
                if (tokenInfo.isExpired()) {
                    Log.d(TAG, "token已过期，立即刷新")
                    refreshToken(onSuccess, onError)
                } else if (tokenInfo.isExpiringSoon(thresholdMillis)) {
                    Log.d(TAG, "token即将过期，尝试刷新")
                    refreshToken(onSuccess, onError)
                } else {
                    Log.d(TAG, "token未过期，无需刷新")
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e(TAG, "检查token过期异常", e)
                onError("检查token异常: ${e.message}")
            }
        }
    }
    
    /**
     * 刷新token
     */
    private suspend fun refreshToken(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val refreshToken = userPreferences.refreshToken.first()
            if (refreshToken.isEmpty()) {
                Log.d(TAG, "refreshToken为空，无法刷新")
                onError("刷新token不存在")
                return
            }
            
            val response = dataSource.refreshToken()
            if (response.code == 1 && response.data != null) {
                val newLoginData = response.data
                
                // 如果refreshToken为null，使用当前refreshToken继续使用
                val newRefreshToken = newLoginData.refreshToken ?: refreshToken
                
                // 保存新的token
                userPreferences.setLoggedIn(
                    isLoggedIn = true,
                    username = userPreferences.username.first(),
                    accessToken = newLoginData.accessToken,
                    refreshToken = newRefreshToken
                )
                
                Log.d(TAG, "token刷新成功" + 
                    if (newLoginData.refreshToken == null) "（服务器返回的refreshToken为null，继续使用原refreshToken）" else "")
                onSuccess()
            } else {
                Log.d(TAG, "token刷新失败: ${response.message}")
                onError(response.message ?: "刷新失败")
            }
        } catch (e: Exception) {
            Log.e(TAG, "刷新token异常", e)
            onError("刷新token异常: ${e.message}")
        }
    }
    
    /**
     * 强制刷新token，无论是否过期
     */
    fun forceRefreshToken(
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "强制刷新token")
                refreshToken(onSuccess, onError)
            } catch (e: Exception) {
                Log.e(TAG, "强制刷新token异常", e)
                onError("刷新token异常: ${e.message}")
            }
        }
    }
    
    /**
     * 获取accessToken的过期时间（毫秒时间戳）
     * @return 过期时间戳，如果token无效则返回null
     */
    suspend fun getAccessTokenExpirationTime(): Long? {
        val accessToken = userPreferences.token.first()
        if (accessToken.isEmpty()) return null
        
        val tokenInfo = TokenInfo.parseJwt(accessToken) ?: return null
        return tokenInfo.getExpirationTimeMillis()
    }
    
    /**
     * 检查accessToken是否已过期
     * @return true表示已过期，false表示未过期
     */
    suspend fun isAccessTokenExpired(): Boolean {
        val accessToken = userPreferences.token.first()
        if (accessToken.isEmpty()) {
            Log.d(TAG, "token为空")
            return true
        }
        
        val tokenInfo = TokenInfo.parseJwt(accessToken) 
        if (tokenInfo == null) {
            Log.d(TAG, "无法解析token")
            return true
        }
        
        val isExpired = tokenInfo.isExpired()
        val expirationTime = tokenInfo.getExpirationTimeMillis()
        val currentTime = System.currentTimeMillis()
        val timeUntilExpiry = expirationTime - currentTime
        
        Log.d(TAG, "token状态: ${if (isExpired) "已过期" else "有效"}，剩余${timeUntilExpiry/1000}秒")
        return isExpired
    }
    
    /**
     * 检查token是否即将过期，使用较短的阈值（10秒）
     * 适用于应用在前台时更频繁地检查
     * @return true表示即将过期，false表示未即将过期
     */
    suspend fun isAccessTokenExpiringSoon(): Boolean {
        return isAccessTokenExpiringSoon(10 * 1000L) // 默认使用10秒阈值
    }
    
    /**
     * 检查token是否即将过期，使用指定的阈值
     * @param thresholdMillis 过期阈值（毫秒）
     * @return true表示即将过期，false表示未即将过期
     */
    suspend fun isAccessTokenExpiringSoon(thresholdMillis: Long): Boolean {
        val accessToken = userPreferences.token.first()
        if (accessToken.isEmpty()) {
            Log.d(TAG, "token为空")
            return true
        }
        
        val tokenInfo = TokenInfo.parseJwt(accessToken)
        if (tokenInfo == null) {
            Log.d(TAG, "无法解析token")
            return true
        }
        
        val isExpiringSoon = tokenInfo.isExpiringSoon(thresholdMillis)
        val expirationTime = tokenInfo.getExpirationTimeMillis()
        val currentTime = System.currentTimeMillis()
        val timeUntilExpiry = expirationTime - currentTime
        
        if (isExpiringSoon) {
            Log.d(TAG, "token即将过期，剩余${timeUntilExpiry/1000}秒")
        }
        return isExpiringSoon
    }
} 