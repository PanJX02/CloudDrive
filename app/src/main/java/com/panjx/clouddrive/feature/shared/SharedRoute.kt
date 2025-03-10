package com.panjx.clouddrive.feature.shared

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class SharedItem(
    val name: String,
    val type: String,
    val shareTime: String,
    val views: Int,
    val downloads: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedRoute(
    onNavigateBack: () -> Unit
) {
    val items = remember {
        listOf(
            SharedItem("项目文档.pdf", "文件", "2024-03-20", 128, 45),
            SharedItem("设计图集", "文件夹", "2024-03-19", 256, 89),
            SharedItem("演示视频.mp4", "视频", "2024-03-18", 512, 167)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的分享") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* 新建分享 */ }) {
                Icon(Icons.Default.Share, contentDescription = "新建分享")
            }
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无分享内容")
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
                                    Text("${item.type} · 分享于 ${item.shareTime}")
                                    Row(
                                        modifier = Modifier.padding(top = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Visibility,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            " ${item.views} 次浏览",
                                            modifier = Modifier.padding(end = 16.dp)
                                        )
                                        Icon(
                                            Icons.Default.Download,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(" ${item.downloads} 次下载")
                                    }
                                }
                            },
                            trailingContent = {
                                Row {
                                    IconButton(onClick = { /* 复制链接 */ }) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "复制链接")
                                    }
                                    IconButton(onClick = { /* 取消分享 */ }) {
                                        Icon(Icons.Default.Close, contentDescription = "取消分享")
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