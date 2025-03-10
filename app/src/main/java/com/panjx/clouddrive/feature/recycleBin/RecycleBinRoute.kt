package com.panjx.clouddrive.feature.recycleBin

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

data class RecycleBinItem(
    val name: String,
    val type: String,
    val deleteTime: String,
    val size: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecycleBinRoute(
    onNavigateBack: () -> Unit
) {
    var showEmptyDialog by remember { mutableStateOf(false) }
    val items = remember {
        listOf(
            RecycleBinItem("文档.docx", "文件", "2024-03-20", "2.5MB"),
            RecycleBinItem("照片.jpg", "图片", "2024-03-19", "1.8MB"),
            RecycleBinItem("项目文件夹", "文件夹", "2024-03-18", "156MB")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("回收站") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showEmptyDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "清空回收站")
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
                Text("回收站是空的")
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
                                Text("${item.type} · ${item.size} · ${item.deleteTime}")
                            },
                            trailingContent = {
                                Row {
                                    IconButton(onClick = { /* 恢复文件 */ }) {
                                        Icon(Icons.Default.Restore, contentDescription = "恢复")
                                    }
                                    IconButton(onClick = { /* 永久删除 */ }) {
                                        Icon(Icons.Default.DeleteForever, contentDescription = "删除")
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showEmptyDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyDialog = false },
            title = { Text("清空回收站") },
            text = { Text("确定要清空回收站吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(onClick = { showEmptyDialog = false }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
} 