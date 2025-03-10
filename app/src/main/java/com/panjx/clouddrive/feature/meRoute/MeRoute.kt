package com.panjx.clouddrive.feature.meRoute

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeRoute(
    onNavigateToSettings: () -> Unit,
    onNavigateToShared: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToRecycleBin: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAnnouncements: () -> Unit
) {
    MeScreen(
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToShared = onNavigateToShared,
        onNavigateToFavorites = onNavigateToFavorites,
        onNavigateToRecycleBin = onNavigateToRecycleBin,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToAnnouncements = onNavigateToAnnouncements
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToShared: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToRecycleBin: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAnnouncements: () -> Unit
) {
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
                    .clickable { onNavigateToProfile() }
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
                headlineContent = { Text("我的分享") },
                leadingContent = { 
                    Icon(Icons.Default.Share, contentDescription = "分享")
                },
                trailingContent = { 
                    Icon(Icons.Default.ChevronRight, contentDescription = "进入")
                },
                modifier = Modifier.clickable { onNavigateToShared() }
            )
            
            ListItem(
                headlineContent = { Text("收藏夹") },
                leadingContent = { 
                    Icon(Icons.Default.Favorite, contentDescription = "收藏")
                },
                trailingContent = { 
                    Icon(Icons.Default.ChevronRight, contentDescription = "进入")
                },
                modifier = Modifier.clickable { onNavigateToFavorites() }
            )
            
            ListItem(
                headlineContent = { Text("回收站") },
                leadingContent = { 
                    Icon(Icons.Default.Delete, contentDescription = "回收站")
                },
                trailingContent = { 
                    Icon(Icons.Default.ChevronRight, contentDescription = "进入")
                },
                modifier = Modifier.clickable { onNavigateToRecycleBin() }
            )
            
            ListItem(
                headlineContent = { Text("公告中心") },
                leadingContent = { 
                    Icon(Icons.Default.Notifications, contentDescription = "公告")
                },
                trailingContent = { 
                    Icon(Icons.Default.ChevronRight, contentDescription = "进入")
                },
                modifier = Modifier.clickable { onNavigateToAnnouncements() }
            )
            
            ListItem(
                headlineContent = { Text("设置") },
                leadingContent = { 
                    Icon(Icons.Default.Settings, contentDescription = "设置")
                },
                trailingContent = { 
                    Icon(Icons.Default.ChevronRight, contentDescription = "进入")
                },
                modifier = Modifier.clickable { onNavigateToSettings() }
            )
        }
    }
}
