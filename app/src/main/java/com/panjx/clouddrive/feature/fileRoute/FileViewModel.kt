package com.panjx.clouddrive.feature.fileRoute

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

// 文件操作状态
sealed class FileOperationState {
    object Idle : FileOperationState() // 空闲状态
    object Loading : FileOperationState() // 操作进行中
    data class Success(val message: String) : FileOperationState() // 操作成功
    data class Error(val message: String) : FileOperationState() // 操作失败
}

class FileViewModel(application: Application): AndroidViewModel(application) {
    private val userPreferences = UserPreferences(application)
    private val networkDataSource = MyRetrofitDatasource(userPreferences)
    
    private val _uiState = MutableStateFlow<FileUiState>(FileUiState.Loading)
    val uiState: StateFlow<FileUiState> = _uiState
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing
    
    // 当前目录ID，0表示根目录
    private val _currentDirId = MutableStateFlow(0L)
    val currentDirId: StateFlow<Long> = _currentDirId
    
    // 当前路径，用于展示面包屑导航
    private val _currentPath = MutableStateFlow<List<Pair<Long, String>>>(listOf(Pair(0L, "根目录")))
    val currentPath: StateFlow<List<Pair<Long, String>>> = _currentPath
    
    // 文件操作状态（复制、移动等）
    private val _operationState = MutableStateFlow<FileOperationState>(FileOperationState.Idle)
    val operationState: StateFlow<FileOperationState> = _operationState

    init {
        // 首次加载不使用下拉刷新动画
        loadDirectoryContent(_currentDirId.value)
    }

    /**
     * 根据ID列表获取选中的文件对象列表
     */
    fun getSelectedFiles(fileIds: List<Long>): List<File> {
        Log.d("FileViewModel", "getSelectedFiles: 开始查找, 传入ID数量: ${fileIds.size}")
        Log.d("FileViewModel", "传入的ID列表: $fileIds")
        
        val currentState = _uiState.value
        if (currentState is FileUiState.Success) {
            Log.d("FileViewModel", "当前状态是Success, 文件列表大小: ${currentState.files.size}")
            
            // 打印所有文件的ID进行对比
            currentState.files.forEachIndexed { index, file ->
                Log.d("FileViewModel", "文件[$index]: id=${file.id}, 名称=${file.fileName}")
            }
            
            // 进行筛选
            val result = currentState.files.filter { file -> 
                val match = fileIds.contains(file.id)
                Log.d("FileViewModel", "检查文件 ${file.fileName}(id=${file.id}): 是否匹配=${match}")
                match
            }
            
            Log.d("FileViewModel", "筛选结果: 找到${result.size}个匹配文件")
            return result
        }
        
        Log.d("FileViewModel", "当前状态不是Success, 无法获取文件列表")
        return emptyList()
    }

    fun loadData() {
        // 设置刷新状态为true
        _isRefreshing.value = true
        // 使用isRefresh参数调用loadDirectoryContent
        loadDirectoryContent(_currentDirId.value, isRefresh = true)
    }
    
    // 加载特定目录的内容
    fun loadDirectoryContent(dirId: Long, dirName: String? = null, isRefresh: Boolean = false) {
        viewModelScope.launch {
            Log.d("FileViewModel", "loadDirectoryContent: 开始加载目录 $dirId 的内容")
            
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
            
            // 立即更新路径
            if (dirId == 0L) {
                // 重置到根目录
                _currentPath.value = listOf(Pair(0L, "根目录"))
            } else if (dirName != null) {
                // 如果提供了目录名，添加到路径中
                val newPath = _currentPath.value.toMutableList()
                // 检查是否已经存在该目录，防止重复添加
                if (newPath.none { it.first == dirId }) {
                    newPath.add(Pair(dirId, dirName))
                    _currentPath.value = newPath
                }
            } else {
                // 处理点击面包屑导航已有路径的情况
                // 查找点击的路径项在当前路径中的位置
                val existingPathIndex = _currentPath.value.indexOfFirst { it.first == dirId }
                if (existingPathIndex != -1) {
                    // 如果找到了，截断路径至该位置（保留该项）
                    _currentPath.value = _currentPath.value.subList(0, existingPathIndex + 1)
                }
            }
            
            // 更新当前目录ID
            _currentDirId.value = dirId

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
    
    // 复制文件到指定目录
    fun copyFiles(fileIds: List<Long>, targetFolderId: Long, onComplete: (Boolean, String) -> Unit) {
        if (fileIds.isEmpty()) {
            onComplete(false, "未选择任何文件")
            return
        }
        
        _operationState.value = FileOperationState.Loading
        
        viewModelScope.launch {
            try {
                Log.d("FileViewModel", "开始复制文件: ${fileIds.joinToString()} 到文件夹 $targetFolderId")
                
                // 调用API执行复制
                val response = networkDataSource.copyFiles(fileIds, targetFolderId)
                if (response.code == 1) {
                    _operationState.value = FileOperationState.Success("复制成功")
                    // 如果当前正在查看的就是目标文件夹，刷新列表
                    if (_currentDirId.value == targetFolderId) {
                        loadDirectoryContent(_currentDirId.value)
                    }
                    onComplete(true, "文件复制成功")
                } else {
                    _operationState.value = FileOperationState.Error(response.message ?: "复制失败")
                    onComplete(false, response.message ?: "复制失败")
                }
            } catch (e: Exception) {
                Log.e("FileViewModel", "复制文件失败: ${e.message}")
                _operationState.value = FileOperationState.Error(e.message ?: "未知错误")
                onComplete(false, e.message ?: "未知错误")
            }
        }
    }
    
    // 移动文件到指定目录
    fun moveFiles(fileIds: List<Long>, targetFolderId: Long, onComplete: (Boolean, String) -> Unit) {
        if (fileIds.isEmpty()) {
            onComplete(false, "未选择任何文件")
            return
        }
        
        _operationState.value = FileOperationState.Loading
        
        viewModelScope.launch {
            try {
                Log.d("FileViewModel", "开始移动文件: ${fileIds.joinToString()} 到文件夹 $targetFolderId")
                
                // 调用API执行移动
                val response = networkDataSource.moveFiles(fileIds, targetFolderId)
                if (response.code == 1) {
                    _operationState.value = FileOperationState.Success("移动成功")
                    // 刷新当前目录，因为移动后文件列表改变了
                    loadDirectoryContent(_currentDirId.value)
                    onComplete(true, "文件移动成功")
                } else {
                    _operationState.value = FileOperationState.Error(response.message ?: "移动失败")
                    onComplete(false, response.message ?: "移动失败")
                }
            } catch (e: Exception) {
                Log.e("FileViewModel", "移动文件失败: ${e.message}")
                _operationState.value = FileOperationState.Error(e.message ?: "未知错误")
                onComplete(false, e.message ?: "未知错误")
            }
        }
    }

    // 创建新文件夹
    fun createFolder(folderName: String, onComplete: (Boolean, String) -> Unit) {
        if (folderName.isBlank()) {
            onComplete(false, "文件夹名称不能为空")
            return
        }
        
        _operationState.value = FileOperationState.Loading
        
        viewModelScope.launch {
            try {
                Log.d("FileViewModel", "创建文件夹: $folderName 在目录 ${_currentDirId.value}")
                
                // 调用API创建文件夹
                val response = networkDataSource.createFolder(folderName, _currentDirId.value)
                if (response.code == 1) {
                    _operationState.value = FileOperationState.Success("创建成功")
                    // 刷新当前目录
                    loadDirectoryContent(_currentDirId.value)
                    onComplete(true, "文件夹创建成功")
                } else {
                    _operationState.value = FileOperationState.Error(response.message ?: "创建失败")
                    onComplete(false, response.message ?: "创建失败")
                }
            } catch (e: Exception) {
                Log.e("FileViewModel", "创建文件夹失败: ${e.message}")
                _operationState.value = FileOperationState.Error(e.message ?: "未知错误")
                onComplete(false, e.message ?: "未知错误")
            }
        }
    }
}