package com.panjx.clouddrive.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.security.MessageDigest
import java.text.DecimalFormat

/**
 * 文件工具类
 * 提供文件大小格式化、文件类型判断等功能
 */
object FileUtils {
    
    // 每次用于计算哈希的缓冲区大小，8KB
    private const val HASH_BUFFER_SIZE = 8 * 1024
    
    // 每个分片的大小，默认为10MB
    private const val CHUNK_SIZE = 10 * 1024 * 1024L
    
    // 设置一个进度更新的间隔，比如每处理100MB数据更新一次
    private const val PROGRESS_UPDATE_INTERVAL = 100 * 1024 * 1024L

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
     * 计算哈希值并记录时间（在后台线程中执行）
     * @param context 上下文
     * @param uri 文件URI
     * @return 包含哈希值和计算时间的Map
     */
    suspend fun calculateFileHashesAsync(context: Context, uri: Uri): Map<String, Pair<String, Long>> = withContext(Dispatchers.IO) {
        val results = mutableMapOf<String, Pair<String, Long>>()
        
        try {
            // 获取文件大小
            var fileSize = 0L
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex != -1) {
                        fileSize = cursor.getLong(sizeIndex)
                    }
                }
            }
            
            // 并行计算多种哈希算法，每个都在自己的协程中执行
            val md5Job = async(Dispatchers.IO) {
                var startTime = System.currentTimeMillis()
                var hash = ""
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val digest = MessageDigest.getInstance("MD5")
                    val buffer = ByteArray(HASH_BUFFER_SIZE)
                    var bytesRead: Int
                    var totalBytesRead = 0L
                    var lastProgressUpdate = 0L
                    
                    while (inputStream.read(buffer).also { bytesRead = it } > 0) {
                        digest.update(buffer, 0, bytesRead)
                        
                        totalBytesRead += bytesRead
                        if (fileSize > 0 && totalBytesRead - lastProgressUpdate > PROGRESS_UPDATE_INTERVAL) {
                            Log.d("FileUtils", "MD5计算进度: ${(totalBytesRead * 100 / fileSize)}%")
                            lastProgressUpdate = totalBytesRead
                        }
                    }
                    
                    hash = digest.digest().joinToString("") { "%02x".format(it) }
                }
                val duration = System.currentTimeMillis() - startTime
                Pair(hash, duration)
            }
            
            val sha1Job = async(Dispatchers.IO) {
                var startTime = System.currentTimeMillis()
                var hash = ""
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val digest = MessageDigest.getInstance("SHA-1")
                    val buffer = ByteArray(HASH_BUFFER_SIZE)
                    var bytesRead: Int
                    var totalBytesRead = 0L
                    var lastProgressUpdate = 0L
                    
                    while (inputStream.read(buffer).also { bytesRead = it } > 0) {
                        digest.update(buffer, 0, bytesRead)
                        
                        totalBytesRead += bytesRead
                        if (fileSize > 0 && totalBytesRead - lastProgressUpdate > PROGRESS_UPDATE_INTERVAL) {
                            Log.d("FileUtils", "SHA-1计算进度: ${(totalBytesRead * 100 / fileSize)}%")
                            lastProgressUpdate = totalBytesRead
                        }
                    }
                    
                    hash = digest.digest().joinToString("") { "%02x".format(it) }
                }
                val duration = System.currentTimeMillis() - startTime
                Pair(hash, duration)
            }
            
            val sha256Job = async(Dispatchers.IO) {
                var startTime = System.currentTimeMillis()
                var hash = ""
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val digest = MessageDigest.getInstance("SHA-256")
                    val buffer = ByteArray(HASH_BUFFER_SIZE)
                    var bytesRead: Int
                    var totalBytesRead = 0L
                    var lastProgressUpdate = 0L
                    
                    while (inputStream.read(buffer).also { bytesRead = it } > 0) {
                        digest.update(buffer, 0, bytesRead)
                        
                        totalBytesRead += bytesRead
                        if (fileSize > 0 && totalBytesRead - lastProgressUpdate > PROGRESS_UPDATE_INTERVAL) {
                            Log.d("FileUtils", "SHA-256计算进度: ${(totalBytesRead * 100 / fileSize)}%")
                            lastProgressUpdate = totalBytesRead
                        }
                    }
                    
                    hash = digest.digest().joinToString("") { "%02x".format(it) }
                }
                val duration = System.currentTimeMillis() - startTime
                Pair(hash, duration)
            }
            
            // 等待所有哈希计算完成并收集结果
            val startTime = System.currentTimeMillis()
            val md5Result = md5Job.await()
            val sha1Result = sha1Job.await()
            val sha256Result = sha256Job.await()
            val totalDuration = System.currentTimeMillis() - startTime
            
            results["md5"] = md5Result
            results["sha1"] = sha1Result 
            results["sha256"] = sha256Result
            
            Log.d("FileUtils", "文件大小: ${formatFileSize(fileSize)}, 总耗时: ${totalDuration}ms, MD5: ${md5Result.second}ms, SHA-1: ${sha1Result.second}ms, SHA-256: ${sha256Result.second}ms")
            
        } catch (e: Exception) {
            Log.e("FileUtils", "计算哈希值时出错: ${e.message}")
            e.printStackTrace()
            results["error"] = Pair(e.message ?: "未知错误", 0)
        }
        
        return@withContext results
    }
    
    /**
     * 计算指定算法的哈希值（供旧代码调用）
     * @param inputStream 输入流
     * @param algorithm 算法名称（MD5, SHA-1, SHA-256等）
     * @return 哈希值和计算时间的Pair
     */
    private fun calculateHash(inputStream: InputStream, algorithm: String): Pair<String, Long> {
        val startTime = System.currentTimeMillis()
        
        val digest = MessageDigest.getInstance(algorithm)
        val buffer = ByteArray(HASH_BUFFER_SIZE)
        var read: Int
        
        while (inputStream.read(buffer).also { read = it } > 0) {
            digest.update(buffer, 0, read)
        }
        
        val hashBytes = digest.digest()
        val hashString = hashBytes.joinToString("") { "%02x".format(it) }
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        return Pair(hashString, duration)
    }
    
    /**
     * 从Uri获取文件信息（不包括哈希值）
     * 哈希值需要异步计算
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
        
        // 保存URI，用于后续计算哈希值
        result["uri"] = uri
        
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