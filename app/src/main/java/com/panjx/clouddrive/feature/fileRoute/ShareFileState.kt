package com.panjx.clouddrive.feature.fileRoute

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * 分享文件状态管理器，用于在分享文件页面和转存选择文件夹页面之间共享数据
 */
object ShareFileState {
    // 选中的分享文件ID列表
    var selectedFileIds by mutableStateOf<List<Long>>(emptyList())
    
    // 分享密钥
    var shareKey by mutableStateOf("")
    
    // 分享验证码
    var shareCode by mutableStateOf("")
    
    // 更新选中的文件ID
    fun updateFiles(fileIds: List<Long>) {
        Log.d("ShareFileState", "更新选中文件: ${fileIds.size}个文件")
        selectedFileIds = fileIds
    }
    
    // 设置分享参数
    fun setShareParams(key: String, code: String) {
        Log.d("ShareFileState", "设置分享参数: key=$key, code=$code")
        shareKey = key
        shareCode = code
    }
    
    // 清空状态
    fun clear() {
        Log.d("ShareFileState", "清空状态")
        selectedFileIds = emptyList()
        shareKey = ""
        shareCode = ""
    }
} 