package com.panjx.clouddrive.feature.fileRoute

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.panjx.clouddrive.feature.fileRoute.component.FileScreen
import com.panjx.clouddrive.feature.transfersRoute.DownloadTransfersViewModel

/**
 * 文件路由主入口
 */
@Composable
fun FileRoute(
    viewModel: FileViewModel = viewModel(),
    // 向调用者提供实现的操作回调
    onActionsReady: (FileActions) -> Unit,
    // 当路由被销毁时的回调
    onDispose: () -> Unit,
    extraBottomSpace: Dp = 0.dp
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

    // 在这里定义操作实现，访问viewModel和selectedFiles
    val fileActions = remember(selectedFiles.size) { // 当选择改变时重新创建操作
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
    
    // 当操作变化时提供给调用者
    LaunchedEffect(fileActions) {
        onActionsReady(fileActions)
    }

    // 当此组件被销毁时通知调用者
    DisposableEffect(Unit) {
        onDispose {
            onDispose()
        }
    }

    // 将selectedFiles传递给FileScreen
    FileScreen(
        uiState = uiState,
        viewModel = viewModel,
        transfersViewModel = hiltViewModel(),
        currentPath = currentPath,
        selectedFiles = selectedFiles, // 传递列表
        onSelectChange = { fileId, isSelected -> // 在此处理选择变化
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
