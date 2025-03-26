package com.panjx.clouddrive.core.config

import android.content.Context
import android.util.Log
import com.panjx.clouddrive.data.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
        
        // 正在处理服务器问题的标志
        private val isHandlingServerIssue = AtomicBoolean(false)
        
        // 计数器，记录连续的HTTP错误次数，防止单个404就触发弹窗
        private val httpErrorCounter = ConcurrentHashMap<String, Int>()
        
        // HTTP错误阈值，连续出现多少次错误才触发服务器选择弹窗
        private const val HTTP_ERROR_THRESHOLD = 3
        
        // 服务器连接错误监听器接口
        interface ServerConnectionErrorListener {
            fun onServerConnectionError(currentServer: String, errorType: String)
        }
        
        // 服务器连接错误监听器
        private var serverConnectionErrorListener: ServerConnectionErrorListener? = null
        
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
        
        // 重置状态和失败记录
        fun reset() {
            isHandlingServerIssue.set(false)
            failedServers.clear()
            httpErrorCounter.clear()
        }
        
        // 设置服务器连接错误监听器
        fun setServerConnectionErrorListener(listener: ServerConnectionErrorListener) {
            serverConnectionErrorListener = listener
        }
    }
    
    // 处理HTTP错误，如404、500等
    fun handleHttpError(currentServer: String, errorCode: Int): Boolean {
        // 增加HTTP错误计数
        val errorCount = httpErrorCounter.compute(currentServer) { _, count ->
            (count ?: 0) + 1
        } ?: 1
        
        Log.d(TAG, "服务器 $currentServer 出现HTTP错误 $errorCode，累计错误次数: $errorCount")
        
        // 如果错误次数未达到阈值，不触发处理
        if (errorCount < HTTP_ERROR_THRESHOLD) {
            return false
        }
        
        // 如果已经在处理服务器问题，则不重复触发
        if (isHandlingServerIssue.getAndSet(true)) {
            return true
        }
        
        // 记录当前服务器为失败状态
        failedServers[currentServer] = true
        
        // 通知监听器处理服务器连接错误
        notifyServerConnectionError(currentServer, "HTTP错误($errorCode)")
        
        return true
    }
    
    // 标记服务器连接失败并通知监听器
    fun handleServerFailure(currentServer: String, error: Throwable): Boolean {
        // 如果错误不是由网络连接问题引起的，则不处理
        if (error !is ConnectException && 
            error !is SocketTimeoutException && 
            error !is UnknownHostException) {
            return false
        }
        
        // 如果已经在处理服务器问题，则不重复触发
        if (isHandlingServerIssue.getAndSet(true)) {
            return true
        }
        
        // 记录当前服务器为失败状态
        failedServers[currentServer] = true
        
        // 通知监听器处理服务器连接错误
        notifyServerConnectionError(currentServer, error.javaClass.simpleName)
        
        return true
    }
    
    // 通知监听器处理服务器连接错误
    private fun notifyServerConnectionError(currentServer: String, errorType: String) {
        // 当前服务器名称
        val currentEndpoint = runBlocking { userPreferences.endpoint.first() }
        val serverName = UserPreferences.getEndpointName(currentEndpoint)
        
        Log.d(TAG, "服务器 $serverName ($currentServer) 连接错误: $errorType")
        
        // 通知监听器
        serverConnectionErrorListener?.onServerConnectionError(currentServer, errorType)
        
        // 重置处理标志
        isHandlingServerIssue.set(false)
    }
} 