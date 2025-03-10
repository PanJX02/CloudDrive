package com.panjx.clouddrive.feature.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class FavoriteItem(
    val name: String,
    val type: String,
    val favoriteTime: String,
    val size: String,
    val location: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesRoute(
    onNavigateBack: () -> Unit
) {
    val items = remember {
        listOf(
            FavoriteItem("重要文档.docx", "文件", "2024-03-20", "2.5MB", "文档/工作"),
            FavoriteItem("家庭照片", "文件夹", "2024-03-19", "1.2GB", "图片"),
            FavoriteItem("会议记录.pdf", "文件", "2024-03-18", "1.8MB", "文档/会议")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("收藏夹") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("收藏夹是空的")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                items(items) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        ListItem(
                            headlineContent = { Text(item.name) },
                            supportingContent = {
                                Column {
                                    Text("${item.type} · ${item.size} · 收藏于 ${item.favoriteTime}")
                                    Row(
                                        modifier = Modifier.padding(top = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Folder,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(" ${item.location}")
                                    }
                                }
                            },
                            trailingContent = {
                                Row {
                                    IconButton(onClick = { /* 打开文件 */ }) {
                                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "打开")
                                    }
                                    IconButton(onClick = { /* 取消收藏 */ }) {
                                        Icon(Icons.Default.Star, contentDescription = "取消收藏")
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
} 