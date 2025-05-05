package com.panjx.clouddrive.feature.fileRoute.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.panjx.clouddrive.core.modle.File
import com.panjx.clouddrive.core.modle.FileDetail
import com.panjx.clouddrive.core.modle.response.ShareResponse
import com.panjx.clouddrive.feature.file.component.BreadcrumbNavigator
import com.panjx.clouddrive.feature.file.component.ItemFile
import com.panjx.clouddrive.feature.fileRoute.SearchOperations
import com.panjx.clouddrive.feature.fileRoute.component.FileActionBar
import com.panjx.clouddrive.feature.fileRoute.component.FileDetailDialog
import com.panjx.clouddrive.feature.fileRoute.component.RenameDialog
import com.panjx.clouddrive.feature.fileRoute.component.SearchTopBar
import com.panjx.clouddrive.feature.fileRoute.component.ShareOptionsDialog
import com.panjx.clouddrive.feature.fileRoute.component.ShareResultDialog
import com.panjx.clouddrive.feature.fileRoute.viewmodel.FileOperationViewModel
import com.panjx.clouddrive.feature.fileRoute.viewmodel.SearchUiState
import com.panjx.clouddrive.feature.fileRoute.viewmodel.SearchViewModel
import com.panjx.clouddrive.feature.transfersRoute.DownloadTransfersViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFileScreen(
    onBackClick: () -> Unit, // Callback to navigate back from the search feature
    searchViewModel: SearchViewModel = hiltViewModel(),
    operationViewModel: FileOperationViewModel = hiltViewModel(), // 添加 FileOperationViewModel
    downloadViewModel: DownloadTransfersViewModel = hiltViewModel(), // 添加 DownloadTransfersViewModel
    onFilesSelected: (List<Long>) -> Unit = {} // 回调函数，用于通知上层文件已选中
) {
    val searchUiState by searchViewModel.uiState.collectAsState()
    val isRefreshing by searchViewModel.isRefreshing.collectAsState(initial = false) // 添加获取isRefreshing状态
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // --- 对话框状态管理 ---
    val showRenameDialog = rememberSaveable { mutableStateOf(false) }
    val fileToRename = remember { mutableStateOf<Pair<Long, String>?>(null) }
    val newFileName = rememberSaveable { mutableStateOf("") } // 保存重命名输入

    val showFileDetailDialog = remember { mutableStateOf(false) }
    val fileDetail = remember { mutableStateOf<FileDetail?>(null) }
    val isLoadingFileDetail = remember { mutableStateOf(false) }
    val fileDetailErrorMessage = remember { mutableStateOf("") }

    val showShareOptionsDialog = remember { mutableStateOf(false) }
    val filesToShare = remember { mutableStateOf<List<Long>>(emptyList()) }
    val showShareResultDialog = remember { mutableStateOf(false) }
    val shareResponse = remember { mutableStateOf<ShareResponse?>(null) }
    // ------

    // 消息显示函数
    val showMessage: (String) -> Unit = { message ->
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    // 创建 SearchOperations 实例
    val searchOperations = remember(searchViewModel, operationViewModel, downloadViewModel, context) {
        SearchOperations(
            searchViewModel = searchViewModel,
            operationViewModel = operationViewModel,
            downloadViewModel = downloadViewModel,
            context = context,
            exitSelectionMode = searchViewModel::exitSelectionMode,
            // 传递对话框状态
            showRenameDialog = showRenameDialog,
            fileToRename = fileToRename,
            newFileName = newFileName,
            showFileDetailDialog = showFileDetailDialog,
            fileDetail = fileDetail,
            isLoadingFileDetail = isLoadingFileDetail,
            fileDetailErrorMessage = fileDetailErrorMessage,
            showShareOptionsDialog = showShareOptionsDialog,
            filesToShare = filesToShare,
            showShareResultDialog = showShareResultDialog,
            shareResponse = shareResponse,
            showMessage = showMessage
        )
    }

    val currentDirId = searchUiState.navigationPath.lastOrNull()?.first ?: 0L
    val currentFolderName = searchUiState.navigationPath.lastOrNull()?.second ?: ""

    // 自动聚焦到搜索框 (仅在初始搜索结果页面)
    LaunchedEffect(currentDirId) {
        if (currentDirId == 0L && !searchUiState.isSelectionMode) { // 仅在非选择模式下聚焦
            focusRequester.requestFocus()
        }
    }

    // 当有文件选中时，通知上层
    LaunchedEffect(searchUiState.selectedFiles) {
        if (searchUiState.selectedFiles.isNotEmpty()) {
            onFilesSelected(searchUiState.selectedFiles)
        }
    }

    // 处理返回按钮
    BackHandler(enabled = searchUiState.navigationPath.size > 1 || searchUiState.isSelectionMode) {
        if (searchUiState.isSelectionMode) {
            searchViewModel.exitSelectionMode()
        } else if (searchUiState.navigationPath.size > 1) {
            searchViewModel.navigateUp()
        } else {
            onBackClick()
        }
    }

    Scaffold(
        snackbarHost = { 
            SnackbarHost(hostState = snackbarHostState) {
                Snackbar(
                    snackbarData = it,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionColor = MaterialTheme.colorScheme.primary
                )
            }
        },
        topBar = {
            SearchTopBar(
                searchQuery = searchUiState.searchQuery,
                onQueryChange = searchViewModel::updateSearchQuery,
                onSearch = searchViewModel::performSearch,
                onBackClick = {
                    if (searchUiState.isSelectionMode) {
                        searchViewModel.exitSelectionMode()
                    } else if (searchUiState.navigationPath.size > 1) {
                        searchViewModel.navigateUp()
                    } else {
                        onBackClick()
                    }
                },
                onClear = searchViewModel::clearSearch,
                currentDirId = currentDirId,
                currentFolderName = currentFolderName,
                focusRequester = focusRequester,
                isSelectionMode = searchUiState.isSelectionMode,
                selectedCount = searchUiState.selectedFiles.size,
                onCancelSelection = searchViewModel::exitSelectionMode
            )
        },
        bottomBar = { // 添加底部操作栏
            if (searchUiState.isSelectionMode) {
                FileActionBar(
                    onDownloadClick = searchOperations::downloadFiles,
                    onMoveClick = searchOperations::moveFiles,
                    onCopyClick = searchOperations::copyFiles,
                    onFavoriteClick = searchOperations::toggleFavorite,
                    onRenameClick = { searchOperations.renameFile() }, // 触发显示对话框
                    onDeleteClick = searchOperations::deleteFiles,
                    onShareClick = searchOperations::shareFiles,
                    onDetailsClick = searchOperations::showFileDetails
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // 应用 Scaffold 提供的 padding
        ) {
            // 内容区域
            SearchContent(
                uiState = searchUiState,
                onNavigateToFolder = searchViewModel::navigateToFolder,
                onNavigateToPathIndex = searchViewModel::navigateToPathIndex,
                onSelectChange = searchViewModel::selectFile,
                isRefreshing = isRefreshing, // 添加刷新状态
                onRefresh = searchViewModel::refreshCurrentFolder // 添加刷新回调
            )
        }

        // --- 对话框 --- 
        // 重命名对话框
        if (showRenameDialog.value && fileToRename.value != null) {
            RenameDialog(
                fileName = newFileName.value,
                onFileNameChange = { newFileName.value = it },
                onDismiss = { 
                    showRenameDialog.value = false
                    fileToRename.value = null 
                },
                onConfirm = { 
                    // 调用 renameFile 并传入新名称
                    fileToRename.value?.first?.let {
                         searchOperations.renameFile(fileId = it, newName = newFileName.value)
                    } 
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
                onDismiss = { showShareOptionsDialog.value = false },
                onConfirm = searchOperations::performShare
            )
        }

        // 分享结果对话框
        if (showShareResultDialog.value && shareResponse.value != null) {
            ShareResultDialog(
                shareResponse = shareResponse.value!!,
                onDismiss = searchOperations::onShareResultDialogDismiss,
                onCopy = searchOperations::copyToClipboard
            )
        }
        // --- End 对话框 ---
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchContent(
    uiState: SearchUiState,
    onNavigateToFolder: (File) -> Unit,
    onNavigateToPathIndex: (Int) -> Unit,
    onSelectChange: (fileId: Long, isSelected: Boolean) -> Unit,
    isRefreshing: Boolean = false, // 添加isRefreshing参数
    onRefresh: () -> Unit = {} // 添加onRefresh回调
) {
    val currentDirId = uiState.navigationPath.lastOrNull()?.first ?: 0L

    // 使用PullToRefreshBox包装内容
    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 面包屑导航 - 始终显示（如果路径不为空）
            // 可以在 BreadcrumbNavigator 内部处理 path 为空的情况
            BreadcrumbNavigator(
                currentPath = uiState.navigationPath,
                onNavigate = { dirId ->
                    val index = uiState.navigationPath.indexOfFirst { it.first == dirId }
                    if (index >= 0) {
                        onNavigateToPathIndex(index)
                    }
                }
            )

            // 根据状态显示不同内容
            if (uiState.isSearching && uiState.currentFiles.isEmpty()) { // 优化加载显示
                Text(
                    text = "正在加载...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .weight(1f), // 使其填充剩余空间
                    textAlign = TextAlign.Center
                )
            } else if (currentDirId == 0L && uiState.searchQuery.isEmpty()) {
                Text(
                    text = "请输入搜索关键词",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .weight(1f), // 使其填充剩余空间
                    textAlign = TextAlign.Center
                )
            } else if (currentDirId == 0L && uiState.currentFiles.isEmpty() && uiState.searchQuery.isNotEmpty() && !uiState.isSearching) {
                // Show 'no results' only if a search was performed and finished
                Text(
                    text = "未找到匹配文件",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .weight(1f), // 使其填充剩余空间
                    textAlign = TextAlign.Center
                )
            } else {
                // 文件列表
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize() // 填充剩余空间
                        .padding(top = 8.dp) // 与面包屑的间距
                ) {
                    items(uiState.currentFiles, key = { it.id ?: 0 }) { file ->
                        val isSelected = file.id?.let { uiState.selectedFiles.contains(it) } ?: false

                        // 直接使用ItemFile组件
                        ItemFile(
                            data = file,
                            isSelected = isSelected,
                            onSelectChange = { selected ->
                                file.id?.let { fileId ->
                                    onSelectChange(fileId, selected)
                                }
                            },
                            onFolderClick = { dirId, dirName ->
                                // 只有在非选择模式下才响应文件夹点击
                                if (!uiState.isSelectionMode) {
                                    val fileToNavigate = uiState.currentFiles.find { it.id == dirId }
                                    if (fileToNavigate != null) {
                                        onNavigateToFolder(fileToNavigate)
                                    }
                                }
                            },
                            hasSelectedItems = uiState.selectedFiles.isNotEmpty(),
                            isSelectionMode = uiState.isSelectionMode,
                            hideSelectionIcon = false // 在搜索结果中显示选择图标
                        )
                    }
                }
            }
        }
    }
} 