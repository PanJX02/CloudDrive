package com.panjx.clouddrive.feature.file

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.panjx.clouddrive.feature.file.component.FileExplorer
import com.panjx.clouddrive.feature.fileRoute.FileViewModel
import kotlinx.coroutines.launch

/**
 * 文件夹选择页面，用于在需要选择目标文件夹的场景下使用
 * 例如：文件移动、复制等操作指定目标位置
 *
 * @param title 页面标题
 * @param onBackClick 返回按钮点击回调
 * @param onFolderSelected 文件夹选择完成回调，参数为选中的文件夹ID
 * @param excludeFolderIds 需要排除的文件夹ID列表（例如不能选择当前所在文件夹作为目标）
 * @param initialDirectoryId 初始目录ID，默认为根目录(0)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderSelectionScreen(
    title: String = "选择文件夹",
    onBackClick: () -> Unit,
    onFolderSelected: (Long) -> Unit,
    excludeFolderIds: List<Long> = emptyList(),
    initialDirectoryId: Long = 0L
) {
    // 使用ViewModel
    val viewModel: FileViewModel = hiltViewModel()
    
    // 当前目录路径和ID
    val currentPath by viewModel.currentPath.collectAsState()
    val currentDirId by viewModel.currentDirId.collectAsState()
    
    // 是否可以选择当前文件夹
    val canSelectCurrentFolder = currentDirId == 0L || !excludeFolderIds.contains(currentDirId)
    
    // 选中的文件列表（此页面不需要，但FileExplorer需要）
    val selectedFiles = remember { emptyList<Long>() }
    
    // 新建文件夹对话框状态
    var showNewFolderDialog by remember { mutableStateOf(false) }
    // 新文件夹名称
    var newFolderName by remember { mutableStateOf("") }
    // Snackbar状态
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // 初始化加载指定目录
    LaunchedEffect(initialDirectoryId) {
        if (initialDirectoryId != 0L && currentPath.size == 1) {
            viewModel.loadDirectoryContent(initialDirectoryId)
        }
    }
    
    // 处理目录导航
    val handleNavigateToDirectory = { dirId: Long, dirName: String? ->
        // 导航到新目录
        viewModel.loadDirectoryContent(dirId, dirName)
    }
    
    // 处理硬件返回键和手势返回
    BackHandler(enabled = true) {
        if (currentPath.size > 1) {
            // 如果在子目录中，返回上级目录
            viewModel.navigateUp()
        } else {
            // 如果在根目录，退出页面
            onBackClick()
        }
    }
    
    // 显示创建文件夹对话框
    if (showNewFolderDialog) {
        AlertDialog(
            onDismissRequest = { 
                showNewFolderDialog = false 
                newFolderName = ""
            },
            title = { Text("新建文件夹") },
            text = {
                Column {
                    Text("请输入文件夹名称：")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            // 创建文件夹
                            viewModel.createFolder(newFolderName) { success, message ->
                                // 显示结果消息
                                scope.launch {
                                    snackbarHostState.showSnackbar(message)
                                }
                            }
                        }
                        showNewFolderDialog = false
                        newFolderName = ""
                    }
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                Button(
                    onClick = { 
                        showNewFolderDialog = false 
                        newFolderName = ""
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 添加新建文件夹按钮
                    IconButton(onClick = { showNewFolderDialog = true }) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = "新建文件夹")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (canSelectCurrentFolder) {
                FloatingActionButton(
                    onClick = { onFolderSelected(currentDirId) }
                ) {
                    Icon(Icons.Default.Check, contentDescription = "选择当前文件夹")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 文件浏览器组件
            FileExplorer(
                viewModel = viewModel,
                selectedFiles = selectedFiles,
                onSelectChange = { _, _ -> /* 文件夹选择页面不需要选择文件 */ },
                onNavigateToDirectory = handleNavigateToDirectory,
                extraBottomSpace = 0.dp
            )
        }
    }
} 