package com.panjx.clouddrive.feature.favorites

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.panjx.clouddrive.feature.favorites.component.FavoritesActions
import com.panjx.clouddrive.feature.favorites.component.FavoritesOperations
import com.panjx.clouddrive.feature.file.component.FileExplorer
import com.panjx.clouddrive.feature.fileRoute.component.FileActionBar
import com.panjx.clouddrive.feature.fileRoute.component.FileDetailDialog
import com.panjx.clouddrive.feature.fileRoute.component.RenameDialog
import com.panjx.clouddrive.feature.fileRoute.component.ShareOptionsDialog
import com.panjx.clouddrive.feature.fileRoute.component.ShareResultDialog
import com.panjx.clouddrive.feature.fileRoute.viewmodel.FileUiState
import com.panjx.clouddrive.feature.transfersRoute.DownloadTransfersViewModel
import kotlinx.coroutines.launch

/**
 * 收藏夹顶部应用栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesTopBar(
    title: String = "收藏夹",
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    isSelectionMode: Boolean,
    selectedCount: Int = 0
) {
    TopAppBar(
        title = { 
            Text(
                if (isSelectionMode) "已选择 $selectedCount 项" else title
            ) 
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
            }
        }
    )
}

/**
 * 收藏夹主路由
 * 参考FileRoute实现更完整的多选和操作功能
 */
@Composable
fun FavoritesRoute(
    onNavigateBack: () -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel(),
    onNavigateToDirectory: ((dirId: Long, dirName: String?) -> Unit)? = null,
    onActionsReady: (FavoritesActions) -> Unit = {},
    onDispose: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentPath by viewModel.currentPath.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    
    // 使用与FileRoute类似的mutableStateListOf管理选中文件
    val selectedFiles = remember { mutableStateListOf<Long>() }
    val downloadViewModel: DownloadTransfersViewModel = hiltViewModel()
    val context = LocalContext.current
    
    // 添加选择模式状态
    val isSelectionMode = remember { mutableStateOf(false) }
    
    // 重命名对话框状态管理
    val showRenameDialog = remember { mutableStateOf(false) }
    val fileToRename = remember { mutableStateOf<Pair<Long, String>?>(null) }
    val newFileName = remember { mutableStateOf("") }
    
    // 文件详情对话框状态管理
    val showFileDetailDialog = remember { mutableStateOf(false) }
    val fileDetail = remember { mutableStateOf<com.panjx.clouddrive.core.modle.FileDetail?>(null) }
    val isLoadingFileDetail = remember { mutableStateOf(false) }
    val fileDetailErrorMessage = remember { mutableStateOf("") }
    
    // 分享对话框状态管理
    val showShareOptionsDialog = remember { mutableStateOf(false) }
    val filesToShare = remember { mutableStateOf<List<Long>>(emptyList()) }
    val showShareResultDialog = remember { mutableStateOf(false) }
    val shareResponse = remember { mutableStateOf<com.panjx.clouddrive.core.modle.response.ShareResponse?>(null) }
    
    // 消息显示
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // 显示消息的帮助函数
    fun showMessage(message: String) {
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }
    
    // 创建函数来清空选中文件，但不退出选择模式
    val clearSelection = {
        if (selectedFiles.isNotEmpty()) {
            selectedFiles.clear()
        }
    }
    
    // 创建函数来完全退出选择模式
    val exitSelectionMode = {
        isSelectionMode.value = false
        clearSelection()
    }
    
    // 当有文件被选中时，进入选择模式
    if (selectedFiles.isNotEmpty() && !isSelectionMode.value) {
        isSelectionMode.value = true
    }
    
    // 复制到剪贴板的函数
    val copyToClipboard = { text: String, isWithCode: Boolean ->
        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("分享链接", text)
        clipboard.setPrimaryClip(clip)
        val message = if (isWithCode) "已复制自带提取码的链接到剪贴板" else "已复制链接和提取码到剪贴板"
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
    }
    
    // 创建收藏夹操作实例
    val favoritesOperations = remember {
        FavoritesOperations(
            viewModel = viewModel,
            downloadViewModel = downloadViewModel,
            context = context,
            exitSelectionMode = exitSelectionMode
        )
    }
    
    // 在这里定义操作实现，访问viewModel和selectedFiles
    val favoritesActions = remember(selectedFiles.size, isSelectionMode.value) {
        val hasSelection = selectedFiles.isNotEmpty() || isSelectionMode.value
        val selectedFileIds = selectedFiles.toList()
        
        FavoritesActions(
            onDownloadClick = { 
                favoritesOperations.downloadFiles(selectedFileIds)
            },
            onMoveClick = {
                // 显示Snackbar提示暂不支持移动操作
                showMessage("暂不支持在收藏夹中移动文件")
            },
            onCopyClick = {
                // 显示Snackbar提示暂不支持复制操作
                showMessage("暂不支持在收藏夹中复制文件")
            },
            onFavoriteClick = {
                favoritesOperations.toggleFavorite(selectedFileIds)
            },
            onRenameClick = {
                // 处理重命名操作
                if (selectedFileIds.size == 1) {
                    // 获取文件信息
                    val fileInfo = favoritesOperations.renameFile(selectedFileIds)
                    if (fileInfo != null) {
                        val (fileId, currentName) = fileInfo
                        // 设置重命名对话框状态
                        fileToRename.value = Pair(fileId, currentName)
                        newFileName.value = currentName
                        showRenameDialog.value = true
                    }
                } else {
                    showMessage("请选择单个文件进行重命名")
                }
            },
            onRemoveFromFavoritesClick = {
                // 只执行从收藏夹移除操作，不执行删除操作
                favoritesOperations.removeFromFavorites(selectedFileIds)
            },
            onShareClick = {
                // 显示分享对话框
                favoritesOperations.shareFiles(selectedFileIds) { fileIds ->
                    filesToShare.value = fileIds
                    showShareOptionsDialog.value = true
                }
            },
            onDetailsClick = { 
                // 处理查看详情操作
                fileDetailErrorMessage.value = ""
                fileDetail.value = null
                isLoadingFileDetail.value = true
                showFileDetailDialog.value = true
                
                // 调用favoritesOperations.showFileDetails并获取详情
                favoritesOperations.showFileDetails(selectedFileIds)
                
                // 获取详情
                viewModel.getFileDetails(selectedFileIds) { success, message, detail ->
                    isLoadingFileDetail.value = false
                    if (success && detail != null) {
                        fileDetail.value = detail
                    } else {
                        fileDetailErrorMessage.value = message
                    }
                }
            },
            hasSelection = hasSelection,
            selectedFileIds = selectedFileIds
        )
    }
    
    // 处理目录导航
    val handleNavigateToDirectory = { dirId: Long, dirName: String? ->
        // 在导航前退出选择模式
        exitSelectionMode()
        // 导航到新目录
        viewModel.loadDirectoryContent(dirId, dirName)
    }
    
    // 当操作变化时提供给调用者
    LaunchedEffect(favoritesActions) {
        onActionsReady(favoritesActions)
    }
    
    // 当此组件被销毁时通知调用者
    DisposableEffect(Unit) {
        onDispose {
            onDispose()
        }
    }
    
    // 处理回退按钮
    BackHandler(enabled = currentPath.size > 1 || isSelectionMode.value) {
        if (isSelectionMode.value) {
            exitSelectionMode()
        } else {
            viewModel.navigateUp()
        }
    }

    Scaffold(
        topBar = {
            FavoritesTopBar(
                showBackButton = true,
                onBackClick = {
                    if (isSelectionMode.value) {
                        exitSelectionMode()
                    } else if (currentPath.size > 1) {
                        viewModel.navigateUp()
                    } else {
                        onNavigateBack()
                    }
                },
                isSelectionMode = isSelectionMode.value,
                selectedCount = selectedFiles.size
            )
        },
        bottomBar = {
            // 选中文件时显示操作栏
            if (isSelectionMode.value) {
                FileActionBar(
                    // 提供必要的操作回调
                    onDownloadClick = favoritesActions.onDownloadClick,
                    onMoveClick = favoritesActions.onMoveClick,
                    onCopyClick = favoritesActions.onCopyClick,
                    onFavoriteClick = favoritesActions.onFavoriteClick,
                    onRenameClick = favoritesActions.onRenameClick,
                    onDeleteClick = favoritesActions.onRemoveFromFavoritesClick, // 删除按钮用作移出收藏
                    onShareClick = favoritesActions.onShareClick,
                    onDetailsClick = favoritesActions.onDetailsClick
                )
            }
        },
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 使用FileExplorer组件，实现文件浏览和多选
            FileExplorer(
                viewModel = viewModel,
                selectedFiles = selectedFiles,
                onSelectChange = { fileId, isSelected -> 
                    // 直接操作selectedFiles列表，而不是调用viewModel
                    if (isSelected) {
                        selectedFiles.add(fileId)
                    } else {
                        selectedFiles.remove(fileId)
                    }
                },
                onNavigateToDirectory = { dirId, dirName ->
                    // 如果处于选择模式，点击文件夹不应导航
                    if (!isSelectionMode.value) {
                        handleNavigateToDirectory(dirId, dirName)
                    }
                },
                hideSelectionIcon = false,
                isSelectionMode = isSelectionMode.value,
                modifier = Modifier.fillMaxSize()
            )
            
            // 如果数据为空且不是加载状态，显示空状态提示
            if (uiState is FileUiState.Success && (uiState as FileUiState.Success).files.isEmpty() && !isRefreshing) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("收藏夹是空的")
                }
            }
        }
    }
    
    // 重命名对话框
    if (showRenameDialog.value && fileToRename.value != null) {
        RenameDialog(
            fileName = newFileName.value,
            onFileNameChange = { newFileName.value = it },
            onDismiss = { 
                showRenameDialog.value = false 
                fileToRename.value = null
                newFileName.value = ""
            },
            onConfirm = {
                val (fileId, _) = fileToRename.value!!
                // 执行重命名操作
                viewModel.renameFile(fileId, newFileName.value) { success, message ->
                    if (success) {
                        showMessage("重命名成功")
                        clearSelection()
                        exitSelectionMode()
                    } else {
                        showMessage("重命名失败: $message")
                    }
                }
                // 直接关闭对话框，不等待回调
                showRenameDialog.value = false
                fileToRename.value = null
                newFileName.value = ""
            }
        )
    }
    
    // 文件详情对话框
    if (showFileDetailDialog.value) {
        FileDetailDialog(
            fileDetail = fileDetail.value,
            isLoading = isLoadingFileDetail.value,
            errorMessage = fileDetailErrorMessage.value,
            onDismiss = { showFileDetailDialog.value = false }
        )
    }
    
    // 分享选项对话框
    if (showShareOptionsDialog.value) {
        ShareOptionsDialog(
            onDismiss = { 
                showShareOptionsDialog.value = false
                clearSelection()
            },
            onConfirm = { validType ->
                showShareOptionsDialog.value = false
                
                // 调用API进行分享
                viewModel.shareFile(filesToShare.value, validType) { success, message, response ->
                    if (success && response != null) {
                        // 显示分享结果对话框
                        shareResponse.value = response
                        showShareResultDialog.value = true
                    } else {
                        // 显示错误提示
                        showMessage(message)
                        clearSelection()
                    }
                }
            }
        )
    }
    
    // 分享结果对话框
    if (showShareResultDialog.value && shareResponse.value != null) {
        ShareResultDialog(
            shareResponse = shareResponse.value!!,
            onDismiss = { 
                showShareResultDialog.value = false
                clearSelection()
            },
            onCopy = { text, isWithCode ->
                copyToClipboard(text, isWithCode)
            }
        )
    }
} 