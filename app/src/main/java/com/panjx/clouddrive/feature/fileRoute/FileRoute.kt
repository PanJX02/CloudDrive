package com.panjx.clouddrive.feature.fileRoute

import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.panjx.clouddrive.core.design.component.FileTopBar
import com.panjx.clouddrive.core.design.theme.MyAppTheme
import com.panjx.clouddrive.core.modle.File
import com.panjx.clouddrive.feature.file.component.ItemFile
import com.panjx.clouddrive.feature.fileRoute.component.FileInfoDialog
import com.panjx.clouddrive.util.FileUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FileRoute(
    viewModel: FileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentPath by viewModel.currentPath.collectAsState()
    
    when (uiState) {
        is FileUiState.Loading -> {
            // 显示带有TopBar的加载界面
            FileScreen(
                files = emptyList(),
                viewModel = viewModel,
                isListLoading = true
            )
        }
        is FileUiState.Error -> {
            // 错误界面包装到FileScreen中以显示TopBar
            FileScreen(
                files = emptyList(),
                viewModel = viewModel,
                isListLoading = false,
                errorContent = {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = (uiState as FileUiState.Error).message)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadData() }) {
                            Text("重试")
                        }
                    }
                }
            )
        }
        is FileUiState.Success -> {
            FileScreen(
                files = (uiState as FileUiState.Success).files,
                viewModel = viewModel,
                isListLoading = false
            )
        }
        is FileUiState.ListLoading -> {
            FileScreen(
                files = emptyList(),
                viewModel = viewModel,
                isListLoading = true
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileScreen(
    toSearch: () -> Unit={},
    files: List<File> = listOf(),
    viewModel: FileViewModel = viewModel(),
    isListLoading: Boolean = false,
    errorContent: @Composable (() -> Unit)? = null
) {
    var showSearchBar by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val fileList = remember(files) { mutableStateListOf(*files.toTypedArray()) }
    val selectedFiles = remember { mutableStateListOf<Long>() }
    val currentPath by viewModel.currentPath.collectAsState()
    val currentDirId by viewModel.currentDirId.collectAsState()
    
    // 文件信息对话框状态
    var showFileInfoDialog by remember { mutableStateOf(false) }
    var selectedFileInfo by remember { mutableStateOf(mapOf<String, Any>()) }
    var fileHashes by remember { mutableStateOf<Map<String, Pair<String, Long>>>(emptyMap()) }
    var isCalculatingHashes by remember { mutableStateOf(false) }
    
    // 获取Context
    val context = LocalContext.current
    
    // 协程作用域
    val coroutineScope = rememberCoroutineScope()
    
    // 文件选择器
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // 获取文件基本信息（不含哈希值）
            val fileInfo = FileUtils.getFileInfoFromUri(context, it)
            selectedFileInfo = fileInfo
            showFileInfoDialog = true
            
            // 重置哈希值
            fileHashes = emptyMap()
            
            // 异步计算哈希值
            isCalculatingHashes = true
            coroutineScope.launch {
                try {
                    val hashes = FileUtils.calculateFileHashesAsync(context, it)
                    fileHashes = hashes
                } catch (e: Exception) {
                    Log.e("FileRoute", "计算哈希值异常: ${e.message}")
                } finally {
                    isCalculatingHashes = false
                }
            }
        }
    }

    // 处理返回键事件
    BackHandler(enabled = currentPath.size > 1) {
        viewModel.navigateUp()
    }

    fun handleSelectChange(fileId: Long, isSelected: Boolean) {
        if (isSelected) {
            selectedFiles.add(fileId)
        } else {
            selectedFiles.remove(fileId)
        }
    }
    
    // 显示文件信息对话框
    if (showFileInfoDialog) {
        // 获取当前路径中最后一个文件夹的名称
        val currentFolder = currentPath.lastOrNull()
        
        FileInfoDialog(
            fileName = selectedFileInfo["name"] as? String ?: "未知文件",
            fileSize = selectedFileInfo["formattedSize"] as? String ?: "未知大小",
            fileSizeBytes = (selectedFileInfo["size"] as? Long)?.toString() ?: "",
            fileType = selectedFileInfo["mimeType"] as? String ?: "未知类型",
            extensionFileType = selectedFileInfo["extensionMimeType"] as? String ?: "未知类型",
            fileExtension = selectedFileInfo["extension"] as? String ?: "",
            uploadFolderId = currentDirId,
            uploadFolderName = currentFolder?.second ?: "",
            md5Hash = fileHashes["md5"]?.first ?: "",
            md5Time = fileHashes["md5"]?.second ?: 0,
            sha1Hash = fileHashes["sha1"]?.first ?: "",
            sha1Time = fileHashes["sha1"]?.second ?: 0,
            sha256Hash = fileHashes["sha256"]?.first ?: "",
            sha256Time = fileHashes["sha256"]?.second ?: 0,
            sha512Hash = fileHashes["sha512"]?.first ?: "",
            sha512Time = fileHashes["sha512"]?.second ?: 0,
            keccak256Hash = fileHashes["keccak256"]?.first ?: "",
            keccak256Time = fileHashes["keccak256"]?.second ?: 0,
            isCalculatingHashes = isCalculatingHashes,
            onDismiss = {
                showFileInfoDialog = false
                selectedFileInfo = mapOf()
                fileHashes = emptyMap()
            },
            onConfirm = {
                // 处理上传文件的逻辑
                showFileInfoDialog = false
                selectedFileInfo = mapOf()
                fileHashes = emptyMap()
            }
        )
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "文件操作",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                ListItem(
                    headlineContent = { Text("上传文件") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Upload,
                            contentDescription = "上传文件"
                        )
                    },
                    modifier = Modifier.clickable { 
                        // 关闭底部菜单
                        showBottomSheet = false
                        // 启动文件选择器
                        filePickerLauncher.launch("*/*")
                    }
                )
                
                ListItem(
                    headlineContent = { Text("新建文件夹") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = "新建文件夹"
                        )
                    },
                    modifier = Modifier.clickable { /* TODO: 处理新建文件夹 */ }
                )
                
                ListItem(
                    headlineContent = { Text("扫描文件") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Scanner,
                            contentDescription = "扫描文件"
                        )
                    },
                    modifier = Modifier.clickable { /* TODO: 处理扫描文件 */ }
                )
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                FileTopBar(
                    toSearch = {  },
                    showBackIcon = currentPath.size > 1,
                    onNavigateUp = { viewModel.navigateUp() }
                )
                
                // 面包屑导航，显示当前路径
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    val scrollState = rememberScrollState()
                    
                    // 当组件加载或路径变化时，自动滚动到最右侧
                    LaunchedEffect(currentPath) {
                        // 延迟一下再滚动，确保布局已完成
                        delay(100)
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "位置: ",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        currentPath.forEachIndexed { index, pathItem ->
                            Text(
                                text = pathItem.second,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (index == currentPath.size - 1) 
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.clickable(enabled = index != currentPath.size - 1) {
                                    // 点击路径导航到对应目录
                                    viewModel.loadDirectoryContent(pathItem.first)
                                }
                            )
                            if (index < currentPath.size - 1) {
                                Text(
                                    text = " > ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        // 添加右侧空白，确保最后一项可以滑动到最左边
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    // 左侧阴影 - 仅当滚动位置不在最左侧时显示
                    if (scrollState.value > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .width(24.dp)
                                .height(16.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }
                    
                    // 右侧阴影 - 仅当滚动位置不在最右侧时显示
                    if (scrollState.value < scrollState.maxValue) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .width(24.dp)
                                .height(16.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                                        )
                                    )
                                )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            val offsetX by animateFloatAsState(
                targetValue = if (selectedFiles.isEmpty()) 0f else 110f,
                animationSpec = tween(300),
                label = "fabOffset"
            )
            
            FloatingActionButton(
                onClick = { showBottomSheet = true },
                modifier = Modifier
                    .padding(bottom = 40.dp)
                    .offset(x = (-32).dp + offsetX.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加"
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        
        Box(modifier = Modifier.fillMaxSize()) {
            val isRefreshing by viewModel.isRefreshing.collectAsState()

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.loadData()},
                modifier = Modifier.fillMaxSize()
            ) {
                if (isListLoading) {
                    // 显示列表加载状态
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (errorContent != null) {
                    // 显示错误内容
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        errorContent()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(bottom = if (selectedFiles.isNotEmpty()) 40.dp else 0.dp)
                    ) {
                        if (fileList.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CreateNewFolder,
                                            contentDescription = "空文件夹",
                                            modifier = Modifier.size(60.dp),
                                            tint = MaterialTheme.colorScheme.outline
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "此文件夹为空",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "点击右下角的 + 按钮添加文件",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        } else {
                            items(fileList, key = { it.id }) { file ->
                                ItemFile(
                                    data = file,
                                    isSelected = selectedFiles.contains(file.id),
                                    onSelectChange = { isSelected ->
                                        handleSelectChange(file.id, isSelected)
                                    },
                                    onFolderClick = { folderId, folderName ->
                                        // 点击文件夹，加载文件夹内容
                                        viewModel.loadDirectoryContent(folderId, folderName)
                                    }
                                )
                            }
                        }
                    }
                }
            }


            AnimatedVisibility(
                visible = selectedFiles.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "已选中 ${selectedFiles.size} 个文件",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FileRoutePreview() {
    MyAppTheme {
        FileScreen()
    }
}
