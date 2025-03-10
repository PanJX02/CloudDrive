package com.panjx.clouddrive.feature.fileRoute

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panjx.clouddrive.core.modle.File
import com.panjx.clouddrive.core.network.datasource.MyRetrofitDatasource
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

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = FileUiState.Loading
            try {
                val files = MyRetrofitDatasource.files()
                val fileList = files.data?.list ?: emptyList()
                _uiState.value = FileUiState.Success(fileList)
            } catch (e: Exception) {
                Log.e("FileViewModel", "网络请求失败: ${e.message}")
                _uiState.value = FileUiState.Error(e.message ?: "未知错误")
            }
        }
    }
}