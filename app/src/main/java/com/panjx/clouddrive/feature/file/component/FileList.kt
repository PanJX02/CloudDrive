package com.panjx.clouddrive.feature.file.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.panjx.clouddrive.core.modle.File
import com.panjx.clouddrive.feature.fileRoute.viewmodel.FileUiState

/**
 * 文件列表组件
 * 
 * @param uiState 文件列表的UI状态
 * @param files 文件列表数据
 * @param selectedFiles 已选中的文件ID列表
 * @param onSelectChange 文件选中状态变更回调
 * @param onFolderClick 文件夹点击回调
 * @param onRetry 重试加载回调
 * @param extraBottomSpace 底部额外空间，用于FAB或操作栏
 * @param isRefreshing 下拉刷新状态
 * @param showEmptyState 是否显示空状态提示
 * @param isSelectionMode 是否为选择模式
 * @param hideSelectionIcon 是否隐藏选择图标
 */
@Composable
fun FileList(
    uiState: FileUiState,
    files: List<File>,
    selectedFiles: List<Long>,
    onSelectChange: (fileId: Long, isSelected: Boolean) -> Unit,
    onFolderClick: (dirId: Long, dirName: String) -> Unit,
    onRetry: () -> Unit,
    extraBottomSpace: Dp = 0.dp,
    isRefreshing: Boolean = false,
    showEmptyState: Boolean = true,
    isSelectionMode: Boolean = false,
    hideSelectionIcon: Boolean = false,
    modifier: Modifier = Modifier
) {
    // 检查是否正在加载
    val isLoading = uiState is FileUiState.Loading
    val isListLoading = uiState is FileUiState.ListLoading
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 处理Loading和ListLoading状态
        if ((isLoading || isListLoading) && files.isEmpty() && !isRefreshing) {
            // 仅当列表为空且加载中且不是下拉刷新时显示加载指示器
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState is FileUiState.Error) {
            // 错误状态
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = uiState.message)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onRetry) { Text("重试") }
                }
            }
        } else {
            // 显示文件列表
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                if (files.isEmpty() && !isLoading && !isListLoading && showEmptyState) {
                    // 空文件夹提示，只有当showEmptyState为true时才显示
                    item { 
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 100.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.CreateNewFolder, 
                                    "空文件夹", 
                                    modifier = Modifier.size(60.dp), 
                                    tint = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "此文件夹为空", 
                                    style = MaterialTheme.typography.bodyLarge, 
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "点击右下角的 + 按钮添加文件", 
                                    style = MaterialTheme.typography.bodyMedium, 
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    } 
                } else {
                    // 文件列表
                    items(files, key = { it.id!! }) { file ->
                        ItemFile(
                            data = file,
                            isSelected = selectedFiles.contains(file.id),
                            onSelectChange = { isSelected -> 
                                file.id?.let { onSelectChange(it, isSelected) }
                            },
                            onFolderClick = { folderId, folderName ->
                                onFolderClick(folderId, folderName)
                            },
                            hasSelectedItems = selectedFiles.isNotEmpty(),
                            isSelectionMode = isSelectionMode,
                            hideSelectionIcon = hideSelectionIcon
                        )
                    }
                    
                    // 添加底部空间，确保当底部操作栏显示时内容不被遮挡
                    if (selectedFiles.isNotEmpty() && extraBottomSpace > 0.dp) {
                        item {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(extraBottomSpace)
                            )
                        }
                    }
                }
            }
        }
    }
} 