package com.panjx.clouddrive.feature.profile

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.panjx.clouddrive.feature.profile.components.EditProfileRoute

// 路由常量
const val PROFILE_ROUTE = "profile"
const val EDIT_PROFILE_ROUTE = "edit_profile"

/**
 * 导航到个人中心页面
 */
fun NavController.navigateToProfile() {
    navigate(PROFILE_ROUTE)
}

/**
 * 导航到编辑个人资料页面
 */
fun NavController.navigateToEditProfile() {
    navigate(EDIT_PROFILE_ROUTE)
}

/**
 * 注册个人中心页面路由
 */
fun NavGraphBuilder.profileScreen(
    onBackClick: () -> Unit,
    onNavigateToEditProfile: () -> Unit
) {
    composable(PROFILE_ROUTE) {
        ProfileRoute(
            onNavigateBack = onBackClick,
            onNavigateToEditProfile = onNavigateToEditProfile
        )
    }
}

/**
 * 注册编辑个人资料页面路由
 */
fun NavGraphBuilder.editProfileScreen(
    onBackClick: () -> Unit
) {
    composable(EDIT_PROFILE_ROUTE) {
        EditProfileRoute(
            onNavigateBack = onBackClick
        )
    }
} 