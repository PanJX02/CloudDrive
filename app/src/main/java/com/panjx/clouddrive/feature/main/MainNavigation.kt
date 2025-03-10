package com.panjx.clouddrive.feature.main

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.panjx.clouddrive.data.UserPreferences
import com.panjx.clouddrive.feature.login.LOGIN_ROUTE
import com.panjx.clouddrive.feature.splash.SPLASH_ROUTE
import com.panjx.clouddrive.feature.splash.SplashRoute

const val MAIN_ROUTE = "main"

/**
 * 跳转到这个界面
 */

fun NavController.navigateToMain(): Unit {
    navigate(MAIN_ROUTE){
        // 跳转页面是否在栈内, 如果栈内存在则不添加到栈内
        launchSingleTop = true

        // 清空栈(栈内所有页面)
        popUpTo(SPLASH_ROUTE){
            inclusive = true //是否包含当前页面
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