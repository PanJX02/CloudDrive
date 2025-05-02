package com.panjx.clouddrive.feature.fileRoute.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.panjx.clouddrive.feature.fileRoute.screen.ShareFileListScreen

// 分享文件列表路由常量
const val SHARE_FILE_LIST_ROUTE = "share_file_list"

// 参数键
private const val SHARE_KEY_ARG = "shareKey"
private const val SHARE_CODE_ARG = "shareCode"

/**
 * 导航到分享文件列表页面
 */
fun NavController.navigateToShareFileList(shareKey: String, shareCode: String = "") {
    navigate("$SHARE_FILE_LIST_ROUTE/$shareKey?$SHARE_CODE_ARG=$shareCode")
}

/**
 * 注册分享文件列表路由
 */
fun NavGraphBuilder.shareFileListScreen(
    navController: NavController,
    onBackClick: () -> Unit
) {
    composable(
        route = "$SHARE_FILE_LIST_ROUTE/{$SHARE_KEY_ARG}?$SHARE_CODE_ARG={$SHARE_CODE_ARG}",
        arguments = listOf(
            navArgument(SHARE_KEY_ARG) {
                type = NavType.StringType
            },
            navArgument(SHARE_CODE_ARG) {
                type = NavType.StringType
                defaultValue = ""
            }
        )
    ) { backStackEntry ->
        val shareKey = backStackEntry.arguments?.getString(SHARE_KEY_ARG) ?: ""
        val shareCode = backStackEntry.arguments?.getString(SHARE_CODE_ARG) ?: ""
        
        ShareFileListScreen(
            shareKey = shareKey,
            shareCode = shareCode,
            onDismiss = onBackClick,
            navController = navController
        )
    }
} 