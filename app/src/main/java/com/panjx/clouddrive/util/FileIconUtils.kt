package com.panjx.clouddrive.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 文件图标工具类
 * 
 * 用于根据文件类型(MIME类型)返回对应的图标
 */
object FileIconUtils {
    
    /**
     * 根据文件夹类型和MIME类型获取对应的图标
     * 
     * @param folderType 文件夹类型，1表示文件夹，0表示文件
     * @param mimeType MIME类型字符串
     * @return 对应的Material图标
     */
    fun getFileIcon(folderType: Int, mimeType: String?): ImageVector {
        // 如果是文件夹，直接返回文件夹图标
        if (folderType == 1) {
            return Icons.Filled.Folder
        }
        
        // 如果是文件，根据MIME类型返回对应图标
        return when {
            mimeType == null -> Icons.Filled.FileCopy
            mimeType.startsWith("image/") -> Icons.Filled.Image
            mimeType.startsWith("video/") -> Icons.Filled.VideoFile
            mimeType.startsWith("audio/") -> Icons.Filled.AudioFile
            mimeType.startsWith("application/pdf") -> Icons.Filled.PictureAsPdf
            mimeType.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml.document") 
                || mimeType.startsWith("application/msword") -> Icons.Filled.Description
            mimeType.startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") 
                || mimeType.startsWith("application/vnd.ms-excel") -> Icons.AutoMirrored.Filled.InsertDriveFile
            mimeType.startsWith("text/") -> Icons.Filled.Code
            else -> Icons.Filled.FileCopy
        }
    }
} 