package com.panjx.clouddrive.util

// 为了支持Keccak-256，引入Bouncy Castle库
// 添加Tika相关导入
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.apache.tika.Tika
import java.io.BufferedInputStream
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
    
    // 初始化Tika
    private val tika = Tika()

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
            
            Log.d("FileUtils", "开始计算文件哈希，文件大小: ${formatFileSize(fileSize)}")
            
            // 并行计算多种哈希算法，每个都在自己的协程中执行
            val md5Job = async(Dispatchers.IO) {
                Log.d("FileUtils", "开始计算MD5哈希")
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
                            val progress = (totalBytesRead * 100 / fileSize)
                            Log.d("FileUtils", "MD5计算进度: $progress%, 已处理: ${formatFileSize(totalBytesRead)}")
                            lastProgressUpdate = totalBytesRead
                        }
                    }
                    
                    hash = digest.digest().joinToString("") { "%02x".format(it) }
                }
                val duration = System.currentTimeMillis() - startTime
                Log.d("FileUtils", "MD5计算完成: $hash, 耗时: $duration ms")
                Pair(hash, duration)
            }
            
            val sha1Job = async(Dispatchers.IO) {
                Log.d("FileUtils", "开始计算SHA-1哈希")
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
                            val progress = (totalBytesRead * 100 / fileSize)
                            Log.d("FileUtils", "SHA-1计算进度: $progress%, 已处理: ${formatFileSize(totalBytesRead)}")
                            lastProgressUpdate = totalBytesRead
                        }
                    }
                    
                    hash = digest.digest().joinToString("") { "%02x".format(it) }
                }
                val duration = System.currentTimeMillis() - startTime
                Log.d("FileUtils", "SHA-1计算完成: $hash, 耗时: $duration ms")
                Pair(hash, duration)
            }
            
            val sha256Job = async(Dispatchers.IO) {
                Log.d("FileUtils", "开始计算SHA-256哈希")
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
                            val progress = (totalBytesRead * 100 / fileSize)
                            Log.d("FileUtils", "SHA-256计算进度: $progress%, 已处理: ${formatFileSize(totalBytesRead)}")
                            lastProgressUpdate = totalBytesRead
                        }
                    }
                    
                    hash = digest.digest().joinToString("") { "%02x".format(it) }
                }
                val duration = System.currentTimeMillis() - startTime
                Log.d("FileUtils", "SHA-256计算完成: $hash, 耗时: $duration ms")
                Pair(hash, duration)
            }
            
//            // 添加SHA-512哈希计算
//            val sha512Job = async(Dispatchers.IO) {
//                var startTime = System.currentTimeMillis()
//                var hash = ""
//                context.contentResolver.openInputStream(uri)?.use { inputStream ->
//                    val digest = MessageDigest.getInstance("SHA-512")
//                    val buffer = ByteArray(HASH_BUFFER_SIZE)
//                    var bytesRead: Int
//                    var totalBytesRead = 0L
//                    var lastProgressUpdate = 0L
//
//                    while (inputStream.read(buffer).also { bytesRead = it } > 0) {
//                        digest.update(buffer, 0, bytesRead)
//
//                        totalBytesRead += bytesRead
//                        if (fileSize > 0 && totalBytesRead - lastProgressUpdate > PROGRESS_UPDATE_INTERVAL) {
//                            Log.d("FileUtils", "SHA-512计算进度: ${(totalBytesRead * 100 / fileSize)}%")
//                            lastProgressUpdate = totalBytesRead
//                        }
//                    }
//
//                    hash = digest.digest().joinToString("") { "%02x".format(it) }
//                }
//                val duration = System.currentTimeMillis() - startTime
//                Pair(hash, duration)
//            }
//
//            // 添加Keccak-256哈希计算（以太坊使用的哈希算法）
//            val keccak256Job = async(Dispatchers.IO) {
//                var startTime = System.currentTimeMillis()
//                var hash = ""
//                context.contentResolver.openInputStream(uri)?.use { inputStream ->
//                    val digest = Keccak.Digest256()
//                    val buffer = ByteArray(HASH_BUFFER_SIZE)
//                    var bytesRead: Int
//                    var totalBytesRead = 0L
//                    var lastProgressUpdate = 0L
//
//                    while (inputStream.read(buffer).also { bytesRead = it } > 0) {
//                        digest.update(buffer, 0, bytesRead)
//
//                        totalBytesRead += bytesRead
//                        if (fileSize > 0 && totalBytesRead - lastProgressUpdate > PROGRESS_UPDATE_INTERVAL) {
//                            Log.d("FileUtils", "Keccak-256计算进度: ${(totalBytesRead * 100 / fileSize)}%")
//                            lastProgressUpdate = totalBytesRead
//                        }
//                    }
//
//                    hash = digest.digest().joinToString("") { "%02x".format(it) }
//                }
//                val duration = System.currentTimeMillis() - startTime
//                Pair(hash, duration)
//            }
            
            // 等待所有哈希计算完成并收集结果
            val startTime = System.currentTimeMillis()
            Log.d("FileUtils", "等待所有哈希计算任务完成...")
            
            val md5Result = md5Job.await()
            Log.d("FileUtils", "MD5计算任务已完成")
            
            val sha1Result = sha1Job.await()
            Log.d("FileUtils", "SHA-1计算任务已完成")
            
            val sha256Result = sha256Job.await()
            Log.d("FileUtils", "SHA-256计算任务已完成")
            
//            val sha512Result = sha512Job.await()
//            Log.d("FileUtils", "SHA-512计算任务已完成")
//
//            val keccak256Result = keccak256Job.await()
//            Log.d("FileUtils", "Keccak-256计算任务已完成")
            
            val totalDuration = System.currentTimeMillis() - startTime
            
            results["md5"] = md5Result
            results["sha1"] = sha1Result 
            results["sha256"] = sha256Result
//            results["sha512"] = sha512Result
//            results["keccak256"] = keccak256Result
            
            Log.d("FileUtils", "所有哈希计算完成，总耗时: ${totalDuration}ms")
            Log.d("FileUtils", "MD5: ${md5Result.first.take(8)}..., 耗时: ${md5Result.second}ms")
            Log.d("FileUtils", "SHA-1: ${sha1Result.first.take(8)}..., 耗时: ${sha1Result.second}ms") 
            Log.d("FileUtils", "SHA-256: ${sha256Result.first.take(8)}..., 耗时: ${sha256Result.second}ms")
//            Log.d("FileUtils", "SHA-512: ${sha512Result.first.take(8)}..., 耗时: ${sha512Result.second}ms")
//            Log.d("FileUtils", "Keccak-256: ${keccak256Result.first.take(8)}..., 耗时: ${keccak256Result.second}ms")
            
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
     * 获取文件信息（不包括哈希值）
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
        
        // 获取基于Tika的MIME类型（通过文件头字节识别）
        val tikaMimeType = getMimeType(context, uri)
        result["mimeType"] = tikaMimeType ?: "未知类型"
        
        // 获取基于文件扩展名的MIME类型
        val fileName = result["name"] as? String ?: ""
        val extensionMimeType = getMimeTypeFromExtension(fileName)
        result["extensionMimeType"] = extensionMimeType ?: "未知类型"
        
        // 保存URI，用于后续计算哈希值
        result["uri"] = uri
        
        return result
    }
    
    /**
     * 通过文件扩展名获取MIME类型
     * @param fileName 文件名
     * @return MIME类型字符串，如果无法识别则返回null
     */
    private fun getMimeTypeFromExtension(fileName: String): String? {
        val extension = getFileExtension(fileName).lowercase()
        if (extension.isEmpty()) return null
        
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }
    
    /**
     * 获取文件的MIME类型
     * 首先尝试使用Tika通过文件头进行判断
     * 如果失败则回退到使用ContentResolver和MimeTypeMap
     */
    private fun getMimeType(context: Context, uri: Uri): String? {
        try {
            // 首先尝试使用Tika通过文件头判断MIME类型
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bufferedInput = BufferedInputStream(inputStream)
                // Tika仅需要读取文件开头一小部分来判断类型
                return tika.detect(bufferedInput, uri.lastPathSegment ?: "")
            }
        } catch (e: Exception) {
            Log.e("FileUtils", "使用Tika检测文件类型失败: ${e.message}")
            // 出现异常，使用回退方法
        }
        
        // 回退方法：使用系统API
        return if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            context.contentResolver.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.lowercase())
        }
    }
} 