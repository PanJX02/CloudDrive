package com.panjx.clouddrive.feature.fileRoute

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.panjx.clouddrive.core.design.component.FileTopBar
import com.panjx.clouddrive.core.design.theme.MyAppTheme
import com.panjx.clouddrive.core.modle.File
import com.panjx.clouddrive.feature.file.component.ItemFile

@Composable
fun FileRoute(
    viewModel: FileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    when (uiState) {
        is FileUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is FileUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
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
        }
        is FileUiState.Success -> {
            FileScreen(
                files = (uiState as FileUiState.Success).files,
                viewModel = viewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileScreen(
    toSearch: () -> Unit={},
    files: List<File> = listOf(),
    viewModel: FileViewModel = viewModel()
) {
    var showSearchBar by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val fileList = remember(files) { mutableStateListOf(*files.toTypedArray()) }
    val selectedFiles = remember { mutableStateListOf<String>() }
    val currentPath by viewModel.currentPath.collectAsState()
    val currentDirId by viewModel.currentDirId.collectAsState()

    // 处理返回键事件
    BackHandler(enabled = currentPath.size > 1) {
        viewModel.navigateUp()
    }

    fun handleSelectChange(fileId: String, isSelected: Boolean) {
        if (isSelected) {
            selectedFiles.add(fileId)
        } else {
            selectedFiles.remove(fileId)
        }
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
                    modifier = Modifier.clickable { /* TODO: 处理上传文件 */ }
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
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
                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(bottom = if (selectedFiles.isNotEmpty()) 40.dp else 0.dp)
                ) {
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
