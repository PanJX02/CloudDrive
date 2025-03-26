package com.panjx.clouddrive

import android.os.Bundle
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
import com.panjx.clouddrive.data.UserPreferences
import com.panjx.clouddrive.ui.MyApp
import com.panjx.clouddrive.ui.components.ServerSelectionDialog

class MainActivity : ComponentActivity(), ServerManager.Companion.ServerConnectionErrorListener {
    private lateinit var userPreferences: UserPreferences
    
    // 服务器连接错误状态
    private var showServerSelectionDialog by mutableStateOf(false)
    private var errorServer by mutableStateOf("")
    private var errorType by mutableStateOf("")
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化用户偏好
        userPreferences = UserPreferences(applicationContext)
        
        // 初始化服务器管理器
        ServerManager.initialize(userPreferences, applicationContext)
        
        // 设置服务器连接错误监听器
        ServerManager.setServerConnectionErrorListener(this)
        
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
    
    // 实现ServerConnectionErrorListener接口
    override fun onServerConnectionError(currentServer: String, errorType: String) {
        runOnUiThread {
            this.errorServer = currentServer
            this.errorType = errorType
            this.showServerSelectionDialog = true
        }
    }
}

