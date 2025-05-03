package com.panjx.clouddrive.feature.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

/**
 * 分享链接对话框
 * 用于显示分享链接并提供复制功能
 */
@Composable
fun ShareLinkDialog(
    shareItem: SharedItem,
    onDismiss: () -> Unit,
    onCopy: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("分享链接") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("分享名称: ${shareItem.name}")
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("带提取码链接:")
                Text(
                    text = shareItem.shareKeyWithCode ?: "",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { onCopy(shareItem.shareKeyWithCode ?: "") }
                    ) {
                        Text("复制自带提取码链接")
                    }
                }

                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("普通链接:")
                Text(
                    text = shareItem.shareKey,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text("提取码: ${shareItem.code}")
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { onCopy("链接：${shareItem.shareKey} 提取码：${shareItem.code}") }
                    ) {
                        Text("复制链接和提取码")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    )
} 