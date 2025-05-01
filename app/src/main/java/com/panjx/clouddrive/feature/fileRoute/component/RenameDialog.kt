package com.panjx.clouddrive.feature.fileRoute.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 重命名对话框组件
 * 
 * @param fileName 当前文件名
 * @param onFileNameChange 文件名变更回调
 * @param onDismiss 取消对话框回调
 * @param onConfirm 确认重命名回调
 */
@Composable
fun RenameDialog(
    fileName: String,
    onFileNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("重命名") },
        text = { 
            Column {
                Text("请输入新的文件名：")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = fileName,
                    onValueChange = onFileNameChange,
                    label = { Text("文件名") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = fileName.isNotBlank()
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        modifier = Modifier.padding(16.dp)
    )
} 