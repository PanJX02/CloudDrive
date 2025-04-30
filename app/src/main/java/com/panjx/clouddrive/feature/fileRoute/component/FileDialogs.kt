package com.panjx.clouddrive.feature.fileRoute.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    onConfirm: () -> Unit
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
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = shareCode,
                    onValueChange = onShareCodeChange,
                    label = { Text("验证码 (选填)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = shareKey.isNotBlank()
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