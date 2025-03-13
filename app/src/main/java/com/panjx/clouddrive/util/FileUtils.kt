package com.panjx.clouddrive.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.text.DecimalFormat

/**
 * 文件工具类
 * 提供文件大小格式化、文件类型判断等功能
 */
object FileUtils {
    
    /**
     * 格式化文件大小
     * @param size 文件大小（字节）
     * @return 格式化后的文件大小字符串，如：1.5 MB
     */
    fun formatFileSize(size: Long): String {
        if (size <= 0) return "0 B"

        val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        
        return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }
    
    /**
     * 获取文件后缀名
     * @param fileName 文件名
     * @return 后缀名，如果没有后缀则返回空字符串
     */
    fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast('.', "")
    }
    
    /**
     * 从Uri获取文件信息
     * @param context 上下文
     * @param uri 文件Uri
     * @return 包含文件名、大小、MIME类型的Map
     */
    fun getFileInfoFromUri(context: Context, uri: Uri): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        
        val contentResolver = context.contentResolver
        
        // 获取文件名
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                
                if (nameIndex != -1) {
                    val fileName = cursor.getString(nameIndex)
                    result["name"] = fileName
                    // 提取并保存文件后缀
                    result["extension"] = getFileExtension(fileName)
                }
                
                if (sizeIndex != -1) {
                    val size = cursor.getLong(sizeIndex)
                    result["size"] = size
                    result["formattedSize"] = formatFileSize(size)
                }
            }
        }
        
        // 获取MIME类型
        val mimeType = getMimeType(context, uri)
        result["mimeType"] = mimeType ?: "未知类型"
        
        return result
    }
    
    /**
     * 获取文件的MIME类型
     */
    private fun getMimeType(context: Context, uri: Uri): String? {
        return if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            context.contentResolver.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.lowercase())
        }
    }
} 