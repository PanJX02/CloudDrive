package com.panjx.clouddrive.feature.fileRoute.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.panjx.clouddrive.core.modle.File
import com.panjx.clouddrive.core.network.datasource.MyRetrofitDatasource
import com.panjx.clouddrive.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// 定义UI状态
sealed class FileUiState {
    object Loading : FileUiState()
    object ListLoading : FileUiState() // 新增状态：只有文件列表在加载
    data class Success(val files: List<File>) : FileUiState()
    data class Error(val message: String) : FileUiState()
}

/**
 * 专门处理文件列表加载和刷新的ViewModel
 */
class FileListViewModel(application: Application) : AndroidViewModel(application) {
    private val userPreferences = UserPreferences(application)
    private val networkDataSource = MyRetrofitDatasource(userPreferences)
    
    private val _uiState = MutableStateFlow<FileUiState>(FileUiState.Loading)
    val uiState: StateFlow<FileUiState> = _uiState
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    /**
     * 根据ID列表获取选中的文件对象列表
     */
    fun getSelectedFiles(fileIds: List<Long>): List<File> {
        Log.d("FileListViewModel", "getSelectedFiles: 开始查找, 传入ID数量: ${fileIds.size}")
        Log.d("FileListViewModel", "传入的ID列表: $fileIds")
        
        val currentState = _uiState.value
        if (currentState is FileUiState.Success) {
            Log.d("FileListViewModel", "当前状态是Success, 文件列表大小: ${currentState.files.size}")
            
            // 打印所有文件的ID进行对比
            currentState.files.forEachIndexed { index, file ->
                Log.d("FileListViewModel", "文件[$index]: id=${file.id}, 名称=${file.fileName}")
            }
            
            // 进行筛选
            val result = currentState.files.filter { file -> 
                val match = fileIds.contains(file.id)
                Log.d("FileListViewModel", "检查文件 ${file.fileName}(id=${file.id}): 是否匹配=${match}")
                match
            }
            
            Log.d("FileListViewModel", "筛选结果: 找到${result.size}个匹配文件")
            return result
        }
        
        Log.d("FileListViewModel", "当前状态不是Success, 无法获取文件列表")
        return emptyList()
    }

    // 下拉刷新数据
    fun refreshData(dirId: Long) {
        // 设置刷新状态为true
        _isRefreshing.value = true
        // 加载目录内容，标记为刷新操作
        loadDirectoryContent(dirId, isRefresh = true)
    }
    
    // 加载特定目录的内容
    fun loadDirectoryContent(dirId: Long, isRefresh: Boolean = false) {
        viewModelScope.launch {
            Log.d("FileListViewModel", "loadDirectoryContent: 开始加载目录 $dirId 的内容")
            
            // 如果不是下拉刷新，才更改UI状态
            if (!isRefresh) {
                // 区分初始加载和文件夹导航
                val isInitialLoad = _uiState.value !is FileUiState.Success
                
                // 如果是初始加载，显示全屏Loading；否则只加载列表部分
                if (isInitialLoad) {
                    _uiState.value = FileUiState.Loading
                } else {
                    _uiState.value = FileUiState.ListLoading
                }
            }

            try {
                // 使用网络API获取数据
                val response = networkDataSource.getFilesByFolderId(dirId.toString())
                if (response.code == 1 && response.data != null) {
                    val files = response.data.list ?: emptyList()
                    _uiState.value = FileUiState.Success(files)
                } else {
                    throw Exception(response.message ?: "请求失败")
                }
                
            } catch (e: Exception) {
                Log.e("FileListViewModel", "加载目录内容失败: ${e.message}")
                _uiState.value = FileUiState.Error(e.message ?: "未知错误")
            } finally {
                _isRefreshing.value = false
            }
        }
    }
} 