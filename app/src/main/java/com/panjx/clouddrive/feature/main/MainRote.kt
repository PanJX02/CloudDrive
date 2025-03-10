package com.panjx.clouddrive.feature.main

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.panjx.clouddrive.core.design.component.BottomBar
import com.panjx.clouddrive.core.design.theme.Gray20
import com.panjx.clouddrive.core.design.theme.Gray40
import com.panjx.clouddrive.data.UserPreferences
import com.panjx.clouddrive.feature.about.AboutRoute
import com.panjx.clouddrive.feature.fileRoute.FileRoute
import com.panjx.clouddrive.feature.login.LOGIN_ROUTE
import com.panjx.clouddrive.feature.meRoute.MeRoute
import com.panjx.clouddrive.feature.settings.SettingsRoute
import com.panjx.clouddrive.feature.transfersRoute.TransfersRoute
import com.panjx.clouddrive.feature.shared.SharedRoute
import com.panjx.clouddrive.feature.favorites.FavoritesRoute
import com.panjx.clouddrive.feature.recycleBin.RecycleBinRoute
import com.panjx.clouddrive.feature.profile.ProfileRoute
import com.panjx.clouddrive.feature.announcements.AnnouncementsRoute

@Composable
fun MainRote(
    finishPage: () -> Unit,
    userPreferences: UserPreferences,
    onNavigateToLogin: () -> Unit
) {
    MainScreen(finishPage, userPreferences, onNavigateToLogin)
}

@Composable
fun MainScreen(
    finishPage: () -> Unit = {},
    userPreferences: UserPreferences,
    onNavigateToLogin: () -> Unit
) {
    Log.d("Composable", "MainScreen")
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // 定义底部导航项对应路由
    val items = listOf(
        Screen.File,
        Screen.Transfers,
        Screen.Me
    )

    // 判断是否显示底部导航栏
    val shouldShowBottomBar = currentDestination?.route in items.map { it.route }

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                BottomBar(
                    navController = navController,
                    items = items
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0) // 禁用默认内边距
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.File.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.File.route) { FileRoute() }
            composable(Screen.Transfers.route) { TransfersRoute() }
            composable(Screen.Me.route) { 
                MeRoute(
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onNavigateToShared = {
                        navController.navigate(Screen.Shared.route)
                    },
                    onNavigateToFavorites = {
                        navController.navigate(Screen.Favorites.route)
                    },
                    onNavigateToRecycleBin = {
                        navController.navigate(Screen.RecycleBin.route)
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route)
                    },
                    onNavigateToAnnouncements = {
                        navController.navigate(Screen.Announcements.route)
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsRoute(
                    userPreferences = userPreferences,
                    onLogout = onNavigateToLogin,
                    onNavigateToAbout = {
                        navController.navigate(Screen.About.route)
                    }
                )
            }
            composable(Screen.About.route) {
                AboutRoute(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.Shared.route) {
                SharedRoute(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.Favorites.route) {
                FavoritesRoute(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.RecycleBin.route) {
                RecycleBinRoute(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.Profile.route) {
                ProfileRoute(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.Announcements.route) {
                AnnouncementsRoute(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
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
        icon = Icons.Filled.Folder
    )

    object Transfers : Screen(
        route = "transfers_route",
        label = "传输",
        icon = Icons.Filled.SwapVert
    )

    object Me : Screen(
        route = "me_route",
        label = "我的",
        icon = Icons.Filled.Person
    )

    object Settings : Screen(
        route = "settings_route",
        label = "设置",
        icon = Icons.Filled.Settings
    )

    object About : Screen(
        route = "about_route",
        label = "关于",
        icon = Icons.Filled.Info
    )

    object Shared : Screen(
        route = "shared_route",
        label = "我的分享",
        icon = Icons.Filled.Share
    )

    object Favorites : Screen(
        route = "favorites_route",
        label = "收藏夹",
        icon = Icons.Filled.Favorite
    )

    object RecycleBin : Screen(
        route = "recycle_bin_route",
        label = "回收站",
        icon = Icons.Filled.Delete
    )

    object Profile : Screen(
        route = "profile_route",
        label = "个人中心",
        icon = Icons.Filled.AccountCircle
    )

    object Announcements : Screen(
        route = "announcements_route",
        label = "公告中心",
        icon = Icons.Filled.Notifications
    )
}



