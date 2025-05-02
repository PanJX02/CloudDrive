package com.panjx.clouddrive.feature.fileRoute.screen

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.panjx.clouddrive.core.modle.File
import com.panjx.clouddrive.feature.file.component.BreadcrumbNavigator
import com.panjx.clouddrive.feature.file.component.FileList
import com.panjx.clouddrive.feature.file.navigateToShareSaveFolderSelection
import com.panjx.clouddrive.feature.fileRoute.FileViewModel
import com.panjx.clouddrive.feature.fileRoute.ShareFileState
import com.panjx.clouddrive.feature.fileRoute.viewmodel.FileUiState

/**
 * 分享文件列表屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareFileListScreen(
    shareKey: String,
    shareCode: String,
    onDismiss: () -> Unit,
    viewModel: FileViewModel = hiltViewModel(),
    navController: NavController? = null
) {
    val context = LocalContext.current
    
    // 状态管理
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var files by remember { mutableStateOf<List<File>>(emptyList()) }
    
    // 文件选择状态
    val selectedFiles = remember { mutableStateListOf<Long>() }
    
    // 面包屑导航路径 - 初始只有根目录
    var currentPath by remember { mutableStateOf(listOf(Pair<Long?, String>(null, "分享内容"))) }
    
    // 当前目录ID，默认为根目录(null)
    var currentDirId by remember { mutableStateOf<Long?>(null) }
    
    // 清空选择函数
    val clearSelection = {
        selectedFiles.clear()
    }
    
    // 保存对话框状态
    var showSaveDialog by remember { mutableStateOf(false) }
    
    // 操作栏高度计算
    var actionBarHeightPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val extraBottomSpace = with(density) { actionBarHeightPx.toDp() }
    
    // 加载分享文件列表
    LaunchedEffect(shareKey, shareCode, currentDirId) {
        isLoading = true
        clearSelection()
        
        // 保存分享参数到共享状态
        ShareFileState.setShareParams(shareKey, shareCode)
        
        Log.d("ShareFileListScreen", "开始获取分享内容: shareKey=$shareKey, code=$shareCode, currentDirId=$currentDirId")
        
        // 调用API获取分享内容，传递当前目录ID
        viewModel.getShareFileList(shareKey, shareCode, currentDirId) { success, message, fileList ->
            isLoading = false
            if (success && fileList != null) {
                Log.d("ShareFileListScreen", "获取分享内容成功: ${fileList.size}个文件")
                
                // 直接使用服务器返回的结果，不需要再次过滤
                files = fileList
                error = null
            } else {
                Log.e("ShareFileListScreen", "获取分享内容失败: $message")
                files = emptyList()
                error = message
            }
        }
    }
    
    // 监听选中文件变化，更新共享状态
    LaunchedEffect(selectedFiles) {
        // 更新共享状态中选中的文件ID
        ShareFileState.updateFiles(selectedFiles.toList())
    }
    
    // 保存文件函数
    val saveFiles = { targetFolderId: Long ->
        if (selectedFiles.isNotEmpty()) {
            viewModel.saveShareFiles(
                fileIds = selectedFiles.toList(),
                targetFolderId = targetFolderId,
                shareKey = shareKey,
                shareCode = shareCode
            ) { success, message ->
                if (success) {
                    // 保存成功提示
                    Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
                    
                    // 清空选择
                    clearSelection()
                } else {
                    // 保存失败提示
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "请先选择要保存的文件", Toast.LENGTH_SHORT).show()
        }
    }
    
    // 文件夹导航处理
    val navigateToDirectory = { dirId: Long, dirName: String? ->
        if (dirName != null) {
            // 更新面包屑导航路径
            if (dirId != currentDirId) {
                // 检查是否已存在该目录在路径中
                val existingIndex = currentPath.indexOfFirst { it.first == dirId }
                if (existingIndex >= 0) {
                    // 已存在，截取到该位置
                    currentPath = currentPath.subList(0, existingIndex + 1)
                } else {
                    // 不存在，添加新目录到路径
                    currentPath = currentPath + Pair<Long?, String>(dirId, dirName)
                }
                
                // 更新当前目录ID
                currentDirId = dirId
                
                // 清空选择
                clearSelection()
            }
        }
    }
    
    // 清理资源
    DisposableEffect(Unit) {
        onDispose {
            // 可以在这里执行一些清理操作
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("分享内容") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 添加转存按钮
                    if (selectedFiles.isNotEmpty()) {
                        // 显示选中文件数量
                        Text(
                            text = "已选择${selectedFiles.size}项",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        
                        IconButton(
                            onClick = {
                                // 确保在导航前将选中的文件ID保存到共享状态
                                ShareFileState.updateFiles(selectedFiles.toList())
                                
                                // 如果没有选中文件，提示用户
                                if (selectedFiles.isEmpty()) {
                                    Toast.makeText(context, "请先选择要转存的文件", Toast.LENGTH_SHORT).show()
                                } else {
                                    // 导航到文件夹选择页面
                                    navController?.navigateToShareSaveFolderSelection(
                                        shareKey = shareKey,
                                        shareCode = shareCode
                                    )
                                }
                            }
                        ) {
                            Icon(Icons.Default.SaveAlt, contentDescription = "转存")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 面包屑导航
                BreadcrumbNavigator(
                    currentPath = currentPath.map { Pair(it.first ?: 0L, it.second) }, // 转换为非空类型用于UI显示
                    onNavigate = { dirId ->
                        // 找到对应的目录名称
                        val dirEntry = currentPath.find { it.first == dirId || (it.first == null && dirId == 0L) }
                        if (dirEntry != null) {
                            // 如果点击的是当前目录，不做操作
                            if (dirId != currentDirId) {
                                // 更新当前目录ID
                                currentDirId = if (dirId == 0L) null else dirId
                                
                                // 更新导航路径，截取到当前点击的位置
                                val index = currentPath.indexOf(dirEntry)
                                if (index >= 0) {
                                    currentPath = currentPath.subList(0, index + 1)
                                }
                                
                                // 清空选择
                                clearSelection()
                            }
                        }
                    }
                )
                
                // 内容区域
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isLoading -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    text = "正在获取分享内容...",
                                    modifier = Modifier.padding(top = 16.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        error != null -> {
                            Text(
                                text = "加载失败: $error",
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                            )
                        }
                        files.isEmpty() -> {
                            Text(
                                text = "此文件夹为空",
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                            )
                        }
                        else -> {
                            // 使用FileList组件显示文件列表
                            FileList(
                                uiState = FileUiState.Success(files),
                                files = files,
                                selectedFiles = selectedFiles,
                                onSelectChange = { fileId, isSelected ->
                                    if (isSelected) {
                                        selectedFiles.add(fileId)
                                    } else {
                                        selectedFiles.remove(fileId)
                                    }
                                },
                                onFolderClick = { dirId, dirName ->
                                    navigateToDirectory(dirId, dirName)
                                },
                                onRetry = { /* 暂不实现刷新功能 */ },
                                extraBottomSpace = extraBottomSpace,
                                isRefreshing = false
                            )
                        }
                    }
                }
            }
            
            // 文件操作栏
            AnimatedVisibility(
                visible = selectedFiles.isNotEmpty(),
                enter = fadeIn() + slideInVertically(initialOffsetY = { 100 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { 100 }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                // 自定义简化版操作栏，只有转存功能
                androidx.compose.material3.Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            actionBarHeightPx = coordinates.size.height
                        },
                    color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(
                            onClick = {
                                // 确保在导航前将选中的文件ID保存到共享状态
                                ShareFileState.updateFiles(selectedFiles.toList())
                                
                                // 如果没有选中文件，提示用户
                                if (selectedFiles.isEmpty()) {
                                    Toast.makeText(context, "请先选择要转存的文件", Toast.LENGTH_SHORT).show()
                                } else {
                                    // 导航到文件夹选择页面
                                    navController?.navigateToShareSaveFolderSelection(
                                        shareKey = shareKey,
                                        shareCode = shareCode
                                    )
                                }
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SaveAlt,
                                contentDescription = "转存",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Text(
                            text = "转存",
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
} 