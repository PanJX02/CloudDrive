package com.panjx.clouddrive.feature.favorites

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
            FavoriteItem("重要文档.docx", "文件", "2025-03-20", "2.5MB", "/根目录/文档/工作"),
            FavoriteItem("家庭照片", "文件夹", "2025-03-25", "1.2GB", "/根目录/图片"),
            FavoriteItem("会议记录.pdf", "文件", "2024-04-10", "1.8MB", "/根目录/文档/会议"),
            FavoriteItem("视频资料", "文件夹", "2025-04-18", "21.3MB", "/根目录")
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