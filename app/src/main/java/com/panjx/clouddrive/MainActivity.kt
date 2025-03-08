package com.panjx.clouddrive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.panjx.clouddrive.ui.MyApp
import com.panjx.clouddrive.core.design.theme.MyAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

