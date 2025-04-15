package com.panjx.clouddrive

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.panjx.clouddrive.core.config.ServerManager
import com.panjx.clouddrive.core.design.theme.MyAppTheme
import com.panjx.clouddrive.core.network.datasource.MyRetrofitDatasource
import com.panjx.clouddrive.core.network.di.NetworkModule
import com.panjx.clouddrive.data.UserPreferences
import com.panjx.clouddrive.ui.MyApp
import com.panjx.clouddrive.ui.components.ServerSelectionDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@AndroidEntryPoint
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
        // 存储权限请求码
        private const val STORAGE_PERMISSION_CODE = 1001
        // 所有文件访问权限请求码
        private const val MANAGE_STORAGE_PERMISSION_CODE = 1002
    }
    
    // 注册权限请求回调
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "存储权限已获取")
            Toast.makeText(this, "存储权限已获取，可以下载到公共目录", Toast.LENGTH_SHORT).show()
        } else {
            Log.d(TAG, "存储权限被拒绝")
            Toast.makeText(this, "未获得存储权限，将使用应用私有目录", Toast.LENGTH_SHORT).show()
        }
    }
    
    // 注册所有文件管理权限回调
    private val requestManageExternalStorageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                Log.d(TAG, "已获取所有文件访问权限")
                Toast.makeText(this, "已获取所有文件访问权限", Toast.LENGTH_SHORT).show()
            } else {
                Log.d(TAG, "未获取所有文件访问权限")
                Toast.makeText(this, "未获取所有文件访问权限，将使用应用私有目录", Toast.LENGTH_SHORT).show()
            }
        }
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
        
        // 请求存储权限
        requestStoragePermissions()
        
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
    
    /**
     * 请求存储权限
     */
    private fun requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11及以上，请求所有文件访问权限
            if (!Environment.isExternalStorageManager()) {
                try {
                    Log.d(TAG, "请求Android 11+所有文件访问权限")
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse("package:$packageName")
                    requestManageExternalStorageLauncher.launch(intent)
                } catch (e: Exception) {
                    Log.e(TAG, "请求所有文件访问权限失败", e)
                    // 尝试使用旧版权限方式
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    requestManageExternalStorageLauncher.launch(intent)
                }
            } else {
                Log.d(TAG, "已有所有文件访问权限")
            }
        } else {
            // Android 10及以下，请求传统存储权限
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "请求传统存储权限")
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else {
                Log.d(TAG, "已有传统存储权限")
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

