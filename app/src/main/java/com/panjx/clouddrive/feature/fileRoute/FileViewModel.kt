package com.panjx.clouddrive.feature.fileRoute

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.panjx.clouddrive.core.modle.File
import com.panjx.clouddrive.feature.fileRoute.viewmodel.FileListViewModel
import com.panjx.clouddrive.feature.fileRoute.viewmodel.FileNavigationViewModel
import com.panjx.clouddrive.feature.fileRoute.viewmodel.FileOperationViewModel
import com.panjx.clouddrive.feature.fileRoute.viewmodel.FileUiState
import kotlinx.coroutines.flow.StateFlow

/**
 * FileViewModel作为协调器，管理文件页面的所有子ViewModel
 * 将具体功能委托给各个专门的ViewModel处理
 */
class FileViewModel(application: Application): AndroidViewModel(application) {

    // 各个功能子ViewModel
    private val navigationViewModel = FileNavigationViewModel(application)
    private val listViewModel = FileListViewModel(application)
    private val operationViewModel = FileOperationViewModel(application)
    
    // 从各个子ViewModel暴露必要的属性
    val uiState: StateFlow<FileUiState> = listViewModel.uiState
    val isRefreshing: StateFlow<Boolean> = listViewModel.isRefreshing
    val currentDirId: StateFlow<Long> = navigationViewModel.currentDirId
    val currentPath: StateFlow<List<Pair<Long, String>>> = navigationViewModel.currentPath
    val operationState = operationViewModel.operationState

    init {
        // 首次加载根目录内容
        loadDirectoryContent(0L)
    }

    /**
     * 根据ID列表获取选中的文件对象列表
     */
    fun getSelectedFiles(fileIds: List<Long>): List<File> {
        return listViewModel.getSelectedFiles(fileIds)
    }

    /**
     * 刷新当前目录内容
     */
    fun loadData() {
        listViewModel.refreshData(currentDirId.value)
    }
    
    /**
     * 加载特定目录的内容
     * 协调导航ViewModel和列表ViewModel
     */
    fun loadDirectoryContent(dirId: Long, dirName: String? = null, isRefresh: Boolean = false) {
        Log.d("FileViewModel", "loadDirectoryContent: 加载目录 $dirId 的内容")
        
        // 更新导航路径
        navigationViewModel.navigateToDirectory(dirId, dirName)
        
        // 加载目录内容
        if (isRefresh) {
            listViewModel.refreshData(dirId)
        } else {
            listViewModel.loadDirectoryContent(dirId)
        }
    }
    
    /**
     * 返回上一级目录
     */
    fun navigateUp() {
        if (navigationViewModel.navigateUp()) {
            // 加载新的当前目录内容
            listViewModel.loadDirectoryContent(currentDirId.value)
        }
    }
    
    /**
     * 复制文件到指定目录
     */
    fun copyFiles(fileIds: List<Long>, targetFolderId: Long, onComplete: (Boolean, String) -> Unit) {
        operationViewModel.copyFiles(fileIds, targetFolderId) { success, message ->
            // 如果操作成功且目标文件夹是当前文件夹，刷新列表
            if (success && targetFolderId == currentDirId.value) {
                listViewModel.loadDirectoryContent(currentDirId.value)
            }
            onComplete(success, message)
        }
    }
    
    /**
     * 移动文件到指定目录
     */
    fun moveFiles(fileIds: List<Long>, targetFolderId: Long, onComplete: (Boolean, String) -> Unit) {
        operationViewModel.moveFiles(fileIds, targetFolderId) { success, message ->
            // 移动成功后刷新当前列表，因为有文件被移走了
            if (success) {
                listViewModel.loadDirectoryContent(currentDirId.value)
            }
            onComplete(success, message)
        }
    }

    /**
     * 创建新文件夹
     */
    fun createFolder(folderName: String, onComplete: (Boolean, String) -> Unit) {
        operationViewModel.createFolder(folderName, currentDirId.value) { success, message ->
            // 创建成功后刷新当前列表
            if (success) {
                listViewModel.loadDirectoryContent(currentDirId.value)
            }
            onComplete(success, message)
        }
    }
    
    /**
     * 重置操作状态
     */
    fun resetOperationState() {
        operationViewModel.resetOperationState()
    }
}