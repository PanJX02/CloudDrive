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
import com.panjx.clouddrive.feature.fileRoute.FileUiState
import com.panjx.clouddrive.feature.fileRoute.FileViewModel
import com.panjx.clouddrive.feature.transfersRoute.TransfersViewModel

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
    extraBottomSpace: Dp = 0.dp,
    toSearch: () -> Unit = {},
    errorContent: @Composable (() -> Unit)? = null
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
    
    val currentDirId by viewModel.currentDirId.collectAsState()
    val context = LocalContext.current

    // 文件选择器
    val filePicker = FilePicker(
        transfersViewModel = transfersViewModel,
        currentDirId = currentDirId,
        context = context
    )

    // 返回处理
    BackHandler(enabled = currentPath.size > 1 || selectedFiles.isNotEmpty()) {
        if (selectedFiles.isNotEmpty()) {
            clearSelection()
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
    
    // 分享内容对话框
    if (showShareContentDialog) {
        ShareContentDialog(
            shareKey = shareKey,
            shareCode = shareCode,
            onShareKeyChange = { shareKey = it },
            onShareCodeChange = { shareCode = it },
            onDismiss = { 
                showShareContentDialog = false
                shareKey = ""
                shareCode = ""
            },
            onConfirm = {
                if (shareKey.isNotBlank()) {
                    // TODO: 处理获取分享内容的逻辑
                    Log.d("FileScreen", "获取分享内容: shareKey=$shareKey, code=$shareCode")
                }
                showShareContentDialog = false
                shareKey = ""
                shareCode = ""
            }
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
            FileTopBar(
                toSearch = toSearch,
                showBackIcon = currentPath.size > 1,
                onNavigateUp = { 
                    if (selectedFiles.isNotEmpty()) {
                        clearSelection()
                    } else {
                        viewModel.navigateUp() 
                    }
                }
            )
        },
        floatingActionButton = {
            val offsetX by animateFloatAsState(
                targetValue = if (selectedFiles.isEmpty()) 0f else 110f,
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
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        // 文件浏览器
        FileExplorer(
            viewModel = viewModel,
            selectedFiles = selectedFiles,
            onSelectChange = onSelectChange,
            onNavigateToDirectory = onNavigateToDirectory,
            extraBottomSpace = extraBottomSpace,
            modifier = Modifier.padding(innerPadding)
        )
    }
} 