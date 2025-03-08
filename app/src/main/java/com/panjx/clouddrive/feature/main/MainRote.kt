package com.panjx.clouddrive.feature.main


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.panjx.clouddrive.core.design.component.BottomBar
import com.panjx.clouddrive.core.design.theme.Gray20
import com.panjx.clouddrive.core.design.theme.Gray40
import com.panjx.clouddrive.feature.fileRoute.FileRoute
import com.panjx.clouddrive.feature.fileRoute.FileTopBar
import com.panjx.clouddrive.feature.meRoute.MeRoute
import com.panjx.clouddrive.feature.transfersRoute.TransfersRoute


@Composable
fun MainRote(
    finishPage: () -> Unit
) {
    MainScreen(finishPage)
}

@Composable
fun MainScreen(finishPage: () -> Unit = {}) {
    val navController = rememberNavController()

    // 定义底部导航项对应路由
    val items = listOf(
        Screen.File,
        Screen.Transfers,
        Screen.Me
    )

    Scaffold(
        bottomBar = {
            BottomBar(
                navController = navController,
                items = items
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0) // 禁用默认内边距
        // 底部导航项点击事件
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.File.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.File.route) { FileRoute() }
            composable(Screen.Transfers.route) { TransfersRoute() }
            composable(Screen.Me.route) { MeRoute() }
        }
    }
}

// 定义路由密封类
sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object File : Screen(
        route = "file_route",
        label = "文件",
        icon = Icons.Filled.Folder // 替换实际图标
    )

    object Transfers : Screen(
        route = "transfers_route",
        label = "传输",
        icon = Icons.Filled.SwapVert // 替换实际图标
    )

    object Me : Screen(
        route = "me_route",
        label = "我的",
        icon = Icons.Filled.Person // 替换实际图标
    )
}



