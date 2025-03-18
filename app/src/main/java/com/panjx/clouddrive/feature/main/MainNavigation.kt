package com.panjx.clouddrive.feature.main

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.panjx.clouddrive.data.UserPreferences

const val MAIN_ROUTE = "main"

/**
 * 跳转到这个界面
 */

fun NavController.navigateToMain(): Unit {
    navigate(MAIN_ROUTE){
        // 跳转页面是否在栈内, 如果栈内存在则不添加到栈内
        launchSingleTop = true

        // 清空栈(栈内所有页面)
        popUpTo(0) { // 使用0表示清除整个导航栈
            inclusive = false // 不包含当前页面
        }
    }
}

/**
 * 主页路由
 */
fun NavGraphBuilder.mainScreen(
    finishPage: () -> Unit,
    userPreferences: UserPreferences,
    onNavigateToLogin: () -> Unit
) {
    composable(MAIN_ROUTE) {
        MainRote(
            finishPage = finishPage,
            userPreferences = userPreferences,
            onNavigateToLogin = onNavigateToLogin
        )
    }
}