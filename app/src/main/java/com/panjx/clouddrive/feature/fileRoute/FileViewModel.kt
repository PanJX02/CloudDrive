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
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            Log.d("FileViewModel", "loadData: 开始加载数据")
            // 如果是初始加载，显示Loading状态，否则使用isRefreshing
            if (_uiState.value !is FileUiState.Success) {
                _uiState.value = FileUiState.Loading
            } else {
                _isRefreshing.value = true
            }

            try {
                val response = MyRetrofitDatasource.files()

                if (response.code == 200) { // 或其他表示成功的状态码
                    Log.d("FileViewModel", "loadData: 请求成功，数据为：${response.data}")
                    val fileList = response.data?.list ?: emptyList()
                    _uiState.value = FileUiState.Success(fileList)
                } else {
                    Log.e("FileViewModel", "loadData: 请求失败，错误码：${response.code}，错误信息：${response.message}")
                    _uiState.value = FileUiState.Error(response.message ?: "请求失败，错误码：${response.code}")
                }
            } catch (e: Exception) {
                Log.e("FileViewModel", "网络请求失败: ${e.message}")
                _uiState.value = FileUiState.Error(e.message ?: "未知错误")
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}