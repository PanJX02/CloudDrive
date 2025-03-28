package com.panjx.clouddrive.feature.transfersRoute

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.panjx.clouddrive.data.database.TransferEntity

@Composable
fun TransfersRoute() {
    TransfersScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransfersScreen(
    viewModel: TransfersViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("上传", "下载")
    
    val uploadTasks by viewModel.uploadTasks.collectAsState()
    val downloadTasks by viewModel.downloadTasks.collectAsState()

    // 首次加载时添加测试数据
    LaunchedEffect(Unit) {
        if (uploadTasks.isEmpty() && downloadTasks.isEmpty()) {
            viewModel.addSampleData()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("传输列表") },
                actions = {
                    IconButton(onClick = { viewModel.clearCompletedTransfers() }) {
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
            
            if (currentTasks.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("没有传输任务")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(currentTasks) { task ->
                        TransferTaskItem(
                            task = task,
                            onPauseResume = { viewModel.pauseOrResumeTransfer(task) },
                            onCancel = { viewModel.cancelTransfer(task) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TransferTaskItem(
    task: TransferEntity,
    onPauseResume: () -> Unit,
    onCancel: () -> Unit
) {
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
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 文件大小信息
            if (task.fileSize > 0) {
                Text(
                    text = formatFileSize(task.fileSize),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            
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
                    IconButton(onClick = onPauseResume) {
                        Icon(
                            if (task.status == TransferStatus.PAUSED) 
                                Icons.Default.PlayArrow 
                            else 
                                Icons.Default.Pause,
                            contentDescription = "暂停/继续"
                        )
                    }
                }
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, "取消")
                }
            }
        }
    }
}

// 格式化文件大小
private fun formatFileSize(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format("%.1f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}

enum class TransferStatus {
    WAITING,
    IN_PROGRESS,
    PAUSED,
    COMPLETED
}
