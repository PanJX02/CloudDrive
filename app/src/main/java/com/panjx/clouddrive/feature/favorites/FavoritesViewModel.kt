package com.panjx.clouddrive.feature.favorites

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.panjx.clouddrive.core.modle.File
import com.panjx.clouddrive.core.modle.FileDetail
import com.panjx.clouddrive.core.modle.response.ShareResponse
import com.panjx.clouddrive.core.network.datasource.MyRetrofitDatasource
import com.panjx.clouddrive.data.UserPreferences
import com.panjx.clouddrive.feature.fileRoute.viewmodel.FileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 收藏夹ViewModel，处理数据加载和导航逻辑
 * 重构后更贴近FileViewModel的设计模式
 */
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    private val TAG = "FavoritesViewModel"
    private val userPreferences = UserPreferences(application)
    private val dataSource = MyRetrofitDatasource(userPreferences)
    
    // 当前目录ID，0表示收藏根目录
    private val _currentDirId = MutableStateFlow(0L)
    val currentDirId: StateFlow<Long> = _currentDirId
    
    // 当前路径，用于展示面包屑导航
    private val _currentPath = MutableStateFlow<List<Pair<Long, String>>>(listOf(Pair(0L, "收藏夹")))
    val currentPath: StateFlow<List<Pair<Long, String>>> = _currentPath
    
    // UI状态，使用与FileViewModel兼容的FileUiState
    private val _uiState = MutableStateFlow<FileUiState>(FileUiState.Loading)
    val uiState: StateFlow<FileUiState> = _uiState
    
    // 下拉刷新状态
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing
    
    // 选中的文件ID列表 - 用于多选
    private val _selectedFiles = MutableStateFlow<List<Long>>(emptyList())
    val selectedFiles: StateFlow<List<Long>> = _selectedFiles
    
    // 是否处于选择模式
    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode
    
    init {
        // 初始加载
        loadFiles(0L)
    }
    
    /**
     * 加载文件函数
     */
    fun loadFiles(dirId: Long) {
        Log.d(TAG, "开始加载目录ID: $dirId 的文件")
        _uiState.value = FileUiState.Loading
        
        viewModelScope.launch {
            try {
                val response = if (dirId == 0L) {
                    // 如果是收藏根目录，使用getFavoriteFiles
                    Log.d(TAG, "使用getFavoriteFiles()获取收藏根目录文件")
                    dataSource.getFavoriteFiles()
                } else {
                    // 否则使用getFilesByFolderId
                    Log.d(TAG, "使用getFilesByFolderId($dirId)获取子文件夹内容")
                    dataSource.getFilesByFolderId(dirId.toString())
                }
                
                if (response.code == 1 && response.data != null) {
                    val files = response.data.list ?: emptyList()
                    Log.d(TAG, "加载成功，获取到 ${files.size} 个文件")
                    _uiState.value = FileUiState.Success(files)
                } else {
                    Log.e(TAG, "加载失败: ${response.message}")
                    throw Exception(response.message ?: "请求失败")
                }
            } catch (e: Exception) {
                Log.e(TAG, "加载异常: ${e.message}", e)
                _uiState.value = FileUiState.Error(e.message ?: "未知错误")
            } finally {
                // 确保在任何情况下都重置刷新状态
                _isRefreshing.value = false
            }
        }
    }
    
    /**
     * 刷新当前数据
     */
    fun loadData() {
        // 直接设置刷新状态，让loadFiles负责重置它
        // 不创建新的协程，避免嵌套协程问题
        _isRefreshing.value = true
        loadFiles(_currentDirId.value)
    }
    
    /**
     * 导航到目录
     * 复用FileViewModel的接口设计
     */
    fun loadDirectoryContent(dirId: Long, dirName: String? = null) {
        navigateToDirectory(dirId, dirName)
    }
    
    /**
     * 导航到目录
     */
    fun navigateToDirectory(dirId: Long, dirName: String?) {
        Log.d(TAG, "导航到目录: dirId=$dirId, dirName=$dirName")
        // 在导航之前清除选择
        clearSelection()
        
        // 更新当前目录ID
        _currentDirId.value = dirId
        
        // 更新导航路径
        if (dirId == 0L) {
            // 返回收藏根目录
            Log.d(TAG, "返回收藏根目录")
            _currentPath.value = listOf(Pair(0L, "收藏夹"))
        } else {
            // 导航到子目录
            if (dirName != null) {
                val newPath = _currentPath.value + listOf(Pair(dirId, dirName))
                Log.d(TAG, "导航路径更新为: ${newPath.map { it.second }.joinToString(" > ")}")
                _currentPath.value = newPath
            }
        }
        
        // 加载目录内容
        loadFiles(dirId)
    }
    
    /**
     * 返回上一级，复用FileViewModel接口设计
     * 返回值表示是否成功导航
     */
    fun navigateUp(): Boolean {
        // 如果处于选择模式，先退出选择模式
        if (_isSelectionMode.value) {
            clearSelection()
            return true
        }
        
        val currentPathValue = _currentPath.value
        if (currentPathValue.size > 1) {
            // 获取上一级目录ID和名称
            val parentDir = currentPathValue[currentPathValue.size - 2]
            val parentDirId = parentDir.first
            
            Log.d(TAG, "返回上一级: ${parentDir.second} (ID: $parentDirId)")
            
            // 更新当前路径，移除当前目录
            val newPath = currentPathValue.subList(0, currentPathValue.size - 1)
            Log.d(TAG, "导航路径更新为: ${newPath.map { it.second }.joinToString(" > ")}")
            _currentPath.value = newPath
            
            // 更新当前目录ID
            _currentDirId.value = parentDirId
            
            // 加载上一级目录内容
            loadFiles(parentDirId)
            
            return true
        }
        
        Log.d(TAG, "已经在根目录，无法返回上一级")
        return false
    }
    
    /**
     * 更新导航路径到指定索引
     */
    fun updatePathToIndex(index: Int) {
        // 在导航之前清除选择
        clearSelection()
        
        val currentPathValue = _currentPath.value
        if (index >= 0 && index < currentPathValue.size) {
            // 获取目标目录ID
            val targetDir = currentPathValue[index]
            val targetDirId = targetDir.first
            
            Log.d(TAG, "通过面包屑导航到: ${targetDir.second} (ID: $targetDirId)")
            
            // 更新路径
            val newPath = currentPathValue.subList(0, index + 1)
            Log.d(TAG, "导航路径更新为: ${newPath.map { it.second }.joinToString(" > ")}")
            _currentPath.value = newPath
            
            // 更新当前目录ID
            _currentDirId.value = targetDirId
            
            // 加载目录内容
            loadFiles(targetDirId)
        } else {
            Log.e(TAG, "索引越界: index=$index, pathSize=${currentPathValue.size}")
        }
    }
    
    /**
     * 添加/移除文件到选中列表
     */
    fun toggleFileSelection(fileId: Long) {
        val currentSelected = _selectedFiles.value.toMutableList()
        if (currentSelected.contains(fileId)) {
            // 已选中，取消选择
            currentSelected.remove(fileId)
        } else {
            // 未选中，添加选择
            currentSelected.add(fileId)
        }
        
        // 更新选中状态
        _selectedFiles.value = currentSelected
        
        // 更新选择模式
        _isSelectionMode.value = currentSelected.isNotEmpty()
        
        Log.d(TAG, "文件选择变更: 当前选中 ${currentSelected.size} 个文件")
    }
    
    /**
     * 清除所有选择
     */
    fun clearSelection() {
        _selectedFiles.value = emptyList()
        _isSelectionMode.value = false
        Log.d(TAG, "清除所有选择")
    }
    
    /**
     * 获取选中的文件对象列表 - 兼容FileViewModel接口
     */
    fun getSelectedFiles(fileIds: List<Long>): List<File> {
        val files = when (val state = uiState.value) {
            is FileUiState.Success -> state.files
            else -> emptyList()
        }
        
        return files.filter { it.id != null && fileIds.contains(it.id) }
    }
    
    /**
     * 获取当前选中的文件对象列表
     */
    fun getSelectedFileObjects(): List<File> {
        return getSelectedFiles(_selectedFiles.value)
    }
    
    /**
     * 分享文件
     * @param fileIds 要分享的文件ID列表
     * @param validType 分享有效期类型（1-永久有效，2-7天，3-1天）
     * @param onComplete 完成回调
     */
    fun shareFile(fileIds: List<Long>, validType: Int, onComplete: (Boolean, String, ShareResponse?) -> Unit) {
        Log.d(TAG, "开始分享文件: $fileIds, 有效期类型: $validType")
        
        if (fileIds.isEmpty()) {
            Log.e(TAG, "文件ID列表为空")
            onComplete(false, "未选择文件", null)
            return
        }
        
        viewModelScope.launch {
            try {
                // 直接传入文件ID列表
                val response = dataSource.shareFile(fileIds, validType)
                if (response.code == 1 && response.data != null) {
                    Log.d(TAG, "分享文件成功: ${response.data}")
                    onComplete(true, "分享成功", response.data)
                } else {
                    Log.e(TAG, "分享文件失败: ${response.message}")
                    onComplete(false, response.message ?: "分享失败", null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "分享文件异常: ${e.message}", e)
                onComplete(false, e.message ?: "网络错误", null)
            }
        }
    }
    
    /**
     * 重命名文件
     */
    fun renameFile(fileId: Long, newName: String, onComplete: (Boolean, String) -> Unit) {
        Log.d(TAG, "开始重命名文件: fileId=$fileId, newName=$newName")
        
        viewModelScope.launch {
            try {
                val response = dataSource.renameFile(fileId, newName)
                if (response.code == 1) {
                    Log.d(TAG, "重命名文件成功")
                    // 重命名成功后刷新当前列表
                    loadData()
                    onComplete(true, "重命名成功")
                } else {
                    Log.e(TAG, "重命名文件失败: ${response.message}")
                    onComplete(false, response.message ?: "重命名失败")
                }
            } catch (e: Exception) {
                Log.e(TAG, "重命名文件异常: ${e.message}", e)
                onComplete(false, e.message ?: "网络错误")
            }
        }
    }
    
    /**
     * 获取文件详情
     */
    fun getFileDetails(fileIds: List<Long>, onComplete: (Boolean, String, FileDetail?) -> Unit) {
        Log.d(TAG, "开始获取文件详情: $fileIds")
        
        if (fileIds.isEmpty()) {
            Log.e(TAG, "文件ID列表为空")
            onComplete(false, "未选择文件", null)
            return
        }
        
        viewModelScope.launch {
            try {
                // 直接传递整个文件ID列表，支持查看多个文件详情
                val response = dataSource.getFileDetails(fileIds)
                if (response.code == 1 && response.data != null) {
                    Log.d(TAG, "获取文件详情成功: ${response.data}")
                    onComplete(true, "获取详情成功", response.data)
                } else {
                    Log.e(TAG, "获取文件详情失败: ${response.message}")
                    onComplete(false, response.message ?: "获取详情失败", null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "获取文件详情异常: ${e.message}", e)
                onComplete(false, e.message ?: "网络错误", null)
            }
        }
    }
    
    /**
     * 从收藏中移除文件
     */
    fun removeFromFavorites(fileIds: List<Long>, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = dataSource.unfavorites(fileIds)
                
                if (response.code == 1) {
                    Log.d(TAG, "成功从收藏夹移除文件")
                    // 刷新当前目录
                    loadFiles(_currentDirId.value)
                    // 清除选择
                    clearSelection()
                    onComplete(true, "成功从收藏夹移除")
                } else {
                    Log.e(TAG, "移除收藏失败: ${response.message}")
                    onComplete(false, response.message ?: "移除收藏失败")
                }
            } catch (e: Exception) {
                Log.e(TAG, "移除收藏异常: ${e.message}", e)
                onComplete(false, e.message ?: "网络错误")
            }
        }
    }
    
    /**
     * 下载文件
     */
    fun downloadFiles(fileIds: List<Long>, onComplete: (Boolean, String) -> Unit) {
        // 实现文件下载逻辑，可以调用相关API
        Log.d(TAG, "下载文件: $fileIds")
        // 模拟下载成功
        onComplete(true, "开始下载文件")
    }
} 