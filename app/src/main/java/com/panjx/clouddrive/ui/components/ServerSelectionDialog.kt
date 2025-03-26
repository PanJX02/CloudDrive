package com.panjx.clouddrive.ui.components

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.panjx.clouddrive.MainActivity
import com.panjx.clouddrive.core.config.Config
import com.panjx.clouddrive.data.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * 服务器选择弹窗
 * 当服务器连接出现问题时显示，允许用户选择切换到其他服务器
 */
@Composable
fun ServerSelectionDialog(
    userPreferences: UserPreferences,
    currentServer: String,
    errorType: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showRestartDialog by remember { mutableStateOf(false) }
    var selectedEndpoint by remember { mutableStateOf("") }
    
    // 获取当前端点
    val currentEndpoint = runBlocking { userPreferences.endpoint.first() }
    
    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = { Text("更换服务器地址") },
            text = { Text("服务器地址已更改，重启应用后生效。是否立即重启？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // 直接重启应用
                        val intent = Intent(context, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        context.startActivity(intent)
                        (context as? Activity)?.finish()
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showRestartDialog = false
                        onDismiss()
                    }
                ) {
                    Text("稍后手动重启")
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("服务器连接问题") },
            text = { 
                Column {
                    Text("当前服务器 \"${UserPreferences.getEndpointName(currentEndpoint)}\" 连接出现问题: $errorType")
                    Text("请选择切换到其他服务器或继续使用当前服务器:", modifier = Modifier.padding(top = 8.dp))
                    
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        UserPreferences.ENDPOINT_OPTIONS.forEach { (name, url) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (url != currentEndpoint) {
                                            // 保存选择的端点
                                            scope.launch {
                                                userPreferences.setEndpoint(url)
                                                Config.updateEndpoint(url)
                                            }
                                            selectedEndpoint = url
                                            showRestartDialog = true
                                        } else {
                                            onDismiss()
                                        }
                                    }
                                    .padding(vertical = 12.dp)
                            ) {
                                RadioButton(
                                    selected = url == currentEndpoint,
                                    onClick = {
                                        if (url != currentEndpoint) {
                                            // 保存选择的端点
                                            scope.launch {
                                                userPreferences.setEndpoint(url)
                                                Config.updateEndpoint(url)
                                            }
                                            selectedEndpoint = url
                                            showRestartDialog = true
                                        } else {
                                            onDismiss()
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(name)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("继续使用当前服务器")
                }
            }
        )
    }
} 