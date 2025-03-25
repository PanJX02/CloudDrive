package com.panjx.clouddrive.feature.settings

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsRoute(
    userPreferences: UserPreferences,
    onLogout: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val currentEndpoint = userPreferences.endpoint.collectAsState(initial = UserPreferences.DEFAULT_ENDPOINT)
    var showEndpointDialog by remember { mutableStateOf(false) }
    var showRestartDialog by remember { mutableStateOf(false) }
    var selectedEndpoint by remember { mutableStateOf("") }
    
    // 获取当前服务器的友好名称
    val currentEndpointName = UserPreferences.getEndpointName(currentEndpoint.value)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ListItem(
                headlineContent = { Text("服务器地址") },
                supportingContent = { Text(currentEndpointName) },
                leadingContent = { 
                    Icon(Icons.Default.Cloud, contentDescription = "服务器地址")
                },
                trailingContent = { 
                    Icon(Icons.Default.ChevronRight, contentDescription = "选择")
                },
                modifier = Modifier.clickable { showEndpointDialog = true }
            )
            
            ListItem(
                headlineContent = { Text("清理缓存") },
                supportingContent = { Text("清除应用缓存数据") },
                leadingContent = { 
                    Icon(Icons.Default.DeleteOutline, contentDescription = "清理缓存")
                },
                trailingContent = { 
                    Icon(Icons.Default.ChevronRight, contentDescription = "进入")
                },
                modifier = Modifier.clickable { /* 清理缓存逻辑 */ }
            )
            
            ListItem(
                headlineContent = { Text("关于") },
                supportingContent = { Text("查看应用信息") },
                leadingContent = { 
                    Icon(Icons.Default.Info, contentDescription = "关于")
                },
                trailingContent = { 
                    Icon(Icons.Default.ChevronRight, contentDescription = "进入")
                },
                modifier = Modifier.clickable { onNavigateToAbout() }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    scope.launch {
                        userPreferences.clearLoginState()
                        onLogout()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("退出登录")
            }
        }
        
        if (showEndpointDialog) {
            AlertDialog(
                onDismissRequest = { showEndpointDialog = false },
                title = { Text("选择服务器地址") },
                text = {
                    Column {
                        UserPreferences.ENDPOINT_OPTIONS.forEach { (name, url) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (url != currentEndpoint.value) {
                                            // 立即保存选择的端点
                                            scope.launch {
                                                userPreferences.setEndpoint(url)
                                                Config.updateEndpoint(url)
                                            }
                                            selectedEndpoint = url
                                            showEndpointDialog = false
                                            showRestartDialog = true
                                        } else {
                                            showEndpointDialog = false
                                        }
                                    }
                                    .padding(vertical = 12.dp)
                            ) {
                                RadioButton(
                                    selected = url == currentEndpoint.value,
                                    onClick = {
                                        if (url != currentEndpoint.value) {
                                            // 立即保存选择的端点
                                            scope.launch {
                                                userPreferences.setEndpoint(url)
                                                Config.updateEndpoint(url)
                                            }
                                            selectedEndpoint = url
                                            showEndpointDialog = false
                                            showRestartDialog = true
                                        } else {
                                            showEndpointDialog = false
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(name)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showEndpointDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }
        
        if (showRestartDialog) {
            AlertDialog(
                onDismissRequest = { showRestartDialog = false },
                title = { Text("更换服务器地址") },
                text = { Text("服务器地址已更改，重启应用后生效。是否立即重启？") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // 直接重启应用，不需要再保存端点
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
                        onClick = { showRestartDialog = false }
                    ) {
                        Text("稍后手动重启")
                    }
                }
            )
        }
    }
} 