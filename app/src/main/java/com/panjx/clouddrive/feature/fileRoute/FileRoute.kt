package com.panjx.clouddrive.feature.fileRoute

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import com.panjx.clouddrive.feature.fileRoute.component.ShareOptionsDialog
import com.panjx.clouddrive.feature.fileRoute.component.ShareResultDialog
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
    // 导航到分享文件列表
    onNavigateToShareFileList: ((shareKey: String, shareCode: String) -> Unit)? = null,
    extraBottomSpace: Dp = 0.dp
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentPath by viewModel.currentPath.collectAsState()
    val selectedFiles = remember { mutableStateListOf<Long>() }
    val downloadViewModel: DownloadTransfersViewModel = hiltViewModel()
    val context = LocalContext.current
    
    // 添加选择模式状态
    val isSelectionMode = remember { mutableStateOf(false) }
    
    // 重命名对话框状态管理
    var showRenameDialog = remember { mutableStateOf(false) }
    var fileToRename = remember { mutableStateOf<Pair<Long, String>?>(null) }
    var newFileName = remember { mutableStateOf("") }
    
    // 文件详情对话框状态管理
    var showFileDetailDialog = remember { mutableStateOf(false) }
    var fileDetail = remember { mutableStateOf<com.panjx.clouddrive.core.modle.FileDetail?>(null) }
    var isLoadingFileDetail = remember { mutableStateOf(false) }
    var fileDetailErrorMessage = remember { mutableStateOf("") }
    
    // 分享对话框状态管理
    var showShareOptionsDialog = remember { mutableStateOf(false) }
    var filesToShare = remember { mutableStateOf<List<Long>>(emptyList()) }
    var showShareResultDialog = remember { mutableStateOf(false) }
    var shareResponse = remember { mutableStateOf<com.panjx.clouddrive.core.modle.response.ShareResponse?>(null) }

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
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("分享链接", text)
        clipboard.setPrimaryClip(clip)
        val message = if (isWithCode) "已复制自带提取码的链接到剪贴板" else "已复制链接和提取码到剪贴板"
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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
    val fileActions = remember(selectedFiles.size, isSelectionMode.value) { // 当选择改变或选择模式改变时重新创建操作
        val hasSelection = selectedFiles.isNotEmpty() || isSelectionMode.value // 修改判断逻辑
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
            onShareClick = { 
                // 显示分享对话框
                fileOperations.shareFiles(selectedFileIds) { fileIds ->
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
        // 在导航前退出选择模式
        exitSelectionMode()
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
        exitSelectionMode = exitSelectionMode, // 传递退出选择模式函数
        isSelectionMode = isSelectionMode.value, // 传递选择模式状态
        extraBottomSpace = extraBottomSpace, // 传递额外底部空间
        // 传递导航到分享文件列表的函数
        onNavigateToShareFileList = onNavigateToShareFileList,
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
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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
