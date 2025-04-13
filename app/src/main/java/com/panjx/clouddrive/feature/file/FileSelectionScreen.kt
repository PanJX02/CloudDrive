package com.panjx.clouddrive.feature.file

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.panjx.clouddrive.feature.file.component.FileExplorer
import com.panjx.clouddrive.feature.fileRoute.FileViewModel

/**
 * 文件选择页面，用于在需要选择文件的场景下使用
 * 例如：文件复制、移动等操作
 *
 * @param title 页面标题
 * @param onBackClick 返回按钮点击回调
 * @param onFilesSelected 文件选择完成回调，参数为选中的文件ID列表
 * @param showConfirmButton 是否显示确认按钮
 * @param allowMultiSelect 是否允许多选
 * @param initialDirectoryId 初始目录ID，默认为根目录(0)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileSelectionScreen(
    title: String = "选择文件",
    onBackClick: () -> Unit,
    onFilesSelected: (List<Long>) -> Unit,
    showConfirmButton: Boolean = true,
    allowMultiSelect: Boolean = true,
    initialDirectoryId: Long = 0L
) {
    // 使用ViewModel
    val viewModel: FileViewModel = hiltViewModel()
    
    // 选中的文件列表
    val selectedFiles = remember { mutableStateListOf<Long>() }
    
    // 当前目录路径
    val currentPath by viewModel.currentPath.collectAsState()
    
    // 初始化加载指定目录
    if (initialDirectoryId != 0L && currentPath.size == 1) {
        viewModel.loadDirectoryContent(initialDirectoryId)
    }
    
    // 处理目录导航
    val handleNavigateToDirectory = { dirId: Long, dirName: String? ->
        // 导航到新目录
        viewModel.loadDirectoryContent(dirId, dirName)
    }
    
    // 处理文件选择变更
    val handleSelectChange = { fileId: Long, isSelected: Boolean ->
        if (isSelected) {
            // 如果不允许多选，先清空已选
            if (!allowMultiSelect) {
                selectedFiles.clear()
            }
            selectedFiles.add(fileId)
        } else {
            selectedFiles.remove(fileId)
        }
        Unit  // 显式指定返回类型为Unit
    }
    
    // 处理硬件返回键和手势返回
    BackHandler(enabled = true) {
        if (selectedFiles.isNotEmpty()) {
            // 如果有选中的文件，先清空选择
            selectedFiles.clear()
        } else if (currentPath.size > 1) {
            // 如果在子目录中，返回上级目录
            viewModel.navigateUp()
        } else {
            // 如果在根目录且没有选择文件，退出页面
            onBackClick()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (showConfirmButton && selectedFiles.isNotEmpty()) {
                        IconButton(onClick = { onFilesSelected(selectedFiles.toList()) }) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "确认选择")
                        }
                    }
                }
            )
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
                onSelectChange = handleSelectChange,
                onNavigateToDirectory = handleNavigateToDirectory,
                extraBottomSpace = 0.dp
            )
        }
    }
} 