package com.panjx.clouddrive.feature.file.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.panjx.clouddrive.core.modle.File
import com.panjx.clouddrive.feature.favorites.FavoritesViewModel
import com.panjx.clouddrive.feature.fileRoute.FileViewModel
import com.panjx.clouddrive.feature.fileRoute.viewmodel.FileUiState

/**
 * 文件浏览器组件，组合面包屑导航和文件列表
 * 
 * 支持FileViewModel和兼容的ViewModel(如FavoritesViewModel)
 *
 * @param viewModel ViewModel实例，提供数据和状态
 * @param selectedFiles 已选中的文件ID列表
 * @param onSelectChange 文件选中状态变更回调
 * @param onNavigateToDirectory 导航到指定目录的回调
 * @param extraBottomSpace 底部额外空间，用于操作栏或FAB
 * @param isSelectionMode 是否为选择模式
 * @param hideSelectionIcon 是否隐藏选择图标
 * @param foldersOnly 是否只显示文件夹
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileExplorer(
    viewModel: Any, // 使用Any类型，支持FileViewModel和FavoritesViewModel
    selectedFiles: List<Long>,
    onSelectChange: (fileId: Long, isSelected: Boolean) -> Unit,
    onNavigateToDirectory: (dirId: Long, dirName: String?) -> Unit,
    extraBottomSpace: Dp = 0.dp,
    isSelectionMode: Boolean = false,
    hideSelectionIcon: Boolean = false,
    foldersOnly: Boolean = false,
    modifier: Modifier = Modifier
) {
    // 从ViewModel获取状态，根据类型选择适当的处理方式
    val uiState by when (viewModel) {
        is FileViewModel -> viewModel.uiState.collectAsState()
        is FavoritesViewModel -> viewModel.uiState.collectAsState()
        else -> throw IllegalArgumentException("不支持的ViewModel类型: ${viewModel.javaClass.name}")
    }
    
    val currentPath by when (viewModel) {
        is FileViewModel -> viewModel.currentPath.collectAsState()
        is FavoritesViewModel -> viewModel.currentPath.collectAsState()
        else -> throw IllegalArgumentException("不支持的ViewModel类型: ${viewModel.javaClass.name}")
    }
    
    val isRefreshing by when (viewModel) {
        is FileViewModel -> viewModel.isRefreshing.collectAsState()
        is FavoritesViewModel -> viewModel.isRefreshing.collectAsState()
        else -> throw IllegalArgumentException("不支持的ViewModel类型: ${viewModel.javaClass.name}")
    }
    
    // 根据UI状态获取文件列表
    val allFiles = when (uiState) {
        is FileUiState.Success -> (uiState as FileUiState.Success).files
        else -> emptyList()
    }
    
    // 如果foldersOnly为true，则过滤出文件夹
    val files = if (foldersOnly) {
        allFiles.filter { it.folderType == 1 }
    } else {
        allFiles
    }

    // 定义刷新回调函数
    val onRefresh: () -> Unit = {
        when (viewModel) {
            is FileViewModel -> viewModel.loadData()
            is FavoritesViewModel -> viewModel.loadData()
            else -> throw IllegalArgumentException("不支持的ViewModel类型: ${viewModel.javaClass.name}")
        }
    }
    
    // 下拉刷新包装
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 面包屑导航
            BreadcrumbNavigator(
                currentPath = currentPath,
                onNavigate = { dirId -> 
                    // 点击面包屑时导航
                    onNavigateToDirectory(dirId, null)
                }
            )
            
            // 文件列表
            FileList(
                uiState = uiState,
                files = files,
                selectedFiles = selectedFiles,
                onSelectChange = onSelectChange,
                onFolderClick = { dirId, dirName ->
                    // 点击文件夹时导航
                    onNavigateToDirectory(dirId, dirName)
                },
                onRetry = onRefresh,
                extraBottomSpace = extraBottomSpace,
                isRefreshing = isRefreshing,
                isSelectionMode = isSelectionMode,
                hideSelectionIcon = hideSelectionIcon,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/**
 * 简化版的文件浏览器组件，用于收藏夹等不需要文件选择的场景
 *
 * @param files 文件列表
 * @param onFileClick 文件点击回调
 * @param currentPath 当前路径
 * @param onPathClick 路径点击回调
 */
@Composable
fun FileExplorer(
    files: List<File>,
    onFileClick: (File) -> Unit,
    currentPath: List<Pair<Long, String>>,
    onPathClick: (dirId: Long, dirName: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // 面包屑导航
        BreadcrumbNavigator(
            currentPath = currentPath,
            onNavigate = { dirId -> 
                // 获取对应的目录名称
                val dirName = currentPath.find { it.first == dirId }?.second
                onPathClick(dirId, dirName)
            }
        )
        
        // 文件列表
        FileList(
            uiState = FileUiState.Success(files),
            files = files,
            selectedFiles = emptyList(),
            onSelectChange = { _, _ -> },
            onFolderClick = { dirId, dirName ->
                // 查找对应的文件对象
                val file = files.find { it.id == dirId }
                if (file != null) {
                    // 先调用onFileClick，让调用者处理文件点击逻辑
                    onFileClick(file)
                }
                // 然后调用onPathClick处理导航逻辑
                onPathClick(dirId, dirName)
            },
            onRetry = { },
            hideSelectionIcon = true,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
} 