package com.panjx.clouddrive.util

import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

/**
 * 文件大小工具类
 * 
 * 用于格式化文件大小显示
 */
object FileSizeUtils {
    
    /**
     * 格式化文件大小
     * 
     * @param size 文件大小（字节）
     * @return 格式化后的文件大小字符串
     */
    fun formatFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        
        return String.format(Locale.US, "%.1f %s", 
            size / 1024.0.pow(digitGroups.toDouble()),
            units[digitGroups])
    }
} 