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
    
    // 需要刷新的文件夹ID集合
    private val foldersToRefresh = mutableSetOf<Long>()
    
    // 标记一个文件夹需要刷新
    fun markFolderForRefresh(folderId: Long) {
        Log.d("FileOperationViewModel", "标记文件夹需要刷新: $folderId")
        foldersToRefresh.add(folderId)
    }
    
    // 检查文件夹是否需要刷新
    fun checkAndClearRefreshFlag(folderId: Long): Boolean {
        val needsRefresh = foldersToRefresh.contains(folderId)
        if (needsRefresh) {
            Log.d("FileOperationViewModel", "文件夹需要刷新，清除标记: $folderId")
            foldersToRefresh.remove(folderId)
        }
        return needsRefresh
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
    
    // 删除文件
    fun deleteFiles(fileIds: List<Long>, onComplete: (Boolean, String) -> Unit) {
        if (fileIds.isEmpty()) {
            onComplete(false, "未选择任何文件")
            return
        }
        
        _operationState.value = FileOperationState.Loading
        
        viewModelScope.launch {
            try {
                Log.d("FileOperationViewModel", "开始删除文件: ${fileIds.joinToString()}")
                
                // 调用API执行删除
                val response = networkDataSource.deleteFiles(fileIds)
                if (response.code == 1) {
                    _operationState.value = FileOperationState.Success("删除成功")
                    onComplete(true, "文件删除成功")
                } else {
                    _operationState.value = FileOperationState.Error(response.message ?: "删除失败")
                    onComplete(false, response.message ?: "删除失败")
                }
            } catch (e: Exception) {
                Log.e("FileOperationViewModel", "删除文件失败: ${e.message}")
                _operationState.value = FileOperationState.Error(e.message ?: "未知错误")
                onComplete(false, e.message ?: "未知错误")
            }
        }
    }
    
    // 重命名文件
    fun renameFile(fileId: Long, newName: String, onComplete: (Boolean, String) -> Unit) {
        if (newName.isBlank()) {
            onComplete(false, "文件名不能为空")
            return
        }
        
        _operationState.value = FileOperationState.Loading
        
        viewModelScope.launch {
            try {
                Log.d("FileOperationViewModel", "开始重命名文件: fileId=$fileId, newName=$newName")
                
                // 调用API执行重命名
                val response = networkDataSource.renameFile(fileId, newName)
                if (response.code == 1) {
                    _operationState.value = FileOperationState.Success("重命名成功")
                    onComplete(true, "文件重命名成功")
                } else {
                    _operationState.value = FileOperationState.Error(response.message ?: "重命名失败")
                    onComplete(false, response.message ?: "重命名失败")
                }
            } catch (e: Exception) {
                Log.e("FileOperationViewModel", "重命名文件失败: ${e.message}")
                _operationState.value = FileOperationState.Error(e.message ?: "未知错误")
                onComplete(false, e.message ?: "未知错误")
            }
        }
    }
    
    // 添加收藏
    fun addToFavorites(fileIds: List<Long>, onComplete: (Boolean, String) -> Unit) {
        if (fileIds.isEmpty()) {
            onComplete(false, "未选择任何文件")
            return
        }
        
        _operationState.value = FileOperationState.Loading
        
        viewModelScope.launch {
            try {
                Log.d("FileOperationViewModel", "开始添加收藏: ${fileIds.joinToString()}")
                
                // 调用API执行收藏
                val response = networkDataSource.favorites(fileIds)
                if (response.code == 1) {
                    _operationState.value = FileOperationState.Success("收藏成功")
                    onComplete(true, "添加收藏成功")
                } else {
                    _operationState.value = FileOperationState.Error(response.message ?: "收藏失败")
                    onComplete(false, response.message ?: "收藏失败")
                }
            } catch (e: Exception) {
                Log.e("FileOperationViewModel", "添加收藏失败: ${e.message}")
                _operationState.value = FileOperationState.Error(e.message ?: "未知错误")
                onComplete(false, e.message ?: "未知错误")
            }
        }
    }
    
    // 取消收藏
    fun removeFromFavorites(fileIds: List<Long>, onComplete: (Boolean, String) -> Unit) {
        if (fileIds.isEmpty()) {
            onComplete(false, "未选择任何文件")
            return
        }
        
        _operationState.value = FileOperationState.Loading
        
        viewModelScope.launch {
            try {
                Log.d("FileOperationViewModel", "开始取消收藏: ${fileIds.joinToString()}")
                
                // 调用API执行取消收藏
                val response = networkDataSource.unfavorites(fileIds)
                if (response.code == 1) {
                    _operationState.value = FileOperationState.Success("取消收藏成功")
                    onComplete(true, "取消收藏成功")
                } else {
                    _operationState.value = FileOperationState.Error(response.message ?: "取消收藏失败")
                    onComplete(false, response.message ?: "取消收藏失败")
                }
            } catch (e: Exception) {
                Log.e("FileOperationViewModel", "取消收藏失败: ${e.message}")
                _operationState.value = FileOperationState.Error(e.message ?: "未知错误")
                onComplete(false, e.message ?: "未知错误")
            }
        }
    }
    
    // 获取文件详情
    fun getFileDetails(fileIds: List<Long>, onComplete: (Boolean, String, com.panjx.clouddrive.core.modle.FileDetail?) -> Unit) {
        if (fileIds.isEmpty()) {
            onComplete(false, "未选择任何文件", null)
            return
        }
        
        _operationState.value = FileOperationState.Loading
        
        viewModelScope.launch {
            try {
                Log.d("FileOperationViewModel", "获取文件详情: ${fileIds.joinToString()}")
                
                // 调用API获取文件详情
                val response = networkDataSource.getFileDetails(fileIds)
                Log.d("FileOperationViewModel", "文件详情响应: code=${response.code}, message=${response.message}")
                Log.d("FileOperationViewModel", "文件详情数据: ${response.data}")
                
                if (response.code == 1) {
                    if (response.data != null) {
                        _operationState.value = FileOperationState.Success("获取详情成功")
                        Log.d("FileOperationViewModel", "文件详情解析成功")
                        onComplete(true, "获取文件详情成功", response.data)
                    } else {
                        Log.e("FileOperationViewModel", "文件详情数据为空")
                        _operationState.value = FileOperationState.Error("获取详情成功但数据为空")
                        onComplete(false, "获取文件详情成功但数据为空", null)
                    }
                } else {
                    Log.e("FileOperationViewModel", "获取文件详情失败: ${response.message}")
                    _operationState.value = FileOperationState.Error(response.message ?: "获取详情失败")
                    onComplete(false, response.message ?: "获取详情失败", null)
                }
            } catch (e: Exception) {
                Log.e("FileOperationViewModel", "获取文件详情异常: ${e.message}")
                e.printStackTrace()
                _operationState.value = FileOperationState.Error(e.message ?: "未知错误")
                onComplete(false, "获取文件详情发生异常: ${e.message}", null)
            }
        }
    }
    
    // 重置操作状态为空闲
    fun resetOperationState() {
        _operationState.value = FileOperationState.Idle
    }
    
    // 分享文件
    fun shareFile(fileIds: List<Long>, validType: Int, onComplete: (Boolean, String, com.panjx.clouddrive.core.modle.response.ShareResponse?) -> Unit) {
        if (fileIds.isEmpty()) {
            onComplete(false, "未选择任何文件", null)
            return
        }
        
        _operationState.value = FileOperationState.Loading
        
        viewModelScope.launch {
            try {
                Log.d("FileOperationViewModel", "开始分享文件: ${fileIds.joinToString()}, 有效期类型: $validType")
                
                // 调用API执行分享
                val response = networkDataSource.shareFile(fileIds, validType)
                Log.d("FileOperationViewModel", "分享文件响应: code=${response.code}, message=${response.message}")
                
                if (response.code == 1) {
                    if (response.data != null) {
                        _operationState.value = FileOperationState.Success("分享成功")
                        Log.d("FileOperationViewModel", "分享成功: shareKey=${response.data.shareKey}")
                        onComplete(true, "分享成功", response.data)
                    } else {
                        Log.e("FileOperationViewModel", "分享成功但数据为空")
                        _operationState.value = FileOperationState.Error("分享成功但数据为空")
                        onComplete(false, "分享成功但数据为空", null)
                    }
                } else {
                    Log.e("FileOperationViewModel", "分享文件失败: ${response.message}")
                    _operationState.value = FileOperationState.Error(response.message ?: "分享失败")
                    onComplete(false, response.message ?: "分享失败", null)
                }
            } catch (e: Exception) {
                Log.e("FileOperationViewModel", "分享文件异常: ${e.message}")
                e.printStackTrace()
                _operationState.value = FileOperationState.Error(e.message ?: "未知错误")
                onComplete(false, "分享文件发生异常: ${e.message}", null)
            }
        }
    }

    // 获取分享文件列表
    fun getShareFileList(shareKey: String, code: String, folderId: Long? = null, onComplete: (Boolean, String, com.panjx.clouddrive.core.modle.response.NetworkPageData<com.panjx.clouddrive.core.modle.File>?) -> Unit) {
        if (shareKey.isBlank()) {
            onComplete(false, "分享密钥不能为空", null)
            return
        }
        
        _operationState.value = FileOperationState.Loading
        
        viewModelScope.launch {
            try {
                Log.d("FileOperationViewModel", "开始获取分享内容: shareKey=$shareKey, code=$code, folderId=$folderId")
                
                // 调用API获取分享内容
                val response = networkDataSource.getShareFileList(shareKey, code, folderId)
                Log.d("FileOperationViewModel", "获取分享内容响应: code=${response.code}, message=${response.message}")
                
                if (response.code == 1) {
                    if (response.data != null) {
                        _operationState.value = FileOperationState.Success("获取分享内容成功")
                        Log.d("FileOperationViewModel", "获取分享内容成功: 共${response.data.list?.size ?: 0}个文件")
                        onComplete(true, "获取分享内容成功", response.data)
                    } else {
                        Log.e("FileOperationViewModel", "获取分享内容成功但数据为空")
                        _operationState.value = FileOperationState.Error("获取分享内容成功但数据为空")
                        onComplete(false, "获取分享内容成功但数据为空", null)
                    }
                } else {
                    Log.e("FileOperationViewModel", "获取分享内容失败: ${response.message}")
                    _operationState.value = FileOperationState.Error(response.message ?: "获取分享内容失败")
                    onComplete(false, response.message ?: "获取分享内容失败", null)
                }
            } catch (e: Exception) {
                Log.e("FileOperationViewModel", "获取分享内容异常: ${e.message}")
                e.printStackTrace()
                _operationState.value = FileOperationState.Error(e.message ?: "未知错误")
                onComplete(false, "获取分享内容发生异常: ${e.message}", null)
            }
        }
    }
    
    // 保存分享文件
    fun saveShareFiles(fileIds: List<Long>, targetFolderId: Long, shareKey: String, code: String, onComplete: (Boolean, String) -> Unit) {
        if (fileIds.isEmpty()) {
            onComplete(false, "未选择任何文件")
            return
        }
        
        _operationState.value = FileOperationState.Loading
        
        viewModelScope.launch {
            try {
                Log.d("FileOperationViewModel", "开始保存分享文件: fileIds=${fileIds.joinToString()}, targetFolderId=$targetFolderId, shareKey=$shareKey")
                
                // 调用API保存分享文件
                val response = networkDataSource.saveShareFiles(fileIds, targetFolderId, shareKey, code)
                Log.d("FileOperationViewModel", "保存分享文件响应: code=${response.code}, message=${response.message}")
                
                if (response.code == 1) {
                    _operationState.value = FileOperationState.Success("保存成功")
                    Log.d("FileOperationViewModel", "保存分享文件成功")
                    onComplete(true, "保存成功")
                } else {
                    Log.e("FileOperationViewModel", "保存分享文件失败: ${response.message}")
                    _operationState.value = FileOperationState.Error(response.message ?: "保存失败")
                    onComplete(false, response.message ?: "保存失败")
                }
            } catch (e: Exception) {
                Log.e("FileOperationViewModel", "保存分享文件异常: ${e.message}")
                e.printStackTrace()
                _operationState.value = FileOperationState.Error(e.message ?: "未知错误")
                onComplete(false, "保存分享文件发生异常: ${e.message}")
            }
        }
    }
} 