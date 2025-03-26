package com.panjx.clouddrive.core.network.di

import android.util.Log
import com.panjx.clouddrive.core.config.Config
import com.panjx.clouddrive.core.config.ServerManager
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * 服务器切换拦截器，处理服务器连接失败的情况
 * 当服务器连接失败时，通知ServerManager处理
 */
class ServerSwitchInterceptor : Interceptor {
    companion object {
        private const val TAG = "ServerSwitchInterceptor"
        
        // 需要处理的HTTP错误码
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
                    ServerManager.getInstance().handleHttpError(currentEndpoint, response.code)
                } catch (ex: Exception) {
                    Log.e(TAG, "处理HTTP错误时出错: ${ex.message}")
                }
            }
            
            return response
            
        } catch (e: IOException) {
            Log.e(TAG, "网络请求失败: ${e.message}", e)
            
            // 处理连接异常
            try {
                ServerManager.getInstance().handleServerFailure(currentEndpoint, e)
            } catch (ex: Exception) {
                Log.e(TAG, "处理服务器连接错误时出错: ${ex.message}")
            }
            
            // 无论是否处理了服务器连接错误，都抛出原始异常供上层处理
            throw e
        }
    }
} 