package com.panjx.clouddrive.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    }
} 