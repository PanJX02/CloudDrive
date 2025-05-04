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
import com.panjx.clouddrive.feature.fileRoute.FileViewModel
import com.panjx.clouddrive.feature.fileRoute.viewmodel.FileUiState

/**
 * 文件浏览器组件，组合面包屑导航和文件列表
 *
 * @param viewModel FileViewModel实例，提供数据和状态
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
    viewModel: FileViewModel,
    selectedFiles: List<Long>,
    onSelectChange: (fileId: Long, isSelected: Boolean) -> Unit,
    onNavigateToDirectory: (dirId: Long, dirName: String?) -> Unit,
    extraBottomSpace: Dp = 0.dp,
    isSelectionMode: Boolean = false,
    hideSelectionIcon: Boolean = false,
    foldersOnly: Boolean = false,
    modifier: Modifier = Modifier
) {
    // 从ViewModel获取状态
    val uiState by viewModel.uiState.collectAsState()
    val currentPath by viewModel.currentPath.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    
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
    
    // 下拉刷新包装
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { 
            // 设置状态为刷新中
            viewModel.loadData()
        },
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
                onRetry = { viewModel.loadData() },
                extraBottomSpace = extraBottomSpace,
                isRefreshing = isRefreshing,
                isSelectionMode = isSelectionMode,
                hideSelectionIcon = hideSelectionIcon,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
} 