package com.panjx.clouddrive.feature.fileRoute

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panjx.clouddrive.core.modle.File
import com.panjx.clouddrive.core.ui.FilePreviewParameterData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// 定义UI状态
sealed class FileUiState {
    object Loading : FileUiState()
    data class Success(val files: List<File>) : FileUiState()
    data class Error(val message: String) : FileUiState()
}

class FileViewModel: ViewModel() {
    private val _uiState = MutableStateFlow<FileUiState>(FileUiState.Loading)
    val uiState: StateFlow<FileUiState> = _uiState
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing
    
    // 当前目录ID，"0"表示根目录
    private val _currentDirId = MutableStateFlow("0")
    val currentDirId: StateFlow<String> = _currentDirId
    
    // 当前路径，用于展示面包屑导航
    private val _currentPath = MutableStateFlow<List<Pair<String, String>>>(listOf(Pair("0", "根目录")))
    val currentPath: StateFlow<List<Pair<String, String>>> = _currentPath

    init {
        loadData()
    }

    fun loadData() {
        loadDirectoryContent(_currentDirId.value)
    }
    
    // 加载特定目录的内容
    fun loadDirectoryContent(dirId: String, dirName: String? = null) {
        viewModelScope.launch {
            Log.d("FileViewModel", "loadDirectoryContent: 开始加载目录 $dirId 的内容")
            
            // 如果是初始加载，显示Loading状态，否则使用isRefreshing
            if (_uiState.value !is FileUiState.Success) {
                _uiState.value = FileUiState.Loading
            } else {
                _isRefreshing.value = true
            }

            try {
                // 暂时使用本地模拟数据
                val allFiles = FilePreviewParameterData.FILES
                val filteredFiles = if (dirId == "0") {
                    // 根目录只显示parentId为"0"的文件
                    allFiles.filter { it.parentId == "0" }
                } else {
                    // 子目录显示parentId匹配的文件
                    allFiles.filter { it.parentId == dirId }
                }
                
                // 更新当前目录ID
                _currentDirId.value = dirId
                
                // 更新路径
                if (dirId == "0") {
                    // 重置到根目录
                    _currentPath.value = listOf(Pair("0", "根目录"))
                } else if (dirName != null) {
                    // 如果提供了目录名，添加到路径中
                    val newPath = _currentPath.value.toMutableList()
                    // 检查是否已经存在该目录，防止重复添加
                    if (newPath.none { it.first == dirId }) {
                        newPath.add(Pair(dirId, dirName))
                        _currentPath.value = newPath
                    }
                }
                
                _uiState.value = FileUiState.Success(filteredFiles)
                
            } catch (e: Exception) {
                Log.e("FileViewModel", "加载目录内容失败: ${e.message}")
                _uiState.value = FileUiState.Error(e.message ?: "未知错误")
            } finally {
                _isRefreshing.value = false
            }
        }
    }
    
    // 返回上一级目录
    fun navigateUp() {
        val currentPath = _currentPath.value
        if (currentPath.size > 1) {
            // 移除当前路径的最后一个元素，返回上一级
            val newPath = currentPath.dropLast(1)
            _currentPath.value = newPath
            // 加载上一级目录内容
            val parentDir = newPath.last()
            loadDirectoryContent(parentDir.first)
        }
    }
}