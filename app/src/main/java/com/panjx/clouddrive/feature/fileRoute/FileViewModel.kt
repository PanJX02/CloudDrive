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
        
        // 检查目录是否被标记为需要刷新
        val needsRefresh = isRefresh || operationViewModel.checkAndClearRefreshFlag(dirId)
        
        // 加载目录内容
        if (needsRefresh) {
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
                // 使用下拉刷新而不是直接加载
                listViewModel.refreshData(currentDirId.value)
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
                // 使用下拉刷新而不是直接加载
                listViewModel.refreshData(currentDirId.value)
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
                // 使用下拉刷新而不是直接加载
                listViewModel.refreshData(currentDirId.value)
            }
            onComplete(success, message)
        }
    }
    
    /**
     * 删除文件
     */
    fun deleteFiles(fileIds: List<Long>, onComplete: (Boolean, String) -> Unit) {
        operationViewModel.deleteFiles(fileIds) { success, message ->
            // 删除成功后刷新当前列表
            if (success) {
                // 使用下拉刷新而不是直接加载
                listViewModel.refreshData(currentDirId.value)
            }
            onComplete(success, message)
        }
    }
    
    /**
     * 重命名文件
     */
    fun renameFile(fileId: Long, newName: String, onComplete: (Boolean, String) -> Unit) {
        operationViewModel.renameFile(fileId, newName) { success, message ->
            // 重命名成功后刷新当前列表
            if (success) {
                // 使用下拉刷新而不是直接加载
                listViewModel.refreshData(currentDirId.value)
            }
            onComplete(success, message)
        }
    }
    
    /**
     * 添加文件到收藏
     */
    fun addToFavorites(fileIds: List<Long>, onComplete: (Boolean, String) -> Unit) {
        operationViewModel.addToFavorites(fileIds) { success, message ->
            // 收藏成功后刷新当前列表
            if (success) {
                // 使用下拉刷新而不是直接加载
                listViewModel.refreshData(currentDirId.value)
            }
            onComplete(success, message)
        }
    }
    
    /**
     * 从收藏中移除文件
     */
    fun removeFromFavorites(fileIds: List<Long>, onComplete: (Boolean, String) -> Unit) {
        operationViewModel.removeFromFavorites(fileIds) { success, message ->
            // 取消收藏成功后刷新当前列表
            if (success) {
                // 使用下拉刷新而不是直接加载
                listViewModel.refreshData(currentDirId.value)
            }
            onComplete(success, message)
        }
    }
    
    /**
     * 获取文件详情
     */
    fun getFileDetails(fileIds: List<Long>, onComplete: (Boolean, String, com.panjx.clouddrive.core.modle.FileDetail?) -> Unit) {
        operationViewModel.getFileDetails(fileIds, onComplete)
    }
    
    /**
     * 分享文件
     */
    fun shareFile(fileIds: List<Long>, validType: Int, onComplete: (Boolean, String, com.panjx.clouddrive.core.modle.response.ShareResponse?) -> Unit) {
        operationViewModel.shareFile(fileIds, validType, onComplete)
    }
    
    /**
     * 重置操作状态
     */
    fun resetOperationState() {
        operationViewModel.resetOperationState()
    }
    
    /**
     * 标记文件夹需要刷新
     */
    fun markFolderForRefresh(folderId: Long) {
        operationViewModel.markFolderForRefresh(folderId)
    }

    /**
     * 获取分享文件列表
     */
    fun getShareFileList(shareKey: String, code: String, folderId: Long? = null, onComplete: (Boolean, String, List<File>?) -> Unit) {
        operationViewModel.getShareFileList(shareKey, code, folderId) { success, message, data ->
            if (success && data != null) {
                // 提取文件列表
                val fileList = data.list ?: emptyList()
                onComplete(true, "获取分享内容成功", fileList)
            } else {
                onComplete(false, message, null)
            }
        }
    }
    
    /**
     * 保存分享文件到指定目录
     */
    fun saveShareFiles(fileIds: List<Long>, targetFolderId: Long, shareKey: String, shareCode: String, onComplete: (Boolean, String) -> Unit) {
        operationViewModel.saveShareFiles(fileIds, targetFolderId, shareKey, shareCode) { success, message ->
            // 如果保存成功且目标文件夹是当前文件夹，刷新列表
            if (success) {
                // 使用下拉刷新而不是直接加载
                if (targetFolderId == currentDirId.value) {
                    // 在当前浏览的目录，直接刷新
                    listViewModel.refreshData(currentDirId.value)
                } else {
                    // 不在当前浏览的目录，标记该目录需要刷新
                    // 当用户导航到这个目录时会刷新
                    operationViewModel.markFolderForRefresh(targetFolderId)
                }
            }
            onComplete(success, message)
        }
    }

    /**
     * 根据关键词搜索文件
     */
    fun searchFiles(keyword: String, onComplete: (List<File>) -> Unit) {
        if (keyword.isBlank()) {
            onComplete(emptyList())
            return
        }
        
        Log.d("FileViewModel", "searchFiles: 搜索关键词 $keyword")
        
        operationViewModel.searchFiles(keyword) { success, message, files ->
            if (success && files != null) {
                Log.d("FileViewModel", "searchFiles: 搜索成功，找到 ${files.size} 个文件")
                onComplete(files)
            } else {
                Log.e("FileViewModel", "searchFiles: 搜索失败 $message")
                onComplete(emptyList())
            }
        }
    }
}