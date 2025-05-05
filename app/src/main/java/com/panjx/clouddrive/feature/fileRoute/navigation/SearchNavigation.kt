package com.panjx.clouddrive.feature.fileRoute.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.panjx.clouddrive.feature.fileRoute.screen.SearchFileScreen

// 搜索文件路由常量
const val SEARCH_FILE_ROUTE = "search_file"

/**
 * 导航到文件搜索页面
 */
fun NavController.navigateToSearchFile() {
    navigate(SEARCH_FILE_ROUTE)
}

/**
 * 注册文件搜索页面路由
 */
fun NavGraphBuilder.searchFileScreen(
    onBackClick: () -> Unit
) {
    composable(
        route = SEARCH_FILE_ROUTE
    ) {
        SearchFileScreen(
            onBackClick = onBackClick
        )
    }
} 