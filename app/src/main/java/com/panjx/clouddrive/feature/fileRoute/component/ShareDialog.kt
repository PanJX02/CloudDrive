package com.panjx.clouddrive.feature.fileRoute.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

/**
 * 分享选项对话框
 * 用于选择分享的有效期
 */
@Composable
fun ShareOptionsDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    // 有效期选项
    val options = listOf(
        "1天" to 0,
        "7天" to 1,
        "30天" to 2,
        "永久" to 3
    )
    
    // 当前选中的有效期索引
    var selectedOption by remember { mutableStateOf(options[0].second) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择分享有效期") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup()
            ) {
                options.forEach { (text, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .selectable(
                                selected = (value == selectedOption),
                                onClick = { selectedOption = value },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (value == selectedOption),
                            onClick = null // null because we're handling the click on the row
                        )
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedOption) }
            ) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("取消")
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    )
}

/**
 * 分享结果对话框
 * 用于显示分享结果
 */
@Composable
fun ShareResultDialog(
    shareResponse: com.panjx.clouddrive.core.modle.response.ShareResponse,
    onDismiss: () -> Unit,
    onCopy: (String, Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("分享成功") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("分享名称: ${shareResponse.shareName}")
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("带提取码链接:")
                Text(
                    text = shareResponse.shareKeyWithCode,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { onCopy(shareResponse.shareKeyWithCode, true) }
                    ) {
                        Text("复制自带提取码链接")
                    }
                }

                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("普通链接:")
                Text(
                    text = shareResponse.shareKey,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text("提取码: ${shareResponse.code}")
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { onCopy("链接：${shareResponse.shareKey} 提取码：${shareResponse.code}", false) }
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