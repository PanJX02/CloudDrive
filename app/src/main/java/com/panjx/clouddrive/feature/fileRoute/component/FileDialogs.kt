package com.panjx.clouddrive.feature.fileRoute.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NewFolderDialog(
    folderName: String,
    onFolderNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建文件夹") },
        text = {
            Column {
                Text("请输入文件夹名称：")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = folderName,
                    onValueChange = onFolderNameChange,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = folderName.isNotBlank()
            ) {
                Text("确认")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun ShareContentDialog(
    shareKey: String,
    shareCode: String,
    onShareKeyChange: (String) -> Unit,
    onShareCodeChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isLoading: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("获取分享内容") },
        text = {
            Column {
                Text("请输入分享密钥和验证码：")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = shareKey,
                    onValueChange = onShareKeyChange,
                    label = { Text("分享密钥 (必填)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = shareCode,
                    onValueChange = onShareCodeChange,
                    label = { Text("验证码 (选填)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                
                if (isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("验证中...")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = shareKey.isNotBlank() && !isLoading
            ) {
                Text("确认")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("取消")
            }
        }
    )
}

/**
 * 文件详情对话框
 */
@Composable
fun FileDetailDialog(
    fileDetail: com.panjx.clouddrive.core.modle.FileDetail?,
    isLoading: Boolean,
    errorMessage: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("文件详情") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    Text("正在加载文件详情...")
                } else if (errorMessage.isNotBlank()) {
                    Text("获取文件详情失败: $errorMessage")
                } else if (fileDetail != null) {
                    // 文件名
                    Text("文件名: ${fileDetail.fileName ?: "未知"}")
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 文件类型
                    Text("文件类型: ${if (fileDetail.folderType == 1) "文件夹" else "文件"}")
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 文件扩展名
                    if (fileDetail.fileExtension != null) {
                        Text("扩展名: ${fileDetail.fileExtension}")
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // 文件类别
                    if (fileDetail.fileCategory != null) {
                        Text("文件类别: ${fileDetail.fileCategory}")
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // 文件大小
                    fileDetail.fileSize?.let {
                        Text("文件大小: ${formatFileSize(it)}")
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // 创建时间
                    fileDetail.createTime?.let {
                        Text("创建时间: ${formatTimestamp(it)}")
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // 最后更新时间
                    fileDetail.lastUpdateTime?.let {
                        Text("最后更新: ${formatTimestamp(it)}")
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // 文件夹下文件和文件夹数量
                    if (fileDetail.folderType == 1) {
                        fileDetail.fileCount?.let {
                            Text("文件数量: $it")
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        fileDetail.folderCount?.let {
                            Text("子文件夹数量: $it")
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    
                    // 是否收藏
                    fileDetail.favoriteFlag?.let {
                        Text("收藏状态: ${if (it == 1) "已收藏" else "未收藏"}")
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // 哈希值
                    if (fileDetail.fileMd5 != null) {
                        Text("MD5: ${fileDetail.fileMd5}")
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    
                    if (fileDetail.fileSha1 != null) {
                        Text("SHA1: ${fileDetail.fileSha1}")
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    
                    if (fileDetail.fileSha256 != null) {
                        Text("SHA256: ${fileDetail.fileSha256}")
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                } else {
                    Text("未找到文件详情")
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

// 格式化文件大小
private fun formatFileSize(size: Long): String {
    if (size < 1024) return "$size B"
    val kb = size / 1024.0
    if (kb < 1024) return String.format("%.2f KB", kb)
    val mb = kb / 1024.0
    if (mb < 1024) return String.format("%.2f MB", mb)
    val gb = mb / 1024.0
    return String.format("%.2f GB", gb)
}

// 格式化时间戳
private fun formatTimestamp(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
    return sdf.format(date)
} 