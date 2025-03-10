package com.panjx.clouddrive.feature.transfersRoute

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TransfersRoute() {
    TransfersScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransfersScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("上传", "下载")
    
    val uploadTasks = remember { mutableStateListOf<TransferTask>() }
    val downloadTasks = remember { mutableStateListOf<TransferTask>() }

    // 模拟一些传输任务
    LaunchedEffect(Unit) {
        uploadTasks.addAll(
            listOf(
                TransferTask("文档1.doc", 75, TransferStatus.IN_PROGRESS),
                TransferTask("图片1.jpg", 100, TransferStatus.COMPLETED),
                TransferTask("视频1.mp4", 30, TransferStatus.PAUSED)
            )
        )
        downloadTasks.addAll(
            listOf(
                TransferTask("文档2.doc", 50, TransferStatus.IN_PROGRESS),
                TransferTask("图片2.jpg", 0, TransferStatus.WAITING),
                TransferTask("视频2.mp4", 100, TransferStatus.COMPLETED)
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("传输列表") },
                actions = {
                    IconButton(onClick = { /* 清除已完成 */ }) {
                        Icon(Icons.Default.Clear, "清除已完成")
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
            // 标签页
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // 传输列表
            val currentTasks = if (selectedTab == 0) uploadTasks else downloadTasks
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(currentTasks) { task ->
                    TransferTaskItem(task)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun TransferTaskItem(task: TransferTask) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.fileName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = when(task.status) {
                        TransferStatus.WAITING -> "等待中"
                        TransferStatus.IN_PROGRESS -> "传输中"
                        TransferStatus.PAUSED -> "已暂停"
                        TransferStatus.COMPLETED -> "已完成"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { task.progress / 100f },
                modifier = Modifier.fillMaxWidth(),
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (task.status != TransferStatus.COMPLETED) {
                    IconButton(onClick = { /* 暂停/继续 */ }) {
                        Icon(
                            if (task.status == TransferStatus.PAUSED) 
                                Icons.Default.PlayArrow 
                            else 
                                Icons.Default.Pause,
                            contentDescription = "暂停/继续"
                        )
                    }
                }
                IconButton(onClick = { /* 取消 */ }) {
                    Icon(Icons.Default.Close, "取消")
                }
            }
        }
    }
}

data class TransferTask(
    val fileName: String,
    val progress: Int,
    val status: TransferStatus
)

enum class TransferStatus {
    WAITING,
    IN_PROGRESS,
    PAUSED,
    COMPLETED
}
