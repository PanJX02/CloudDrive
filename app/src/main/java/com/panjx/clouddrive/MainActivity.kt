package com.panjx.clouddrive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.panjx.clouddrive.feature.splash.SplashRoute
import com.panjx.clouddrive.ui.MyApp
import com.panjx.clouddrive.ui.theme.CloudDriveTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 设置沉浸式状态栏
        enableEdgeToEdge()


        setContent {
            val navController = rememberNavController()
            CloudDriveTheme {
                MyApp(
                    navController = navController,
                )
            }
        }
    }
}

