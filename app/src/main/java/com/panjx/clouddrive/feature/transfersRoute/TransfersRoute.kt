package com.panjx.clouddrive.feature.transfersRoute

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.panjx.clouddrive.data.database.TransferEntity
import com.panjx.clouddrive.data.database.TransferType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.log10
import kotlin.math.pow

@Composable
fun TransfersRoute() {
    TransfersScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransfersScreen(
    viewModel: TransfersViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("上传", "下载")
    
    var uploadInProgressExpanded by remember { mutableStateOf(true) }
    var uploadCompletedExpanded by remember { mutableStateOf(true) }
    var downloadInProgressExpanded by remember { mutableStateOf(true) }
    var downloadCompletedExpanded by remember { mutableStateOf(true) }
    
    // 跟踪上一次的选项卡状态
    var prevSelectedTab by remember { mutableIntStateOf(selectedTab) }
    // 跟踪是否是手动展开/收起
    var isManualToggle by remember { mutableStateOf(false) }

    
    val inProgressUploadTasks by viewModel.inProgressUploadTasks.collectAsState()
    val completedUploadTasks by viewModel.completedUploadTasks.collectAsState()
    val inProgressDownloadTasks by viewModel.inProgressDownloadTasks.collectAsState()
    val completedDownloadTasks by viewModel.completedDownloadTasks.collectAsState()

    // 添加状态变量用于显示文件信息弹窗
    var showFileInfoDialog by remember { mutableStateOf(false) }
    var selectedTransfer by remember { mutableStateOf<TransferEntity?>(null) }
    
    // 获取当前上下文
    val context = LocalContext.current


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
            // 主标签页 (上传/下载)
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { 
                            // 记录是选项卡切换而不是手动展开/收起
                            isManualToggle = false
                            prevSelectedTab = selectedTab
                            selectedTab = index 
                        },
                        text = { Text(title) }
                    )
                }
            }

            // 传输列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                val inProgressTasks = if (selectedTab == 0) inProgressUploadTasks else inProgressDownloadTasks
                val completedTasks = if (selectedTab == 0) completedUploadTasks else completedDownloadTasks
                val currentInProgressExpanded = if (selectedTab == 0) uploadInProgressExpanded else downloadInProgressExpanded
                val currentCompletedExpanded = if (selectedTab == 0) uploadCompletedExpanded else downloadCompletedExpanded
                // 决定使用哪种动画
                val fadeAnimSpec = if (isManualToggle) {
                    tween<Float>(300)
                } else {
                    snap(0)
                }
                
                val sizeAnimSpec = if (isManualToggle) {
                    tween<IntSize>(300)
                } else {
                    snap(0)
                }
                
                if (inProgressTasks.isEmpty() && completedTasks.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("没有传输任务")
                        }
                    }
                } else {
                    // 显示进行中的任务
                    if (inProgressTasks.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "进行中",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                IconButton(onClick = { 
                                    // 设置为手动触发的状态切换
                                    isManualToggle = true
                                    if (selectedTab == 0) {
                                        uploadInProgressExpanded = !uploadInProgressExpanded
                                    } else {
                                        downloadInProgressExpanded = !downloadInProgressExpanded
                                    }
                                }) {
                                    Icon(
                                        if (currentInProgressExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (currentInProgressExpanded) "收起" else "展开"
                                    )
                                }
                            }
                        }
                        
                        item {
                            AnimatedVisibility(
                                visible = currentInProgressExpanded,
                                enter = fadeIn(animationSpec = fadeAnimSpec) + expandVertically(animationSpec = sizeAnimSpec),
                                exit = fadeOut(animationSpec = fadeAnimSpec) + shrinkVertically(animationSpec = sizeAnimSpec)
                            ) {
                                Column {
                                    inProgressTasks.forEach { task ->
                                        TransferTaskItem(
                                            task = task,
                                            onPauseResume = { viewModel.pauseOrResumeTransfer(task, context) },
                                            onCancel = { viewModel.cancelTransfer(task) },
                                            onItemClick = {
                                                selectedTransfer = task
                                                showFileInfoDialog = true
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }
                    
                    // 显示已完成的任务
                    if (completedTasks.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp, bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "已完成",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                IconButton(onClick = { 
                                    // 设置为手动触发的状态切换
                                    isManualToggle = true
                                    if (selectedTab == 0) {
                                        uploadCompletedExpanded = !uploadCompletedExpanded
                                    } else {
                                        downloadCompletedExpanded = !downloadCompletedExpanded
                                    }
                                }) {
                                    Icon(
                                        if (currentCompletedExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (currentCompletedExpanded) "收起" else "展开"
                                    )
                                }
                            }
                        }
                        
                        item {
                            AnimatedVisibility(
                                visible = currentCompletedExpanded,
                                enter = fadeIn(animationSpec = fadeAnimSpec) + expandVertically(animationSpec = sizeAnimSpec),
                                exit = fadeOut(animationSpec = fadeAnimSpec) + shrinkVertically(animationSpec = sizeAnimSpec)
                            ) {
                                Column {
                                    completedTasks.forEach { task ->
                                        TransferTaskItem(
                                            task = task,
                                            onPauseResume = { viewModel.pauseOrResumeTransfer(task, context) },
                                            onCancel = { viewModel.cancelTransfer(task) },
                                            onItemClick = {
                                                selectedTransfer = task
                                                showFileInfoDialog = true
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 处理文件信息弹窗
    if (showFileInfoDialog && selectedTransfer != null) {
        val transfer = selectedTransfer!!
        FileInfoDialog(
            transfer = transfer,
            onDismiss = { showFileInfoDialog = false },
            viewModel = viewModel
        )
    }
}

@Composable
fun TransferTaskItem(
    task: TransferEntity,
    onPauseResume: () -> Unit,
    onCancel: () -> Unit,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onItemClick() },
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    text = formatFileName(task.fileName, task.fileExtension),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when(task.status) {
                        TransferStatus.WAITING -> "等待中"
                        TransferStatus.IN_PROGRESS -> "传输中"
                        TransferStatus.PAUSED -> "已暂停"
                        TransferStatus.COMPLETED -> "已完成"
                        TransferStatus.FAILED -> "失败"
                        TransferStatus.CALCULATING_HASH -> "计算哈希中"
                        TransferStatus.HASH_CALCULATED -> "计算完成"
                        TransferStatus.UPLOAD_STORAGE_COMPLETED -> "等待确认"
                        TransferStatus.CANCELLING -> "取消中"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 只显示文件大小信息
                if (task.fileSize > 0) {
                    Text(
                        text = formatFileSize(task.fileSize),
                        style = MaterialTheme.typography.bodySmall
                    )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            LinearProgressIndicator(
                progress = { task.progress / 100f },
                modifier = Modifier.fillMaxWidth(),
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (task.status != TransferStatus.COMPLETED && 
                    task.status != TransferStatus.CALCULATING_HASH &&
                    task.status != TransferStatus.HASH_CALCULATED &&
                    task.status != TransferStatus.UPLOAD_STORAGE_COMPLETED &&
                    task.status != TransferStatus.CANCELLING) {
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
    val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
    return String.format("%.1f %s", size / 1024.0.pow(digitGroups.toDouble()), units[digitGroups])
}

// 格式化时间戳为可读形式
private fun formatTimestamp(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
    return format.format(date)
}

// 智能地拼接文件名和扩展名，避免重复
private fun formatFileName(fileName: String, extension: String?): String {
    if (extension.isNullOrEmpty()) {
        return fileName
    }
    
    // 检查文件名是否已经以该扩展名结尾
    if (fileName.lowercase().endsWith("." + extension.lowercase())) {
        return fileName
    }
    
    return "$fileName.$extension"
}

// enum class TransferStatus已移动到单独的TransferStatus.kt文件

// 添加文件信息弹窗组件
@Composable
fun FileInfoDialog(
    transfer: TransferEntity,
    onDismiss: () -> Unit,
    viewModel: TransfersViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("文件详细信息") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(vertical = 8.dp)
            ) {
                // 基本信息
                Text(
                    text = "基本信息",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "ID：${transfer.id}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "文件名：${formatFileName(transfer.fileName, transfer.fileExtension)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "大小：${formatFileSize(transfer.fileSize)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "进度：${transfer.progress}%",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "状态：${transfer.status}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "类型：${transfer.type}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "本地路径：${transfer.filePath}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                if (transfer.remoteUrl.isNotEmpty()) {
                    Text(
                        text = "远程URL：${transfer.remoteUrl}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Text(
                    text = "创建时间：${formatTimestamp(transfer.createdAt)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "更新时间：${formatTimestamp(transfer.updatedAt)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 文件元数据
                Text(
                    text = "文件元数据",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "用户ID：${transfer.userId ?: "未设置"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "文件ID：${transfer.fileId ?: "未设置"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "扩展名：${transfer.fileExtension ?: "无"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "文件类型：${transfer.fileCategory ?: "未知"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "父文件夹ID：${transfer.filePid ?: "根目录"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "文件夹类型：${transfer.folderType}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "删除标记：${transfer.deleteFlag}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "存储ID：${transfer.storageId ?: "未设置"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "文件封面：${transfer.fileCover ?: "无"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "引用计数：${transfer.referCount ?: "未设置"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "文件状态：${transfer.fileStatus ?: "未设置"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "转码状态：${transfer.transcodeStatus ?: "未设置"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 哈希值信息
                Text(
                    text = "哈希值信息",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                if (transfer.fileMD5 != null) {
                    Text(
                        text = "MD5：${transfer.fileMD5}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = "MD5：未计算",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                if (transfer.fileSHA1 != null) {
                    Text(
                        text = "SHA1：${transfer.fileSHA1}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = "SHA1：未计算",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                if (transfer.fileSHA256 != null) {
                    Text(
                        text = "SHA256：${transfer.fileSHA256}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = "SHA256：未计算",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 上传信息
                if (transfer.type == TransferType.UPLOAD) {
                    Text(
                        text = "上传信息",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // 显示更详细的域名信息
                    if (transfer.domain != null) {
                        Text(
                            text = "域名列表：",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        
                        // 如果域名是逗号分隔的列表，将其分割显示
                        transfer.domain.split(",").forEach { domainItem ->
                            Text(
                                text = "· $domainItem",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "域名：未设置",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // 显示上传令牌信息
                    if (transfer.uploadToken != null) {
                        Text(
                            text = "上传令牌：",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        
                        // 直接显示完整令牌
                        Text(
                            text = transfer.uploadToken,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = "上传令牌：未设置",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 添加自动上传选项
                if (transfer.status == TransferStatus.HASH_CALCULATED && transfer.type == TransferType.UPLOAD) {
                    Button(
                        onClick = {
                            // 使用自动上传流程
                            viewModel.setTransferStatusToWaitingAndRequestToken(transfer.id)
                            
                            // 获取上传令牌后继续下一步
                            viewModel.viewModelScope.launch {
                                // 稍等片刻，等待令牌获取完成
                                delay(2000)
                                
                                val updatedTask = viewModel.transferRepository.getTransferById(transfer.id)
                                if (updatedTask != null && updatedTask.status == TransferStatus.WAITING) {
                                    // 开始上传
                                    viewModel.startUploadFile(transfer.id, context)
                                }
                            }
                            onDismiss()
                        }
                    ) {
                        Text("自动上传")
                    }
                }


            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}
