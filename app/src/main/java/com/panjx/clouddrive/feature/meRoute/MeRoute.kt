package com.panjx.clouddrive.feature.meRoute

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.panjx.clouddrive.data.UserPreferences
import kotlinx.coroutines.launch

@Composable
fun MeRoute(
    userPreferences: UserPreferences,
    onLogout: () -> Unit
) {
    MeScreen(userPreferences, onLogout)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeScreen(
    userPreferences: UserPreferences,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("个人中心") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // 用户信息卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "用户头像",
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "用户名",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "已使用空间：2.5GB / 10GB",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // 功能列表
            ListItem(
                headlineContent = { Text("设置") },
                leadingContent = { 
                    Icon(Icons.Default.Settings, contentDescription = "设置")
                },
                trailingContent = { 
                    Icon(Icons.Default.ChevronRight, contentDescription = "进入")
                }
            )
            
            ListItem(
                headlineContent = { Text("清理缓存") },
                leadingContent = { 
                    Icon(Icons.Default.DeleteOutline, contentDescription = "清理缓存")
                },
                trailingContent = { 
                    Icon(Icons.Default.ChevronRight, contentDescription = "进入")
                }
            )
            
            ListItem(
                headlineContent = { Text("关于") },
                leadingContent = { 
                    Icon(Icons.Default.Info, contentDescription = "关于")
                },
                trailingContent = { 
                    Icon(Icons.Default.ChevronRight, contentDescription = "进入")
                }
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
                    .padding(vertical = 16.dp)
            ) {
                Text("退出登录")
            }
        }
    }
}
