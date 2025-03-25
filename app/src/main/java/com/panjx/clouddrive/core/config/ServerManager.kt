package com.panjx.clouddrive.core.config

import android.content.Context
import android.content.Intent
import android.util.Log
import com.panjx.clouddrive.MainActivity
import com.panjx.clouddrive.data.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

class ServerManager private constructor(
    private val userPreferences: UserPreferences,
    private val appContext: Context
) {
    companion object {
        private const val TAG = "ServerManager"
        private var INSTANCE: ServerManager? = null
        
        // 记录已经尝试过的服务器，避免无限重试
        private val failedServers = ConcurrentHashMap<String, Boolean>()
        
        // 正在切换服务器的标志，避免多个请求同时触发切换
        private val isSwitching = AtomicBoolean(false)
        
        // 计数器，记录连续的HTTP错误次数，防止单个404就触发切换
        private val httpErrorCounter = ConcurrentHashMap<String, Int>()
        
        // HTTP错误阈值，连续出现多少次错误才触发服务器切换
        private const val HTTP_ERROR_THRESHOLD = 3
        
        fun initialize(userPreferences: UserPreferences, appContext: Context) {
            if (INSTANCE == null) {
                synchronized(this) {
                    if (INSTANCE == null) {
                        INSTANCE = ServerManager(userPreferences, appContext)
                    }
                }
            }
        }
        
        fun getInstance(): ServerManager {
            return INSTANCE ?: throw IllegalStateException("ServerManager 未初始化")
        }
        
        // 重置切换状态和失败记录
        fun reset() {
            isSwitching.set(false)
            failedServers.clear()
            httpErrorCounter.clear()
        }
    }
    
    // 处理HTTP错误，如404、500等
    fun handleHttpError(currentServer: String, errorCode: Int): Boolean {
        // 增加HTTP错误计数
        val errorCount = httpErrorCounter.compute(currentServer) { _, count ->
            (count ?: 0) + 1
        } ?: 1
        
        Log.d(TAG, "服务器 $currentServer 出现HTTP错误 $errorCode，累计错误次数: $errorCount")
        
        // 如果错误次数未达到阈值，不触发切换
        if (errorCount < HTTP_ERROR_THRESHOLD) {
            return false
        }
        
        // 如果已经在切换服务器，则不重复触发
        if (isSwitching.getAndSet(true)) {
            return true
        }
        
        // 记录当前服务器为失败状态
        failedServers[currentServer] = true
        
        // 切换到下一个服务器
        return switchToNextServer(currentServer, "HTTP错误($errorCode)")
    }
    
    // 标记服务器连接失败并尝试切换到下一个可用服务器
    fun handleServerFailure(currentServer: String, error: Throwable): Boolean {
        // 如果错误不是由网络连接问题引起的，则不切换服务器
        if (error !is ConnectException && 
            error !is SocketTimeoutException && 
            error !is UnknownHostException) {
            return false
        }
        
        // 如果已经在切换服务器，则不重复触发
        if (isSwitching.getAndSet(true)) {
            return true
        }
        
        // 记录当前服务器为失败状态
        failedServers[currentServer] = true
        
        // 切换到下一个服务器
        return switchToNextServer(currentServer, error.javaClass.simpleName)
    }
    
    // 切换到下一个可用服务器
    private fun switchToNextServer(currentServer: String, errorType: String): Boolean {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val availableServers = UserPreferences.ENDPOINT_OPTIONS.values.toList()
                val currentEndpoint = runBlocking { userPreferences.endpoint.first() }
                
                // 找到当前服务器的索引
                val currentIndex = availableServers.indexOf(currentEndpoint)
                if (currentIndex == -1) {
                    Log.e(TAG, "当前服务器不在可用服务器列表中")
                    isSwitching.set(false)
                    return@launch
                }
                
                // 尝试切换到下一个未失败的服务器
                for (i in 1 until availableServers.size) {
                    val nextIndex = (currentIndex + i) % availableServers.size
                    val nextServer = availableServers[nextIndex]
                    
                    // 如果这个服务器已经被标记为失败，则跳过
                    if (failedServers[nextServer] == true) {
                        continue
                    }
                    
                    // 切换到新服务器
                    Log.d(TAG, "由于$errorType，切换到新服务器: $nextServer")
                    userPreferences.setEndpoint(nextServer)
                    Config.updateEndpoint(nextServer)
                    
                    // 通知用户并重启应用
                    withContext(Dispatchers.Main) {
                        val intent = Intent(appContext, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        intent.putExtra("AUTO_SERVER_SWITCH", true)
                        intent.putExtra("SERVER_NAME", UserPreferences.getEndpointName(nextServer))
                        intent.putExtra("ERROR_TYPE", errorType)
                        appContext.startActivity(intent)
                    }
                    
                    return@launch
                }
                
                // 如果所有服务器都失败了
                Log.e(TAG, "所有服务器都无法连接")
                // 这里可以添加通知用户所有服务器都无法连接的逻辑
                
            } catch (e: Exception) {
                Log.e(TAG, "切换服务器时出错: ${e.message}")
            } finally {
                isSwitching.set(false)
            }
        }
        
        return true
    }
} 