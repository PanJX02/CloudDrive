package com.panjx.clouddrive.feature.fileRoute.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.panjx.clouddrive.core.network.datasource.MyRetrofitDatasource
import com.panjx.clouddrive.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// 文件操作状态
sealed class FileOperationState {
    object Idle : FileOperationState() // 空闲状态
    object Loading : FileOperationState() // 操作进行中
    data class Success(val message: String) : FileOperationState() // 操作成功
    data class Error(val message: String) : FileOperationState() // 操作失败
}

/**
 * 专门处理文件操作（复制、移动、创建等）的ViewModel
 */
class FileOperationViewModel(application: Application) : AndroidViewModel(application) {
    private val userPreferences = UserPreferences(application)
    private val networkDataSource = MyRetrofitDatasource(userPreferences)
    
    // 文件操作状态（复制、移动等）
    private val _operationState = MutableStateFlow<FileOperationState>(FileOperationState.Idle)
    val operationState: StateFlow<FileOperationState> = _operationState

    // 复制文件到指定目录
    fun copyFiles(fileIds: List<Long>, targetFolderId: Long, onComplete: (Boolean, String) -> Unit) {
        if (fileIds.isEmpty()) {
            onComplete(false, "未选择任何文件")
            return
        }
        
        _operationState.value = FileOperationState.Loading
        
        viewModelScope.launch {
            try {
                Log.d("FileOperationViewModel", "开始复制文件: ${fileIds.joinToString()} 到文件夹 $targetFolderId")
                
                // 调用API执行复制
                val response = networkDataSource.copyFiles(fileIds, targetFolderId)
                if (response.code == 1) {
                    _operationState.value = FileOperationState.Success("复制成功")
                    onComplete(true, "文件复制成功")
                } else {
                    _operationState.value = FileOperationState.Error(response.message ?: "复制失败")
                    onComplete(false, response.message ?: "复制失败")
                }
            } catch (e: Exception) {
                Log.e("FileOperationViewModel", "复制文件失败: ${e.message}")
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
                Log.d("FileOperationViewModel", "开始移动文件: ${fileIds.joinToString()} 到文件夹 $targetFolderId")
                
                // 调用API执行移动
                val response = networkDataSource.moveFiles(fileIds, targetFolderId)
                if (response.code == 1) {
                    _operationState.value = FileOperationState.Success("移动成功")
                    onComplete(true, "文件移动成功")
                } else {
                    _operationState.value = FileOperationState.Error(response.message ?: "移动失败")
                    onComplete(false, response.message ?: "移动失败")
                }
            } catch (e: Exception) {
                Log.e("FileOperationViewModel", "移动文件失败: ${e.message}")
                _operationState.value = FileOperationState.Error(e.message ?: "未知错误")
                onComplete(false, e.message ?: "未知错误")
            }
        }
    }

    // 创建新文件夹
    fun createFolder(folderName: String, parentDirId: Long, onComplete: (Boolean, String) -> Unit) {
        if (folderName.isBlank()) {
            onComplete(false, "文件夹名称不能为空")
            return
        }
        
        _operationState.value = FileOperationState.Loading
        
        viewModelScope.launch {
            try {
                Log.d("FileOperationViewModel", "创建文件夹: $folderName 在目录 $parentDirId")
                
                // 调用API创建文件夹
                val response = networkDataSource.createFolder(folderName, parentDirId)
                if (response.code == 1) {
                    _operationState.value = FileOperationState.Success("创建成功")
                    onComplete(true, "文件夹创建成功")
                } else {
                    _operationState.value = FileOperationState.Error(response.message ?: "创建失败")
                    onComplete(false, response.message ?: "创建失败")
                }
            } catch (e: Exception) {
                Log.e("FileOperationViewModel", "创建文件夹失败: ${e.message}")
                _operationState.value = FileOperationState.Error(e.message ?: "未知错误")
                onComplete(false, e.message ?: "未知错误")
            }
        }
    }
    
    // 重置操作状态为空闲
    fun resetOperationState() {
        _operationState.value = FileOperationState.Idle
    }
} 