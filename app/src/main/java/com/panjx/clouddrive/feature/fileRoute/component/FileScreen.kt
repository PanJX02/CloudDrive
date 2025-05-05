package com.panjx.clouddrive.feature.fileRoute.component

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.panjx.clouddrive.core.design.component.FileTopBar
import com.panjx.clouddrive.core.modle.File
import com.panjx.clouddrive.feature.file.component.FileExplorer
import com.panjx.clouddrive.feature.fileRoute.FileViewModel
import com.panjx.clouddrive.feature.fileRoute.viewmodel.FileUiState
import com.panjx.clouddrive.feature.transfersRoute.TransfersViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 文件浏览界面主组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileScreen(
    uiState: FileUiState,
    viewModel: FileViewModel,
    transfersViewModel: TransfersViewModel,
    currentPath: List<Pair<Long, String>>,
    selectedFiles: List<Long>,
    onSelectChange: (fileId: Long, isSelected: Boolean) -> Unit,
    onNavigateToDirectory: (dirId: Long, dirName: String?) -> Unit,
    clearSelection: () -> Unit,
    exitSelectionMode: () -> Unit,
    isSelectionMode: Boolean,
    extraBottomSpace: Dp = 0.dp,
    toSearch: () -> Unit = {},
    onNavigateToShareFileList: ((shareKey: String, shareCode: String) -> Unit)? = null,
    errorContent: @Composable (() -> Unit)? = null,
    // 重命名对话框参数
    showRenameDialog: Boolean = false,
    onShowRenameDialogChange: (Boolean) -> Unit = {},
    fileToRename: Pair<Long, String>? = null,
    onFileToRenameChange: (Pair<Long, String>?) -> Unit = {},
    newFileName: String = "",
    onNewFileNameChange: (String) -> Unit = {},
    // 文件详情对话框参数
    showFileDetailDialog: Boolean = false,
    onShowFileDetailDialogChange: (Boolean) -> Unit = {},
    fileDetail: com.panjx.clouddrive.core.modle.FileDetail? = null,
    isLoadingFileDetail: Boolean = false,
    fileDetailErrorMessage: String = "",
    // Snackbar状态
    snackbarHostState: SnackbarHostState,
    // 自定义Snackbar
    customSnackbar: @Composable (SnackbarData) -> Unit = { Snackbar(it) }
) {
    // 确定文件列表和加载状态
    val (files, isListLoading) = when (uiState) {
        is FileUiState.Success -> uiState.files to false
        is FileUiState.ListLoading -> emptyList<File>() to true
        is FileUiState.Loading -> emptyList<File>() to true
        is FileUiState.Error -> emptyList<File>() to false
    }

    // 各种状态
    var showBottomSheet by remember { mutableStateOf(false) }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var showShareContentDialog by remember { mutableStateOf(false) }
    var shareKey by remember { mutableStateOf("") }
    var shareCode by remember { mutableStateOf("") }
    var isValidatingShare by remember { mutableStateOf(false) }
    
    val currentDirId by viewModel.currentDirId.collectAsState()
    val context = LocalContext.current

    // 文件选择器
    val filePicker = FilePicker(
        transfersViewModel = transfersViewModel,
        currentDirId = currentDirId,
        context = context
    )

    // 返回处理
    BackHandler(enabled = currentPath.size > 1 || isSelectionMode) {
        if (isSelectionMode) {
            exitSelectionMode()
        } else {
            viewModel.navigateUp()
        }
    }

    // 新建文件夹对话框
    if (showNewFolderDialog) {
        NewFolderDialog(
            folderName = newFolderName,
            onFolderNameChange = { newFolderName = it },
            onDismiss = { 
                showNewFolderDialog = false
                newFolderName = ""
            },
            onConfirm = {
                if (newFolderName.isNotBlank()) {
                    viewModel.createFolder(newFolderName) { success, message ->
                        if (success) {
                            Log.d("FileScreen", "文件夹创建成功: $message")
                        } else {
                            Log.e("FileScreen", "文件夹创建失败: $message")
                        }
                    }
                }
                showNewFolderDialog = false
                newFolderName = ""
            }
        )
    }
    
    // 重命名对话框
    if (showRenameDialog && fileToRename != null) {
        RenameDialog(
            fileName = newFileName,
            onFileNameChange = onNewFileNameChange,
            onDismiss = { 
                onShowRenameDialogChange(false)
                onFileToRenameChange(null)
                onNewFileNameChange("")
            },
            onConfirm = {
                val fileName = newFileName.trim()
                if (fileName.isNotBlank()) {
                    val (fileId, oldName) = fileToRename
                    if (fileName != oldName) {  // 只有当名称发生变化时才执行重命名
                        viewModel.renameFile(fileId, fileName) { success, message ->
                            if (success) {
                                // 使用Snackbar显示成功消息
                                CoroutineScope(Dispatchers.Main).launch {
                                    snackbarHostState.showSnackbar("重命名成功")
                                }
                                clearSelection()
                                exitSelectionMode()
                            } else {
                                // 使用Snackbar显示错误消息
                                CoroutineScope(Dispatchers.Main).launch {
                                    snackbarHostState.showSnackbar("重命名失败: $message")
                                }
                            }
                        }
                    }
                }
                // 直接关闭对话框，不等待回调
                onShowRenameDialogChange(false)
                onFileToRenameChange(null)
                onNewFileNameChange("")
            }
        )
    }
    
    // 文件详情对话框
    if (showFileDetailDialog) {
        FileDetailDialog(
            fileDetail = fileDetail,
            isLoading = isLoadingFileDetail,
            errorMessage = fileDetailErrorMessage,
            onDismiss = { onShowFileDetailDialogChange(false) }
        )
    }
    
    // 分享内容对话框
    if (showShareContentDialog) {
        ShareContentDialog(
            shareKey = shareKey,
            shareCode = shareCode,
            onShareKeyChange = { shareKey = it },
            onShareCodeChange = { shareCode = it },
            onDismiss = { 
                if (!isValidatingShare) {
                    showShareContentDialog = false
                    shareKey = ""
                    shareCode = ""
                }
            },
            onConfirm = {
                if (shareKey.isNotBlank() && !isValidatingShare) {
                    isValidatingShare = true
                    
                    Log.d("FileScreen", "验证分享密钥: shareKey=$shareKey, code=$shareCode")
                    
                    viewModel.getShareFileList(shareKey, shareCode, null) { success, message, fileList ->
                        isValidatingShare = false
                        
                        if (success && fileList != null) {
                            showShareContentDialog = false
                            
                            onNavigateToShareFileList?.invoke(shareKey, shareCode)
                            
                            shareKey = ""
                            shareCode = ""
                        } else {
                            (context as? android.app.Activity)?.runOnUiThread {
                                android.widget.Toast.makeText(
                                    context,
                                    "无法获取分享内容: $message",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                            
                            if (message != "无效的分享链接") {
                                showShareContentDialog = false
                                shareKey = ""
                                shareCode = ""
                            }
                        }
                    }
                }
            },
            isLoading = isValidatingShare
        )
    }

    // 底部菜单
    if (showBottomSheet) {
        FileBottomSheet(
            transfersViewModel = transfersViewModel,
            currentDirId = currentDirId,
            onDismiss = { showBottomSheet = false },
            onNewFolderClick = { showNewFolderDialog = true },
            onShareContentClick = { showShareContentDialog = true }
        )
    }

    // 主界面脚手架
    Scaffold(
        topBar = {
            // 根据是否处于选择模式来决定显示哪种顶部栏
            if (isSelectionMode) {
                // 计算是否已全选：判断当前显示的所有文件是否都已被选中
                val allFileIds = files.mapNotNull { it.id }
                val isAllSelected = allFileIds.isNotEmpty() && 
                                   allFileIds.all { selectedFiles.contains(it) }
                
                // 选择模式下显示选择顶部栏
                FileSelectionTopBar(
                    selectedCount = selectedFiles.size,
                    onClearSelection = exitSelectionMode,
                    isAllSelected = isAllSelected,
                    onSelectAllClick = {
                        if (isAllSelected) {
                            // 如果已全选，执行取消全选操作
                            clearSelection() // 这里只清除选择，不退出选择模式
                        } else {
                            // 如果未全选，执行全选操作
                            allFileIds.forEach { fileId ->
                                if (!selectedFiles.contains(fileId)) {
                                    onSelectChange(fileId, true)
                                }
                            }
                        }
                    }
                )
            } else {
                // 非选择模式下显示普通顶部栏
                FileTopBar(
                    toSearch = toSearch,
                    showBackIcon = currentPath.size > 1,
                    onNavigateUp = { 
                        viewModel.navigateUp() 
                    }
                )
            }
        },
        floatingActionButton = {
            val offsetX by animateFloatAsState(
                targetValue = if (isSelectionMode) 110f else 0f,
                animationSpec = tween(300),
                label = "fabOffset"
            )
            
            FloatingActionButton(
                onClick = { showBottomSheet = true },
                modifier = Modifier
                    .padding(bottom = 40.dp)
                    .offset(x = (-32).dp + offsetX.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加"
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        snackbarHost = { 
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = customSnackbar
            )
        }
    ) { innerPadding ->
        // 文件浏览器
        FileExplorer(
            viewModel = viewModel,
            selectedFiles = selectedFiles,
            onSelectChange = onSelectChange,
            onNavigateToDirectory = onNavigateToDirectory,
            extraBottomSpace = extraBottomSpace,
            isSelectionMode = isSelectionMode,
            modifier = Modifier.padding(innerPadding)
        )
    }
} 