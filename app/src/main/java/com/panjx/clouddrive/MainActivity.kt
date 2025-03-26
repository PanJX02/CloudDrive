package com.panjx.clouddrive

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.panjx.clouddrive.core.config.ServerManager
import com.panjx.clouddrive.core.design.theme.MyAppTheme
import com.panjx.clouddrive.core.network.datasource.MyRetrofitDatasource
import com.panjx.clouddrive.core.network.di.NetworkModule
import com.panjx.clouddrive.data.UserPreferences
import com.panjx.clouddrive.ui.MyApp
import com.panjx.clouddrive.ui.components.ServerSelectionDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), ServerManager.Companion.ServerConnectionErrorListener {
    private lateinit var userPreferences: UserPreferences
    private lateinit var tokenManager: TokenManager
    
    // 服务器连接错误状态
    private var showServerSelectionDialog by mutableStateOf(false)
    private var errorServer by mutableStateOf("")
    private var errorType by mutableStateOf("")
    
    // 前台token检查任务
    private var tokenCheckJob: Job? = null
    
    // token刷新相关常量
    companion object {
        private const val TAG = "MainActivity"
        // 前台检查间隔（30秒，单位毫秒）
        private const val FIXED_CHECK_INTERVAL_MS =  30 * 1000L
        // 默认刷新阈值（24小时，单位毫秒）
        private const val DEFAULT_REFRESH_THRESHOLD_MS =24 * 60 * 60 * 1000L
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化用户偏好
        userPreferences = UserPreferences(applicationContext)
        
        // 初始化服务器管理器
        ServerManager.initialize(userPreferences, applicationContext)
        
        // 初始化用于刷新token的数据源（使用基础客户端，避免循环依赖）
        val baseClient = NetworkModule.providesBaseOkHttpClient(userPreferences)
        val baseDataSource = MyRetrofitDatasource(userPreferences, baseClient)
        
        // 初始化token管理器
        tokenManager = TokenManager(userPreferences, baseDataSource)
        
        // 设置服务器连接错误监听器
        ServerManager.setServerConnectionErrorListener(this)
        
        // 检查并初始化token刷新
        initTokenRefresh()
        
        // 设置沉浸式状态栏
        enableEdgeToEdge()

        // 关键：允许内容延伸到系统栏下方
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val navController = rememberNavController()
            MyAppTheme {
                // 显示主应用界面
                MyApp(
                    navController = navController,
                )
                
                // 如果需要显示服务器选择弹窗
                if (showServerSelectionDialog) {
                    ServerSelectionDialog(
                        userPreferences = userPreferences,
                        currentServer = errorServer,
                        errorType = errorType,
                        onDismiss = { showServerSelectionDialog = false }
                    )
                }
            }
        }
    }
    
    // 初始化token刷新机制
    private fun initTokenRefresh() {
        CoroutineScope(Dispatchers.IO).launch {
            // 检查用户是否已登录
            val isLoggedIn = userPreferences.isLoggedIn.first()
            if (isLoggedIn) {
                // 先检查token是否已过期
                if (tokenManager.isAccessTokenExpired()) {
                    Log.d(TAG, "token已过期，立即刷新")
                    // token已过期，强制刷新
                    tokenManager.forceRefreshToken(
                        onSuccess = {
                            Log.d(TAG, "token刷新成功")
                        },
                        onError = { error ->
                            // 刷新失败，可能需要重新登录
                            Log.e(TAG, "token刷新失败: $error")
                            if (error.contains("无法解析") || error.contains("刷新失败")) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    userPreferences.clearLoginState()
                                }
                            }
                        }
                    )
                } else {
                    Log.d(TAG, "token状态正常")
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // 应用恢复前台时，重新检查token状态
        initTokenRefresh()
        // 启动前台token检查任务
        startForegroundTokenCheck()
    }
    
    override fun onPause() {
        super.onPause()
        // 停止前台token检查任务
        stopForegroundTokenCheck()
    }
    
    /**
     * 启动前台token检查，使用固定间隔
     */
    private fun startForegroundTokenCheck() {
        // 取消可能存在的旧任务
        tokenCheckJob?.cancel()
        
        Log.d(TAG, "启动token检查，间隔${FIXED_CHECK_INTERVAL_MS/1000}秒")
        
        // 创建新的检查任务
        tokenCheckJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    // 检查用户是否已登录
                    val isLoggedIn = userPreferences.isLoggedIn.first()
                    if (isLoggedIn) {
                        // 获取当前token
                        val accessToken = userPreferences.token.first()
                        if (accessToken.isEmpty()) {
                            Log.d(TAG, "token为空，等待${FIXED_CHECK_INTERVAL_MS/1000}秒后重试")
                            delay(FIXED_CHECK_INTERVAL_MS)
                            continue
                        }
                        
                        // 检查token是否已经过期
                        if (tokenManager.isAccessTokenExpired()) {
                            Log.d(TAG, "token已过期，开始刷新")
                            tokenManager.forceRefreshToken(
                                onSuccess = { Log.d(TAG, "token刷新成功") },
                                onError = { error -> Log.e(TAG, "token刷新失败: $error") }
                            )
                        } 
                        // 检查token是否即将过期（使用阈值）
                        else if (tokenManager.isAccessTokenExpiringSoon(DEFAULT_REFRESH_THRESHOLD_MS)) {
                            tokenManager.forceRefreshToken(
                                onSuccess = { Log.d(TAG, "token刷新成功") },
                                onError = { error -> Log.e(TAG, "token刷新失败: $error") }
                            )
                        }
                    } else {
                        Log.d(TAG, "用户未登录，跳过检查")
                    }
                    
                    delay(FIXED_CHECK_INTERVAL_MS)
                    
                } catch (e: Exception) {
                    Log.e(TAG, "token检查异常", e)
                    delay(FIXED_CHECK_INTERVAL_MS)
                }
            }
        }
    }
    
    /**
     * 停止前台token检查
     */
    private fun stopForegroundTokenCheck() {
        Log.d(TAG, "停止前台token检查任务")
        tokenCheckJob?.cancel()
        tokenCheckJob = null
    }
    
    // 实现ServerConnectionErrorListener接口
    override fun onServerConnectionError(currentServer: String, errorType: String) {
        runOnUiThread {
            this.errorServer = currentServer
            this.errorType = errorType
            this.showServerSelectionDialog = true
        }
    }
}

