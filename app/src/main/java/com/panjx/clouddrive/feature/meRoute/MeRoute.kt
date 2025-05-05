package com.panjx.clouddrive.feature.meRoute

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.panjx.clouddrive.core.util.formatFileSize
import com.panjx.clouddrive.feature.profile.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeRoute(
    onNavigateToSettings: () -> Unit,
    onNavigateToShared: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToRecycleBin: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAnnouncements: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    MeScreen(
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToShared = onNavigateToShared,
        onNavigateToFavorites = onNavigateToFavorites,
        onNavigateToRecycleBin = onNavigateToRecycleBin,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToAnnouncements = onNavigateToAnnouncements,
        viewModel = viewModel
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
    onNavigateToAnnouncements: () -> Unit,
    viewModel: ProfileViewModel
) {
    val userState by viewModel.userState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("个人中心") },
                actions = {
                    // 添加刷新按钮
                    IconButton(onClick = { viewModel.loadUserInfo() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                }
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
                if (userState.isInitializing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
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
                                text = userState.userInfo?.nickname ?: userState.userInfo?.username ?: "未知用户",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "已使用空间：${formatFileSize(userState.userInfo?.usedSpace ?: 0)} / ${formatFileSize(userState.userInfo?.totalSpace ?: 0)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
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
