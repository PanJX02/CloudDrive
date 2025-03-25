package com.panjx.clouddrive

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.panjx.clouddrive.core.config.ServerManager
import com.panjx.clouddrive.core.design.theme.MyAppTheme
import com.panjx.clouddrive.data.UserPreferences
import com.panjx.clouddrive.ui.MyApp

class MainActivity : ComponentActivity() {
    private lateinit var userPreferences: UserPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化用户偏好
        userPreferences = UserPreferences(applicationContext)
        
        // 初始化服务器管理器
        ServerManager.initialize(userPreferences, applicationContext)
        
        // 检查是否是由自动服务器切换触发的启动
        val isAutoServerSwitch = intent.getBooleanExtra("AUTO_SERVER_SWITCH", false)
        if (isAutoServerSwitch) {
            val serverName = intent.getStringExtra("SERVER_NAME") ?: "备用服务器"
            val errorType = intent.getStringExtra("ERROR_TYPE") ?: "连接问题"
            
            // 显示切换服务器的通知，包含错误类型
            Toast.makeText(
                this, 
                "检测到服务器问题($errorType)，已自动切换到$serverName", 
                Toast.LENGTH_LONG
            ).show()
            
            // 重置服务器管理器状态
            ServerManager.reset()
        }
        
        // 设置沉浸式状态栏
        enableEdgeToEdge()

        // 关键：允许内容延伸到系统栏下方
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val navController = rememberNavController()
            MyAppTheme {
                MyApp(
                    navController = navController,
                )
            }
        }
    }
}

