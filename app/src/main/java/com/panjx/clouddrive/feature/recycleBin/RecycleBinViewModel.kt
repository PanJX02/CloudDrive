package com.panjx.clouddrive.feature.recycleBin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panjx.clouddrive.core.modle.File
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecycleBinViewModel @Inject constructor(
    private val repository: RecycleBinRepository
) : ViewModel() {
    // UI状态
    private val _uiState = MutableStateFlow<RecycleBinUiState>(RecycleBinUiState.Loading)
    val uiState: StateFlow<RecycleBinUiState> = _uiState.asStateFlow()

    // 回收站文件列表
    private val _files = MutableStateFlow<List<File>>(emptyList())
    val files: StateFlow<List<File>> = _files.asStateFlow()

    // 当前选中的文件ID列表
    private val _selectedFiles = MutableStateFlow<List<Long>>(emptyList())
    val selectedFiles: StateFlow<List<Long>> = _selectedFiles.asStateFlow()

    // 是否正在刷新
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadRecycleBinFiles()
    }

    // 加载回收站文件列表
    fun loadRecycleBinFiles() {
        viewModelScope.launch {
            _uiState.value = RecycleBinUiState.Loading
            _isRefreshing.value = true
            
            try {
                val result = repository.getRecycleBinFiles()
                _files.value = result
                _uiState.value = RecycleBinUiState.Success
            } catch (e: Exception) {
                _uiState.value = RecycleBinUiState.Error(e.message ?: "获取回收站文件失败")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    // 更新选中状态
    fun updateSelection(fileId: Long, isSelected: Boolean) {
        val currentSelection = _selectedFiles.value.toMutableList()
        if (isSelected) {
            if (!currentSelection.contains(fileId)) {
                currentSelection.add(fileId)
            }
        } else {
            currentSelection.remove(fileId)
        }
        _selectedFiles.value = currentSelection
    }

    // 清除所有选中
    fun clearSelection() {
        _selectedFiles.value = emptyList()
    }

    // 恢复文件
    fun restoreFiles() {
        val fileIds = _selectedFiles.value
        if (fileIds.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = RecycleBinUiState.Operating
            try {
                repository.restoreFiles(fileIds)
                // 恢复成功后，从列表中移除已恢复的文件
                _files.value = _files.value.filter { it.id !in fileIds }
                _selectedFiles.value = emptyList()
                _uiState.value = RecycleBinUiState.Success
            } catch (e: Exception) {
                _uiState.value = RecycleBinUiState.Error(e.message ?: "恢复文件失败")
            }
        }
    }

    // 永久删除文件
    fun deleteFiles() {
        val fileIds = _selectedFiles.value
        if (fileIds.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = RecycleBinUiState.Operating
            try {
                repository.deleteFilesFromRecycleBin(fileIds)
                // 删除成功后，从列表中移除已删除的文件
                _files.value = _files.value.filter { it.id !in fileIds }
                _selectedFiles.value = emptyList()
                _uiState.value = RecycleBinUiState.Success
            } catch (e: Exception) {
                _uiState.value = RecycleBinUiState.Error(e.message ?: "删除文件失败")
            }
        }
    }

    // 清空回收站
    fun emptyRecycleBin() {
        viewModelScope.launch {
            _uiState.value = RecycleBinUiState.Operating
            try {
                repository.emptyRecycleBin()
                _files.value = emptyList()
                _selectedFiles.value = emptyList()
                _uiState.value = RecycleBinUiState.Success
            } catch (e: Exception) {
                _uiState.value = RecycleBinUiState.Error(e.message ?: "清空回收站失败")
            }
        }
    }
}

// UI状态
sealed class RecycleBinUiState {
    object Loading : RecycleBinUiState()
    object Success : RecycleBinUiState()
    object Operating : RecycleBinUiState()
    data class Error(val message: String) : RecycleBinUiState()
} 