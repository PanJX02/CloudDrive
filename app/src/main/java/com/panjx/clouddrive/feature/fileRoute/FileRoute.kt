package com.panjx.clouddrive.feature.fileRoute

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.panjx.clouddrive.feature.fileRoute.component.FileScreen
import com.panjx.clouddrive.feature.transfersRoute.DownloadTransfersViewModel

/**
 * 文件路由主入口
 */
@Composable
fun FileRoute(
    viewModel: FileViewModel = viewModel(),
    // 向调用者提供实现的操作回调
    onActionsReady: (FileActions) -> Unit,
    // 当路由被销毁时的回调
    onDispose: () -> Unit,
    extraBottomSpace: Dp = 0.dp
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentPath by viewModel.currentPath.collectAsState()
    val selectedFiles = remember { mutableStateListOf<Long>() }
    val downloadViewModel: DownloadTransfersViewModel = hiltViewModel()
    val context = LocalContext.current
    
    // 重命名对话框状态管理
    var showRenameDialog = remember { mutableStateOf(false) }
    var fileToRename = remember { mutableStateOf<Pair<Long, String>?>(null) }
    var newFileName = remember { mutableStateOf("") }
    
    // 文件详情对话框状态管理
    var showFileDetailDialog = remember { mutableStateOf(false) }
    var fileDetail = remember { mutableStateOf<com.panjx.clouddrive.core.modle.FileDetail?>(null) }
    var isLoadingFileDetail = remember { mutableStateOf(false) }
    var fileDetailErrorMessage = remember { mutableStateOf("") }

    // 创建函数来清空选中文件
    val clearSelection = {
        if (selectedFiles.isNotEmpty()) {
            selectedFiles.clear()
        }
    }

    // 创建文件操作实例
    val fileOperations = remember {
        FileOperations(
            viewModel = viewModel,
            downloadViewModel = downloadViewModel,
            context = context,
            clearSelection = clearSelection
        )
    }

    // 在这里定义操作实现，访问viewModel和selectedFiles
    val fileActions = remember(selectedFiles.size) { // 当选择改变时重新创建操作
        val hasSelection = selectedFiles.isNotEmpty()
        val selectedFileIds = selectedFiles.toList()
        
        FileActions(
            onDownloadClick = { fileOperations.downloadFiles(selectedFileIds) },
            onMoveClick = { fileOperations.moveFiles(selectedFileIds) },
            onCopyClick = { fileOperations.copyFiles(selectedFileIds) },
            onFavoriteClick = { fileOperations.toggleFavorite(selectedFileIds) },
            onRenameClick = { 
                // 处理重命名操作
                if (selectedFileIds.size == 1) {
                    // 获取文件信息
                    val fileInfo = fileOperations.renameFile(selectedFileIds)
                    if (fileInfo != null) {
                        val (fileId, currentName) = fileInfo
                        // 设置重命名对话框状态
                        fileToRename.value = Pair(fileId, currentName)
                        newFileName.value = currentName
                        showRenameDialog.value = true
                    }
                }
            },
            onDeleteClick = { fileOperations.deleteFiles(selectedFileIds) },
            onShareClick = { fileOperations.shareFiles(selectedFileIds) },
            onDetailsClick = { 
                // 处理查看详情操作
                fileDetailErrorMessage.value = ""
                fileDetail.value = null
                isLoadingFileDetail.value = true
                showFileDetailDialog.value = true
                
                // 调用fileOperations.showFileDetails并获取详情
                fileOperations.showFileDetails(selectedFileIds)
                
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
        // 在导航前清空已选文件
        clearSelection()
        // 导航到新目录
        viewModel.loadDirectoryContent(dirId, dirName)
    }
    
    // 当操作变化时提供给调用者
    LaunchedEffect(fileActions) {
        onActionsReady(fileActions)
    }

    // 当此组件被销毁时通知调用者
    DisposableEffect(Unit) {
        onDispose {
            onDispose()
        }
    }

    // 将selectedFiles传递给FileScreen
    FileScreen(
        uiState = uiState,
        viewModel = viewModel,
        transfersViewModel = hiltViewModel(),
        currentPath = currentPath,
        selectedFiles = selectedFiles, // 传递列表
        onSelectChange = { fileId, isSelected -> // 在此处理选择变化
            if (isSelected) {
                selectedFiles.add(fileId)
            } else {
                selectedFiles.remove(fileId)
            }
        },
        onNavigateToDirectory = handleNavigateToDirectory, // 传递导航处理函数
        clearSelection = clearSelection, // 传递清空选择函数
        extraBottomSpace = extraBottomSpace, // 传递额外底部空间
        // 传递重命名对话框状态
        showRenameDialog = showRenameDialog.value,
        onShowRenameDialogChange = { showRenameDialog.value = it },
        fileToRename = fileToRename.value,
        onFileToRenameChange = { fileToRename.value = it },
        newFileName = newFileName.value,
        onNewFileNameChange = { newFileName.value = it },
        // 文件详情对话框参数
        showFileDetailDialog = showFileDetailDialog.value,
        onShowFileDetailDialogChange = { showFileDetailDialog.value = it },
        fileDetail = fileDetail.value,
        isLoadingFileDetail = isLoadingFileDetail.value,
        fileDetailErrorMessage = fileDetailErrorMessage.value
    )
}
