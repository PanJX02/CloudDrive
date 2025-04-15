package com.panjx.clouddrive.feature.fileRoute

import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.panjx.clouddrive.core.design.component.FileTopBar
import com.panjx.clouddrive.core.design.theme.MyAppTheme
import com.panjx.clouddrive.core.modle.File
import com.panjx.clouddrive.feature.file.component.FileExplorer
import com.panjx.clouddrive.feature.transfersRoute.DownloadTransfersViewModel
import com.panjx.clouddrive.feature.transfersRoute.TransfersViewModel
import com.panjx.clouddrive.util.FileUtils
import kotlinx.coroutines.launch

// Data class to hold action callbacks
data class FileActions(
    val onDownloadClick: () -> Unit = {},
    val onMoveClick: () -> Unit = {},
    val onCopyClick: () -> Unit = {},
    val onFavoriteClick: () -> Unit = {},
    val onRenameClick: () -> Unit = {},
    val onDeleteClick: () -> Unit = {},
    val onShareClick: () -> Unit = {},
    val onDetailsClick: () -> Unit = {},
    val hasSelection: Boolean = false, // Indicates if there are selected items
    val selectedFileIds: List<Long> = emptyList() // 选中的文件ID列表
)

@Composable
fun FileRoute(
    viewModel: FileViewModel = viewModel(),
    // Callback to provide the implemented actions to the caller
    onActionsReady: (FileActions) -> Unit,
    // Callback when this route is disposed
    onDispose: () -> Unit,
    extraBottomSpace: Dp = 0.dp // 重命名参数为 extraBottomSpace
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentPath by viewModel.currentPath.collectAsState()
    val selectedFiles = remember { mutableStateListOf<Long>() }
    val downloadViewModel: DownloadTransfersViewModel = hiltViewModel()
    val context = LocalContext.current

    // 创建函数来清空选中文件
    val clearSelection = {
        if (selectedFiles.isNotEmpty()) {
            selectedFiles.clear()
        }
    }

    // Define action implementations here, accessing viewModel and selectedFiles
    val fileActions = remember(selectedFiles.size) { // Re-create actions when selection changes
        val hasSelection = selectedFiles.isNotEmpty()
        FileActions(
            onDownloadClick = { 
                Log.d("FileRoute", "================== 下载流程开始 ==================")
                Log.d("FileRoute", "用户点击下载按钮，选中文件数量: ${selectedFiles.size}")
                Log.d("FileRoute", "选中的文件ID: ${selectedFiles.toList()}")
                
                // 获取选中的文件对象
                val filesToDownload = viewModel.getSelectedFiles(selectedFiles.toList())
                Log.d("FileRoute", "获取到的文件对象数量: ${filesToDownload.size}")
                filesToDownload.forEachIndexed { index, file ->
                    Log.d("FileRoute", "文件[$index]: id=${file.id}, 名称=${file.fileName}, 类型=${file.folderType}")
                }
                
                // 调用下载ViewModel进行下载
                Log.d("FileRoute", "调用DownloadTransfersViewModel.addDownloadTasks开始下载...")
                downloadViewModel.addDownloadTasks(filesToDownload, context)
                
                // 清空选中
                clearSelection()
                Log.d("FileRoute", "已清空选中状态")
            },
            onMoveClick = { Log.d("FileRoute", "Move clicked: ${selectedFiles.toList()}") /* TODO: viewModel.move(selectedFiles) */ },
            onCopyClick = { Log.d("FileRoute", "Copy clicked: ${selectedFiles.toList()}") /* TODO: viewModel.copy(selectedFiles) */ },
            onFavoriteClick = { Log.d("FileRoute", "Favorite clicked: ${selectedFiles.toList()}") /* TODO: viewModel.favorite(selectedFiles) */ },
            onRenameClick = { Log.d("FileRoute", "Rename clicked: ${selectedFiles.toList()}") /* TODO: viewModel.rename(selectedFiles) */ },
            onDeleteClick = { Log.d("FileRoute", "Delete clicked: ${selectedFiles.toList()}") /* TODO: viewModel.delete(selectedFiles) */ },
            onShareClick = { Log.d("FileRoute", "Share clicked: ${selectedFiles.toList()}") /* TODO: viewModel.share(selectedFiles) */ },
            onDetailsClick = { Log.d("FileRoute", "Details clicked: ${selectedFiles.toList()}") /* TODO: viewModel.details(selectedFiles) */ },
            hasSelection = hasSelection,
            selectedFileIds = selectedFiles.toList() // 存储选中的文件ID列表
        )
    }

    // 处理目录导航
    val handleNavigateToDirectory = { dirId: Long, dirName: String? ->
        // 在导航前清空已选文件
        clearSelection()
        // 导航到新目录
        viewModel.loadDirectoryContent(dirId, dirName)
    }
    
    // Provide the actions to the caller whenever they change
    LaunchedEffect(fileActions) {
        onActionsReady(fileActions)
    }

    // Notify caller when this Composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            onDispose()
        }
    }

    // Pass selectedFiles down to FileScreen
    FileScreen(
        uiState = uiState,
        viewModel = viewModel,
        transfersViewModel = hiltViewModel(),
        currentPath = currentPath,
        selectedFiles = selectedFiles, // Pass the list down
        onSelectChange = { fileId, isSelected -> // Handle selection changes here
            if (isSelected) {
                selectedFiles.add(fileId)
            } else {
                selectedFiles.remove(fileId)
            }
        },
        onNavigateToDirectory = handleNavigateToDirectory, // 传递导航处理函数
        clearSelection = clearSelection, // 传递清空选择函数
        extraBottomSpace = extraBottomSpace // 传递额外底部空间
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileScreen(
    uiState: FileUiState,
    viewModel: FileViewModel,
    transfersViewModel: TransfersViewModel,
    currentPath: List<Pair<Long, String>>,
    selectedFiles: List<Long>, // Receive selected files list
    onSelectChange: (fileId: Long, isSelected: Boolean) -> Unit, // Receive selection change handler
    onNavigateToDirectory: (dirId: Long, dirName: String?) -> Unit, // 添加导航回调
    clearSelection: () -> Unit, // 添加清空选择回调
    extraBottomSpace: Dp = 0.dp, // 重命名参数为 extraBottomSpace
    toSearch: () -> Unit={},
    errorContent: @Composable (() -> Unit)? = null // Keep errorContent for flexibility if needed
) {
    // Determine files and loading state from uiState
    val (files, isListLoading) = when (uiState) {
        is FileUiState.Success -> uiState.files to false
        is FileUiState.ListLoading -> emptyList<File>() to true
        is FileUiState.Loading -> emptyList<File>() to true // Consider full screen loading state
        is FileUiState.Error -> emptyList<File>() to false // Error state implies loading finished (with error)
    }

    var showBottomSheet by remember { mutableStateOf(false) }
    // 添加新文件夹对话框状态
    var showNewFolderDialog by remember { mutableStateOf(false) }
    // 新文件夹名称
    var newFolderName by remember { mutableStateOf("") }
    
    val currentDirId by viewModel.currentDirId.collectAsState() // Needed for upload

    // Coroutine scope & Context
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                try {
                    val contentResolver = context.contentResolver
                    val takeFlags: Int = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    contentResolver.takePersistableUriPermission(it, takeFlags)
                    Log.d("FileRoute", "成功获取持久化URI权限: $it")
                } catch (e: SecurityException) {
                    Log.e("FileRoute", "无法获取持久化URI权限: $it", e)
                    return@let
                }
                val fileInfo = FileUtils.getFileInfoFromUri(context, it)
                val fullFileName = fileInfo["name"] as? String ?: "未知文件"
                val extension = fileInfo["extension"] as? String ?: ""
                val fileName = if (extension.isNotEmpty() && fullFileName.endsWith(".$extension", ignoreCase = true)) {
                    fullFileName.substring(0, fullFileName.length - extension.length - 1)
                } else {
                    fullFileName
                }
                coroutineScope.launch {
                    transfersViewModel.autoUploadProcess(
                        uri = it,
                        fileName = fileName,
                        fileSize = fileInfo["size"] as? Long ?: 0L,
                        fileExtension = extension,
                        fileCategory = fileInfo["mimeType"] as? String ?: "",
                        filePid = currentDirId,
                        context = context
                    )
                }
            }
        }
    )

    // Back handler - 修改返回上一级的逻辑，先清空选择
    BackHandler(enabled = currentPath.size > 1 || selectedFiles.isNotEmpty()) {
        if (selectedFiles.isNotEmpty()) {
            clearSelection()
        } else {
            viewModel.navigateUp()
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
                                // 可以在这里显示结果消息，但现在先简单处理
                                if (success) {
                                    Log.d("FileRoute", "文件夹创建成功: $message")
                                } else {
                                    Log.e("FileRoute", "文件夹创建失败: $message")
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

    // Bottom sheet content
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
                    leadingContent = { Icon(Icons.Default.Upload, "上传文件") },
                    modifier = Modifier.clickable { 
                        showBottomSheet = false
                        filePickerLauncher.launch(arrayOf("*/*"))
                    }
                )
                ListItem(
                    headlineContent = { Text("新建文件夹") },
                    leadingContent = { Icon(Icons.Default.CreateNewFolder, "新建文件夹") },
                    modifier = Modifier.clickable { 
                        showBottomSheet = false
                        showNewFolderDialog = true
                    }
                )
                ListItem(
                    headlineContent = { Text("扫描文件") },
                    leadingContent = { Icon(Icons.Default.Scanner, "扫描文件") },
                    modifier = Modifier.clickable { /* TODO */ }
                )
            }
        }
    }

    Scaffold(
        topBar = {
            FileTopBar(
                toSearch = toSearch,
                showBackIcon = currentPath.size > 1,
                onNavigateUp = { 
                    // 点击返回按钮时先清空选择，如果没有选择才返回上一级
                    if (selectedFiles.isNotEmpty()) {
                        clearSelection()
                    } else {
                        viewModel.navigateUp() 
                    }
                }
            )
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
        // 使用新的FileExplorer组件
        FileExplorer(
            viewModel = viewModel,
            selectedFiles = selectedFiles,
            onSelectChange = onSelectChange,
            onNavigateToDirectory = onNavigateToDirectory,
            extraBottomSpace = extraBottomSpace,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FileRoutePreview() {
    MyAppTheme {
        // Preview needs adjustment as FileScreen signature changed
        FileScreen(
            uiState = FileUiState.Success(listOf()), // Example state
            viewModel = viewModel(), // Use a preview ViewModel or mock
            transfersViewModel = hiltViewModel(),
            currentPath = listOf(0L to "根目录"),
            selectedFiles = listOf(),
            onSelectChange = { _, _ -> },
            onNavigateToDirectory = { _, _ -> },
            clearSelection = {},
            extraBottomSpace = 90.dp // 添加默认的 extraBottomSpace 值
        )
    }
}
