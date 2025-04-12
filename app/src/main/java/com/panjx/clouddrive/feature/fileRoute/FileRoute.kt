package com.panjx.clouddrive.feature.fileRoute

import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.panjx.clouddrive.core.design.component.FileTopBar
import com.panjx.clouddrive.core.design.theme.MyAppTheme
import com.panjx.clouddrive.core.modle.File
import com.panjx.clouddrive.feature.file.component.ItemFile
import com.panjx.clouddrive.feature.transfersRoute.TransfersViewModel
import com.panjx.clouddrive.util.FileUtils
import kotlinx.coroutines.delay
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
    val hasSelection: Boolean = false // Indicates if there are selected items
)

@Composable
fun FileRoute(
    viewModel: FileViewModel = viewModel(),
    // Callback to provide the implemented actions to the caller
    onActionsReady: (FileActions) -> Unit,
    // Callback when this route is disposed
    onDispose: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentPath by viewModel.currentPath.collectAsState()
    val selectedFiles = remember { mutableStateListOf<Long>() }

    // Define action implementations here, accessing viewModel and selectedFiles
    val fileActions = remember(selectedFiles.size) { // Re-create actions when selection changes
        val hasSelection = selectedFiles.isNotEmpty()
        FileActions(
            onDownloadClick = { Log.d("FileRoute", "Download clicked: ${selectedFiles.toList()}") /* TODO: viewModel.download(selectedFiles) */ },
            onMoveClick = { Log.d("FileRoute", "Move clicked: ${selectedFiles.toList()}") /* TODO: viewModel.move(selectedFiles) */ },
            onCopyClick = { Log.d("FileRoute", "Copy clicked: ${selectedFiles.toList()}") /* TODO: viewModel.copy(selectedFiles) */ },
            onFavoriteClick = { Log.d("FileRoute", "Favorite clicked: ${selectedFiles.toList()}") /* TODO: viewModel.favorite(selectedFiles) */ },
            onRenameClick = { Log.d("FileRoute", "Rename clicked: ${selectedFiles.toList()}") /* TODO: viewModel.rename(selectedFiles) */ },
            onDeleteClick = { Log.d("FileRoute", "Delete clicked: ${selectedFiles.toList()}") /* TODO: viewModel.delete(selectedFiles) */ },
            onShareClick = { Log.d("FileRoute", "Share clicked: ${selectedFiles.toList()}") /* TODO: viewModel.share(selectedFiles) */ },
            onDetailsClick = { Log.d("FileRoute", "Details clicked: ${selectedFiles.toList()}") /* TODO: viewModel.details(selectedFiles) */ },
            hasSelection = hasSelection
        )
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
        }
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

    // Back handler
    BackHandler(enabled = currentPath.size > 1) {
        viewModel.navigateUp()
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
                    modifier = Modifier.clickable { /* TODO */ }
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
                if (isListLoading && files.isEmpty()) { // Show loading only if list is truly empty during load
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (uiState is FileUiState.Error) {
                     Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                         Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = uiState.message)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadData() }) { Text("重试") }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(innerPadding) // No extra bottom padding needed now
                    ) {
                        if (files.isEmpty() && !isListLoading) {
                             item { 
                                Box(modifier = Modifier.fillMaxWidth().padding(top = 100.dp), contentAlignment = Alignment.Center) {
                                     Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.CreateNewFolder, "空文件夹", modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.outline)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text("此文件夹为空", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("点击右下角的 + 按钮添加文件", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                            } 
                        } else {
                            items(files, key = { it.id!! }) { file ->
                                ItemFile(
                                    data = file,
                                    isSelected = selectedFiles.contains(file.id),
                                    onSelectChange = { isSelected -> // Use the passed handler
                                        file.id?.let { onSelectChange(it, isSelected) }
                                    },
                                    onFolderClick = { folderId, folderName ->
                                        viewModel.loadDirectoryContent(folderId, folderName)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
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
            onSelectChange = { _, _ -> }
        )
    }
}
