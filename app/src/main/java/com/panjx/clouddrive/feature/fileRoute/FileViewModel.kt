package com.panjx.clouddrive.feature.fileRoute

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panjx.clouddrive.core.modle.File
import com.panjx.clouddrive.core.network.datasource.MyRetrofitDatasource
import com.panjx.clouddrive.core.ui.FilePreviewParameterData
import com.panjx.clouddrive.feature.main.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FileViewModel: ViewModel() {
    private val _datum = MutableStateFlow<List<File>>(emptyList())
    val datum: StateFlow<List<File>> = _datum
    init {
        loadData()
    }

    private fun loadData() {
//        _datum.value = FilePreviewParameterData.FILES

//        //测试序列化
//        val json= Json.encodeToString(FilePreviewParameterData.FILE)
//        Log.d("FileViewModel", "loadData: $json")
//
//        val obj = Json.decodeFromString<File>(json)
//        Log.d("FileViewModel", "loadData: $obj")

        //测试网络请求
        viewModelScope.launch {
            try {
                val files = MyRetrofitDatasource.files()
                _datum.value = files.data?.list ?: emptyList()
            } catch (e: Exception) {
                Log.e("FileViewModel", "网络请求失败: ${e.message}")
                // 更新 UI 显示错误状态
            }
        }
    }
}