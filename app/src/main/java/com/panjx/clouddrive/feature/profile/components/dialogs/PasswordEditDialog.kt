package com.panjx.clouddrive.feature.profile.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.panjx.clouddrive.feature.profile.components.widgets.PasswordInput

@Composable
fun PasswordEditDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    onError: (String) -> Unit = {},
    externalError: String? = null,
    canSubmitWithSameError: Boolean = false
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var previousError by remember { mutableStateOf<String?>(null) }
    
    // 处理外部错误
    LaunchedEffect(externalError) {
        if (externalError != null) {
            // 检查是否是新的错误，或者允许相同错误重复提交
            if (externalError != previousError || canSubmitWithSameError) {
                error = externalError
                previousError = externalError
                isSubmitting = false
            }
        } else {
            // 外部错误为空时也重置提交状态
            isSubmitting = false
        }
    }
    
    // 当错误状态改变且非空时，调用onError回调
    LaunchedEffect(error) {
        if (error != null) {
            onError(error!!)
        }
    }
    
    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("修改密码") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // 当前密码输入
                PasswordInput(
                    value = oldPassword,
                    onValueChange = { 
                        oldPassword = it
                        // 清除与原密码相关的错误
                        if (error?.contains("原密码") == true) {
                            error = null
                            previousError = null
                        }
                    },
                    label = "当前密码",
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting,
                    isError = error?.contains("原密码") == true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 新密码输入
                PasswordInput(
                    value = newPassword,
                    onValueChange = { 
                        newPassword = it
                        // 清除与新密码相同的错误
                        if (error?.contains("新密码不能与原密码相同") == true) {
                            error = null
                            previousError = null
                        }
                    },
                    label = "新密码",
                    supportingText = "密码至少6位",
                    isError = error?.contains("新密码") == true || error?.contains("两次密码") == true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 确认新密码
                PasswordInput(
                    value = confirmPassword,
                    onValueChange = { 
                        confirmPassword = it
                        // 清除两次密码不一致的错误
                        if (error?.contains("两次密码") == true) {
                            error = null
                            previousError = null
                        }
                    },
                    label = "确认新密码",
                    isError = error?.contains("两次密码") == true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting
                )
                
                // 错误提示区域
                if (error != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        oldPassword.isEmpty() -> {
                            error = "请输入当前密码"
                            previousError = error
                        }
                        newPassword.isEmpty() -> {
                            error = "请输入新密码"
                            previousError = error
                        }
                        newPassword.length < 6 -> {
                            error = "密码长度至少6位"
                            previousError = error
                        }
                        newPassword != confirmPassword -> {
                            error = "两次密码输入不一致"
                            previousError = error
                        }
                        else -> {
                            error = null
                            previousError = null
                            isSubmitting = true // 禁用按钮
                            onConfirm(oldPassword, newPassword)
                        }
                    }
                },
                enabled = !isSubmitting
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting
            ) {
                Text("取消")
            }
        }
    )
} 