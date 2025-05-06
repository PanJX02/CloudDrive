package com.panjx.clouddrive.feature.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.panjx.clouddrive.core.util.formatTimestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileRoute(
    onNavigateBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val userState by viewModel.userState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("个人中心") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 添加刷新按钮
                    IconButton(onClick = { viewModel.loadUserInfo() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                    IconButton(onClick = { onNavigateToEditProfile() }) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 显示加载状态
            if (userState.isInitializing) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp)
                )
            } else if (userState.error != null && userState.userInfo == null) {
                // 显示错误信息，但仅当没有用户信息时才显示
                Text(
                    text = "加载失败: ${userState.error}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                // 当有用户信息时显示，即使正在重新加载或有错误
                
                // 头像
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "用户头像",
                    modifier = Modifier
                        .size(120.dp)
                        .padding(vertical = 24.dp)
                )
                
                // 用户名
                Text(
                    text = userState.userInfo?.nickname ?: userState.userInfo?.username ?: "未知用户",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 邮箱
                Text(
                    text = userState.userInfo?.email ?: "未设置邮箱",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // 存储空间使用情况
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "存储空间",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val spaceProgress = userState.userInfo?.let {
                            if (it.totalSpace > 0) it.usedSpace.toFloat() / it.totalSpace.toFloat() else 0f
                        } ?: 0f
                        
                        LinearProgressIndicator(
                            progress = { spaceProgress },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "已使用 ${formatFileSize(userState.userInfo?.usedSpace ?: 0)} / ${formatFileSize(userState.userInfo?.totalSpace ?: 0)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 账号信息
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "账号信息",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ListItem(
                            headlineContent = { Text("注册时间") },
                            supportingContent = { 
                                Text(formatTimestamp(userState.userInfo?.registerTime, "未知")) 
                            }
                        )
                        ListItem(
                            headlineContent = { Text("最后登录") },
                            supportingContent = { 
                                Text(formatTimestamp(userState.userInfo?.lastLoginTime, "未知")) 
                            }
                        )
                    }
                }
            }
        }
    }
} 