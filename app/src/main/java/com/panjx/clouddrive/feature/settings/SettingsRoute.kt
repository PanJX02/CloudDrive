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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.panjx.clouddrive.MainActivity
import com.panjx.clouddrive.data.UserPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsRoute(
    onLogout: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // 从ViewModel获取状态
    val currentEndpoint by viewModel.currentEndpoint.collectAsState()
    val showEndpointDialog by viewModel.showEndpointDialog.collectAsState()
    val showRestartDialog by viewModel.showRestartDialog.collectAsState()
    val selectedEndpoint by viewModel.selectedEndpoint.collectAsState()
    
    // 获取当前服务器的友好名称
    val currentEndpointName = UserPreferences.getEndpointName(currentEndpoint)
    
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
                modifier = Modifier.clickable { viewModel.setShowEndpointDialog(true) }
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
                modifier = Modifier.clickable { viewModel.clearCache() }
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
                    viewModel.logout()
                    onLogout()
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
                onDismissRequest = { viewModel.setShowEndpointDialog(false) },
                title = { Text("选择服务器地址") },
                text = {
                    Column {
                        UserPreferences.ENDPOINT_OPTIONS.forEach { (name, url) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.changeEndpoint(url)
                                    }
                                    .padding(vertical = 12.dp)
                            ) {
                                RadioButton(
                                    selected = url == currentEndpoint,
                                    onClick = {
                                        viewModel.changeEndpoint(url)
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(name)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.setShowEndpointDialog(false) }) {
                        Text("取消")
                    }
                }
            )
        }
        
        if (showRestartDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.setShowRestartDialog(false) },
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
                        onClick = { viewModel.setShowRestartDialog(false) }
                    ) {
                        Text("稍后手动重启")
                    }
                }
            )
        }
    }
} 