package com.panjx.clouddrive.core.modle.response

import android.util.Base64
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.charset.StandardCharsets

@Serializable
data class TokenInfo(
    val sub: String,
    val username: String,
    val nickname: String? = null,
    val type: String,
    val iat: Long,
    val exp: Long
) {
    companion object {
        private const val TAG = "TokenInfo"
        
        // 创建Json格式化器的单例
        private val json = Json { ignoreUnknownKeys = true }
        
        fun parseJwt(token: String): TokenInfo? {
            return try {
                val parts = token.split(".")
                if (parts.size != 3) return null
                
                val payload = parts[1].replace('-', '+').replace('_', '/')
                val padding = when (payload.length % 4) {
                    0 -> ""
                    1 -> "==="
                    2 -> "=="
                    3 -> "="
                    else -> ""
                }
                
                val decodedBytes = Base64.decode(payload + padding, Base64.DEFAULT)
                val decodedString = String(decodedBytes, StandardCharsets.UTF_8)
                
                json.decodeFromString<TokenInfo>(decodedString)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    
    // 获取过期时间戳（毫秒）
    fun getExpirationTimeMillis(): Long {
        return exp * 1000 // 转换为毫秒
    }
    
    // 检查token是否即将过期（小于指定的阈值）
    fun isExpiringSoon(thresholdMillis: Long = 5 * 60 * 1000): Boolean { // 默认5分钟
        val currentTimeMillis = System.currentTimeMillis()
        val expirationTimeMillis = getExpirationTimeMillis()
        val timeUntilExpiry = expirationTimeMillis - currentTimeMillis
        
        return timeUntilExpiry < thresholdMillis && timeUntilExpiry > 0
    }
    
    // 检查token是否已过期
    fun isExpired(): Boolean {
        val currentTimeMillis = System.currentTimeMillis()
        val expirationTimeMillis = getExpirationTimeMillis()
        return currentTimeMillis >= expirationTimeMillis
    }
} 