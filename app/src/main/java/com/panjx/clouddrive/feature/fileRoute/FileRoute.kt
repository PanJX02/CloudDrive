package com.panjx.clouddrive.feature.fileRoute

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
                files = (uiState as FileUiState.Success).files
            )
        }
    }
}

@Composable
fun FileScreen(
    toSearch: () -> Unit={},
    files: List<File> = listOf(),
) {
    var showSearchBar by remember { mutableStateOf(false) } // 控制搜索栏显示
    // 使用可观察的mutableStateList
    val fileList = remember { mutableStateListOf(*files.toTypedArray()) }

    // 记录所有选中文件的ID
    val selectedFiles = remember { mutableStateListOf<String>() }

    // 优化选中处理逻辑
    fun handleSelectChange(fileId: String, isSelected: Boolean) {
        if (isSelected) {
            selectedFiles.add(fileId)
        } else {
            selectedFiles.remove(fileId)
        }
    }
    Scaffold(
        topBar = {
            FileTopBar(
                toSearch = {  },
                showBackIcon = showSearchBar
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = selectedFiles.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it }),   // 从底部滑入
                exit = slideOutVertically(targetOffsetY = { it })    // 向底部滑出
            ) {
                Text(
                    text = "已选中 ${selectedFiles.size} 个文件",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

    ) { innerPadding ->
        // 文件列表内容（示例）
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            items(fileList, key = { it.id }) { file ->
                ItemFile(
                    data = file,
                    isSelected = selectedFiles.contains(file.id), // 传递选中状态
                    onSelectChange = { isSelected ->
                        handleSelectChange(file.id, isSelected)
                    }
                )
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
