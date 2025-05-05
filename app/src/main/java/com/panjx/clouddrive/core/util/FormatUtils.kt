package com.panjx.clouddrive.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 格式化文件大小
 * @param bytes 文件大小（字节）
 * @return 格式化后的文件大小字符串
 */
fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format("%.2f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}

/**
 * 格式化时间戳
 * @param timestamp 时间戳（毫秒）
 * @param defaultValue 默认值，当时间戳为null时返回
 * @return 格式化后的时间字符串
 */
fun formatTimestamp(timestamp: Long?, defaultValue: String = "未知"): String {
    if (timestamp == null) return defaultValue
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return formatter.format(date)
} 