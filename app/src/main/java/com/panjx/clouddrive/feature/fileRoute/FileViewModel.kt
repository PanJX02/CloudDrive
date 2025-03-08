package com.panjx.clouddrive.feature.fileRoute

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.panjx.clouddrive.core.modle.File
import com.panjx.clouddrive.core.ui.FilePreviewParameterData
import com.panjx.clouddrive.feature.main.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FileViewModel: ViewModel() {
    private val _datum = MutableStateFlow<List<File>>(emptyList())
    val datum: StateFlow<List<File>> = _datum
    init {
        loadData()
    }

    private fun loadData() {
        _datum.value = FilePreviewParameterData.FILES
    }
}