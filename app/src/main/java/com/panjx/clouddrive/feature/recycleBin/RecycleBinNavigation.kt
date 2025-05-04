package com.panjx.clouddrive.feature.recycleBin

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

// 回收站路由常量
const val RECYCLE_BIN_ROUTE = "recycle_bin"

// 导航到回收站
fun NavController.navigateToRecycleBin() {
    navigate(RECYCLE_BIN_ROUTE)
}

// 注册回收站路由
fun NavGraphBuilder.recycleBinScreen(
    onNavigateBack: () -> Unit
) {
    composable(route = RECYCLE_BIN_ROUTE) {
        RecycleBinRoute(
            onNavigateBack = onNavigateBack
        )
    }
} 