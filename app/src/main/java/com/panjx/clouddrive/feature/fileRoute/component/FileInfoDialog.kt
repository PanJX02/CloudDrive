package com.panjx.clouddrive.feature.fileRoute.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.panjx.clouddrive.core.design.theme.SpaceSmall

/**
 * 文件信息对话框
 * 显示选择的文件信息，包括文件名、大小、类型等
 */
@Composable
fun FileInfoDialog(
    fileName: String,
    fileSize: String,
    fileSizeBytes: String = "",
    fileType: String,
    fileExtension: String = "",
    uploadFolderId: String = "",
    uploadFolderName: String = "",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "文件信息")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "文件名：$fileName",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(SpaceSmall))
                
                Text(
                    text = "文件大小：$fileSize",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                if (fileSizeBytes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(SpaceSmall))
                    
                    Text(
                        text = "字节大小：$fileSizeBytes 字节",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                Spacer(modifier = Modifier.height(SpaceSmall))
                
                Text(
                    text = "文件类型：$fileType",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                if (fileExtension.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(SpaceSmall))

                    Text(
                        text = "文件后缀：$fileExtension",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }else {
                    Spacer(modifier = Modifier.height(SpaceSmall))

                    Text(
                        text = "文件后缀：null",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                Spacer(modifier = Modifier.height(SpaceSmall))
                
                Text(
                    text = "上传位置：${if (uploadFolderName.isNotEmpty()) uploadFolderName else "根目录"}",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(SpaceSmall))
                
                Text(
                    text = "文件夹ID：$uploadFolderId",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm
            ) {
                Text("上传")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("取消")
            }
        }
    )
} 