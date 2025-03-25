package com.panjx.clouddrive.core.network.di

import android.util.Log
import com.panjx.clouddrive.core.config.Config
import com.panjx.clouddrive.core.config.ServerManager
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * 服务器切换拦截器，处理服务器连接失败的情况
 * 当服务器连接失败时，会尝试切换到下一个可用的服务器
 */
class ServerSwitchInterceptor : Interceptor {
    companion object {
        private const val TAG = "ServerSwitchInterceptor"
        
        // 需要自动切换服务器的HTTP错误码
        private val SERVER_ERROR_CODES = setOf(
            404, // 资源不存在
            408, // 请求超时
            500, // 服务器内部错误
            502, // 网关错误
            503, // 服务不可用
            504  // 网关超时
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val currentEndpoint = Config.getEndpoint()
        
        try {
            // 尝试执行请求
            val response = chain.proceed(originalRequest)
            
            // 检查HTTP状态码
            if (response.code in SERVER_ERROR_CODES) {
                Log.e(TAG, "服务器返回错误状态码: ${response.code}")
                
                // 处理HTTP错误码
                try {
                    val handled = ServerManager.getInstance().handleHttpError(currentEndpoint, response.code)
                    if (handled) {
                        // 如果已经处理了服务器切换，我们仍然需要返回原始响应
                        // 应用可能会在稍后重启
                        Log.d(TAG, "服务器错误已处理，将自动切换服务器")
                    }
                } catch (ex: Exception) {
                    Log.e(TAG, "处理HTTP错误时出错: ${ex.message}")
                }
            }
            
            return response
            
        } catch (e: IOException) {
            Log.e(TAG, "网络请求失败: ${e.message}", e)
            
            // 处理连接异常
            val handled = try {
                ServerManager.getInstance().handleServerFailure(currentEndpoint, e)
            } catch (ex: Exception) {
                Log.e(TAG, "处理服务器切换时出错: ${ex.message}")
                false
            }
            
            if (handled) {
                // 如果已经处理了服务器切换，抛出原始异常供上层处理
                // 应用将会重启，所以这里不需要再次尝试请求
                throw e
            } else {
                // 如果未处理服务器切换（例如，不是连接错误），直接抛出原始异常
                throw e
            }
        }
    }
} 