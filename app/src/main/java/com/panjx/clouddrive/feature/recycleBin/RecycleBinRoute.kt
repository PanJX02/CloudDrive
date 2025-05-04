package com.panjx.clouddrive.feature.recycleBin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.panjx.clouddrive.feature.file.component.FileList
import com.panjx.clouddrive.feature.fileRoute.viewmodel.FileUiState
import com.panjx.clouddrive.feature.recycleBin.component.RecycleBinActions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecycleBinRoute(
    onNavigateBack: () -> Unit,
    viewModel: RecycleBinViewModel = hiltViewModel()
) {
    // 获取状态
    val uiState by viewModel.uiState.collectAsState()
    val files by viewModel.files.collectAsState()
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    var showEmptyDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("回收站") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showEmptyDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "清空回收站")
                    }
                }
            )
        },
        bottomBar = {
            // 当有选中文件时显示操作栏
            if (selectedFiles.isNotEmpty()) {
                RecycleBinActions(
                    selectedCount = selectedFiles.size,
                    onRestore = { viewModel.restoreFiles() },
                    onDelete = { showDeleteDialog = true }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // 将RecycleBinUiState转换为FileUiState以复用FileList组件
            val fileUiState = when (uiState) {
                is RecycleBinUiState.Loading -> FileUiState.Loading
                is RecycleBinUiState.Error -> FileUiState.Error((uiState as RecycleBinUiState.Error).message)
                is RecycleBinUiState.Operating -> FileUiState.Loading
                else -> FileUiState.Success(files)
            }

            // 使用自定义的空状态显示，而不是依赖FileList组件的空状态
            if (files.isEmpty() && uiState !is RecycleBinUiState.Loading) {
                // 空状态显示
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "回收站空",
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "回收站是空的",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // 正常显示文件列表
                FileList(
                    uiState = fileUiState,
                    files = files,
                    selectedFiles = selectedFiles,
                    onSelectChange = { fileId, isSelected ->
                        viewModel.updateSelection(fileId, isSelected)
                    },
                    onFolderClick = { _, _ -> /* 回收站中的文件夹不可点击 */ },
                    onRetry = { viewModel.loadRecycleBinFiles() },
                    extraBottomSpace = if (selectedFiles.isNotEmpty()) 65.dp else 0.dp,
                    isRefreshing = isRefreshing,
                    // 重要：设置空列表处理为false，禁用FileList内部的空状态显示
                    showEmptyState = false
                )
            }
            
            // 显示加载指示器
            if (uiState is RecycleBinUiState.Loading || uiState is RecycleBinUiState.Operating) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    // 清空回收站对话框
    if (showEmptyDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyDialog = false },
            title = { Text("清空回收站") },
            text = { Text("确定要清空回收站吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.emptyRecycleBin()
                        showEmptyDialog = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 永久删除文件对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("永久删除") },
            text = { Text("确定要永久删除选中的${selectedFiles.size}个文件吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteFiles()
                        showDeleteDialog = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
} 