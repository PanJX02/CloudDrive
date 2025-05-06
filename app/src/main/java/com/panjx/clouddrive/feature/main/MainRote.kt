package com.panjx.clouddrive.feature.main

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.panjx.clouddrive.core.design.component.BottomBar
import com.panjx.clouddrive.data.UserPreferences
import com.panjx.clouddrive.feature.about.AboutRoute
import com.panjx.clouddrive.feature.announcements.AnnouncementsRoute
import com.panjx.clouddrive.feature.favorites.FavoritesRoute
import com.panjx.clouddrive.feature.file.FileOperationType
import com.panjx.clouddrive.feature.file.folderSelectionScreen
import com.panjx.clouddrive.feature.file.navigateToFolderSelection
import com.panjx.clouddrive.feature.fileRoute.FileActions
import com.panjx.clouddrive.feature.fileRoute.FileRoute
import com.panjx.clouddrive.feature.fileRoute.FileViewModel
import com.panjx.clouddrive.feature.fileRoute.ShareFileState
import com.panjx.clouddrive.feature.fileRoute.component.FileActionBar
import com.panjx.clouddrive.feature.fileRoute.navigation.navigateToSearchFile
import com.panjx.clouddrive.feature.fileRoute.navigation.navigateToShareFileList
import com.panjx.clouddrive.feature.fileRoute.navigation.searchFileScreen
import com.panjx.clouddrive.feature.fileRoute.navigation.shareFileListScreen
import com.panjx.clouddrive.feature.meRoute.MeRoute
import com.panjx.clouddrive.feature.profile.EDIT_PROFILE_ROUTE
import com.panjx.clouddrive.feature.profile.ProfileRoute
import com.panjx.clouddrive.feature.profile.editProfileScreen
import com.panjx.clouddrive.feature.recycleBin.RecycleBinRoute
import com.panjx.clouddrive.feature.settings.SettingsRoute
import com.panjx.clouddrive.feature.shared.SharedRoute
import com.panjx.clouddrive.feature.transfersRoute.TransfersRoute
import kotlinx.coroutines.launch

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
    // 状态：持有来自 FileRoute 的操作回调
    var fileActions by remember { mutableStateOf(FileActions()) }
    
    // 保存 FileActionBar 和 BottomBar 的高度
    var actionBarHeightPx by remember { mutableStateOf(0) }
    var bottomBarHeightPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    
    // 计算底部需要额外添加的空间高度（ActionBar高度 - BottomBar高度）
    // 如果差值为负，则不需要额外空间
    val extraBottomSpaceHeight = with(density) { 
        val actionBarDp = actionBarHeightPx.toDp()
        val bottomBarDp = bottomBarHeightPx.toDp()
        if (actionBarDp > bottomBarDp) actionBarDp - bottomBarDp else 0.dp
    }
    
    // 文件操作状态 - 用于显示操作结果
    val snackbarHostState = remember { SnackbarHostState() }
    // 协程作用域
    val scope = rememberCoroutineScope()
    
    // 文件视图模型
    val fileViewModel = viewModel<FileViewModel>()
    
    // 记录要复制或移动的文件ID列表
    var selectedFileIds by remember { mutableStateOf<List<Long>>(emptyList()) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { 
                SnackbarHost(
                    hostState = snackbarHostState,
                    snackbar = { snackbarData ->
                        Snackbar(
                            snackbarData = snackbarData,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            actionColor = MaterialTheme.colorScheme.primary
                        )
                    }
                ) 
            },
            bottomBar = {
                // 始终显示标准底部栏（如果shouldShowBottomBar为true）
                if (shouldShowBottomBar) {
                    Box(
                        modifier = Modifier
                            .onGloballyPositioned { coordinates ->
                                // 保存 BottomBar 高度
                                bottomBarHeightPx = coordinates.size.height
                            }
                    ) {
                        BottomBar(
                            navController = navController,
                            items = items
                        )
                    }
                }
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            // 主内容区域
            Box(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.File.route,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(Screen.File.route) {
                        FileRoute(
                            onActionsReady = { actions ->
                                // 更新文件操作
                                fileActions = actions.copy(
                                    onCopyClick = {
                                        // 保存选中的文件ID列表，从FileActions中获取
                                        selectedFileIds = actions.selectedFileIds
                                        // 导航到文件夹选择页面，排除当前目录
                                        val currentDirId = fileViewModel.currentDirId.value
                                        navController.navigateToFolderSelection(
                                            excludeFolderIds = currentDirId.toString(),
                                            operationType = FileOperationType.COPY
                                        )
                                    },
                                    onMoveClick = {
                                        // 保存选中的文件ID列表，从FileActions中获取
                                        selectedFileIds = actions.selectedFileIds
                                        // 导航到文件夹选择页面，排除当前目录
                                        val currentDirId = fileViewModel.currentDirId.value
                                        navController.navigateToFolderSelection(
                                            excludeFolderIds = currentDirId.toString(),
                                            operationType = FileOperationType.MOVE
                                        )
                                    }
                                )
                            },
                            onDispose = { 
                                fileActions = FileActions()
                            },
                            onNavigateToShareFileList = { shareKey, shareCode ->
                                navController.navigateToShareFileList(shareKey, shareCode)
                            },
                            extraBottomSpace = extraBottomSpaceHeight, // 传递计算出的额外空间高度
                            toSearch = {
                                // 添加搜索路由导航
                                navController.navigateToSearchFile()
                            }
                        )
                    }
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
                            onLogout = onNavigateToLogin,
                            onNavigateToAbout = {
                                navController.navigate(Screen.About.route)
                            },
                            onNavigateBack = {
                                navController.popBackStack()
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
                            },
                            onNavigateToEditProfile = {
                                navController.navigate(EDIT_PROFILE_ROUTE)
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
                    // 添加搜索页面路由
                    searchFileScreen(
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                    
                    // 分享文件列表路由
                    shareFileListScreen(
                        navController = navController,
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                    
                    // 文件夹选择页面路由 - 用于复制或移动文件
                    folderSelectionScreen(
                        onBackClick = { navController.popBackStack() },
                        onFolderSelected = { targetFolderId, operationType ->
                            // 根据操作类型执行不同的操作
                            when (operationType) {
                                FileOperationType.COPY -> {
                                    // 复制文件到选择的目标文件夹
                                    fileViewModel.copyFiles(selectedFileIds, targetFolderId) { success, message ->
                                        // 显示操作结果
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = message,
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                        // 返回文件页面
                                        navController.popBackStack()
                                    }
                                }
                                FileOperationType.MOVE -> {
                                    // 移动文件到选择的目标文件夹
                                    fileViewModel.moveFiles(selectedFileIds, targetFolderId) { success, message ->
                                        // 显示操作结果
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = message,
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                        // 返回文件页面
                                        navController.popBackStack()
                                    }
                                }
                                else -> { /* 不处理其他类型 */ }
                            }
                        },
                        // 分享文件转存处理
                        onShareSaveSelected = { targetFolderId, shareKey, shareCode ->
                            // 获取当前在分享页面选中的文件ID列表
                            val selectedShareFileIds = ShareFileState.selectedFileIds
                            
                            // 记录一下状态用于调试
                            Log.d("MainRote", "转存处理: 选中文件数: ${selectedShareFileIds.size}, shareKey: $shareKey")
                            
                            // 如果没有选择文件，显示提示并返回
                            if (selectedShareFileIds.isEmpty()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "请选择要转存的文件",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                                // 返回文件页面
                                navController.popBackStack()
                                navController.navigate(Screen.File.route) {
                                    popUpTo(Screen.File.route) {
                                        inclusive = true
                                    }
                                }
                            } else {
                                // 调用ViewModel执行转存操作
                                scope.launch {
                                    try {
                                        // 显示加载中提示
                                        snackbarHostState.showSnackbar("正在转存...")
                                        
                                        // 转存分享文件到选择的目标文件夹
                                        fileViewModel.saveShareFiles(
                                            fileIds = selectedShareFileIds,
                                            targetFolderId = targetFolderId,
                                            shareKey = shareKey,
                                            shareCode = shareCode
                                        ) { success, message ->
                                            // 显示操作结果
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = message,
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                            
                                            // 清空选择状态
                                            ShareFileState.clear()
                                            
                                            // 添加：如果转存成功，先刷新当前目录内容
                                            if (success) {
                                                // 如果转存目标是当前目录，直接刷新
                                                if (targetFolderId == fileViewModel.currentDirId.value) {
                                                    // 刷新当前目录内容
                                                    fileViewModel.loadData()
                                                } else {
                                                    // 否则标记目标文件夹需要刷新
                                                    fileViewModel.markFolderForRefresh(targetFolderId)
                                                }
                                                
                                                // 返回文件页面，先返回上一级
                                                navController.popBackStack()
                                                // 再导航到文件页面
                                                navController.navigate(Screen.File.route) {
                                                    // 清除导航栈中所有内容，避免重复
                                                    popUpTo(Screen.File.route) {
                                                        inclusive = true
                                                    }
                                                }
                                            } else {
                                                // 失败时只返回上一级
                                                navController.popBackStack()
                                            }
                                        }
                                    } catch (e: Exception) {
                                        // 显示错误信息
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "转存失败: ${e.message}",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                        
                                        // 清空选择状态
                                        ShareFileState.clear()
                                        
                                        // 返回文件页面
                                        navController.popBackStack()
                                        // 异常时也要返回文件页面
                                        navController.navigate(Screen.File.route) {
                                            popUpTo(Screen.File.route) {
                                                inclusive = true
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    )
                    
                    // 编辑个人资料页面
                    editProfileScreen(
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
        
        // FileActionBar 放在最外层 Box 中，确保它可以覆盖在所有内容上方
        AnimatedVisibility(
            visible = fileActions.hasSelection,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            FileActionBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        // 保存 FileActionBar 高度
                        actionBarHeightPx = coordinates.size.height
                    },
                onDownloadClick = fileActions.onDownloadClick,
                onMoveClick = fileActions.onMoveClick,
                onCopyClick = fileActions.onCopyClick,
                onFavoriteClick = fileActions.onFavoriteClick,
                onRenameClick = fileActions.onRenameClick,
                onDeleteClick = fileActions.onDeleteClick,
                onShareClick = fileActions.onShareClick,
                onDetailsClick = fileActions.onDetailsClick
            )
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



