package com.panjx.clouddrive.util

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.qiniu.android.common.AutoZone
import com.qiniu.android.storage.Configuration
import com.qiniu.android.storage.FileRecorder
import com.qiniu.android.storage.UpCompletionHandler
import com.qiniu.android.storage.UploadManager
import com.qiniu.android.storage.UploadOptions
import com.qiniu.android.utils.Utils
import java.io.IOException

/**
 * 七牛云上传管理工具类
 */
class KodoUtils {
    companion object {
        const val TAG = "QiniuUpload"
        // 添加静态uploadManager，确保在应用生命周期内只创建一次
        private var staticUploadManager: UploadManager? = null
    }
    
    // 使用companion object的静态uploadManager
    private val uploadManager: UploadManager?
        get() = staticUploadManager
    
    /**
     * 初始化七牛云上传管理器
     * @return 是否初始化成功
     */
    fun init(): Boolean {
        // 如果已经初始化，直接返回
        if (staticUploadManager != null) {
            Log.d(TAG, "七牛云上传管理器已经初始化")
            return true
        }
        
        Log.d(TAG, "初始化七牛云上传管理器...")
        try {
            // 创建文件记录器用于断点续传
            var recorder: FileRecorder? = null
            try {
                // 使用绝对路径创建recorder目录
                val recorderPath = "${Utils.sdkDirectory()}/recorder"
                val recorderDir = java.io.File(recorderPath)
                if (!recorderDir.exists()) {
                    val created = recorderDir.mkdirs()
                    Log.d(TAG, "创建断点续传记录目录: $recorderPath, 创建结果: $created")
                }
                
                recorder = FileRecorder(recorderPath)
                Log.d(TAG, "断点续传记录器路径: $recorderPath")
                // 确保记录器创建成功
                if (recorder != null) {
                    Log.d(TAG, "断点续传记录器创建成功")
                } else {
                    Log.e(TAG, "警告: 断点续传记录器为空")
                }
            } catch (e: IOException) {
                Log.e(TAG, "创建断点续传记录器失败", e)
                e.printStackTrace()
            }

            // 配置上传区域
            val zone = AutoZone()
            Log.d(TAG, "使用自动区域配置")

            // 分片大小
            val chunkSize = 128 * 1024 // 128KB

            // 创建配置
            val configuration = Configuration.Builder()
                .zone(zone)                          // 配置上传区域
                .putThreshold(chunkSize)             // 分片上传阈值：128KB
                .useConcurrentResumeUpload(true)     // 开启分片上传
                .recorder(recorder)                  // 文件分片上传时断点续传信息保存
                .resumeUploadVersion(Configuration.RESUME_UPLOAD_VERSION_V2) // 使用分片 V2
                .connectTimeout(90)                  // 设置连接超时，单位秒
                .responseTimeout(90)                 // 设置响应超时，单位秒
                .useHttps(true)                      // 使用HTTPS (默认值，推荐)
                .build()
                
            Log.d(TAG, "七牛云配置完成: 分片阈值=${chunkSize}字节, 启用并发上传")
            
            // 创建上传管理器
            staticUploadManager = UploadManager(configuration)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "初始化七牛云上传管理器失败", e)
            return false
        }
    }
    
    /**
     * 上传文件到七牛云
     * @param context 上下文
     * @param uri 文件Uri
     * @param token 上传Token，必须提供
     * @param key 指定的文件名，如果为null则由七牛云自动生成。注意：要支持断点续传必须提供一个固定的key
     * @param onProgress 上传进度回调
     * @param onComplete 上传完成回调
     * @param onCancelled 取消检查回调，返回true表示需要取消上传
     */
    fun uploadFile(
        context: Context,
        uri: Uri,
        token: String,
        key: String? = null,
        onProgress: (Double) -> Unit,
        onComplete: (Boolean, String) -> Unit,
        onCancelled: () -> Boolean = { false } // 默认不取消
    ) {
        if (uploadManager == null) {
            if (!init()) {
                onComplete(false, "初始化七牛云上传管理器失败")
                return
            }
        }
        
        // 获取文件路径
        val filePath = getFilePathFromUri(context, uri)
        if (filePath == null) {
            Log.e(TAG, "无法获取文件路径，URI: $uri")
            onComplete(false, "无法获取文件路径")
            return
        }
        Log.d(TAG, "文件路径: $filePath")
        
        // 上传选项
        val options = UploadOptions(null, null, true,
            { _, percent ->
                // 更新上传进度
                onProgress(percent)
                // 仅在进度变化显著时记录日志（每10%记录一次）
                if ((percent * 10).toInt() % 1 == 0) {
                    Log.v(TAG, "上传进度: ${String.format("%.1f", percent * 100)}%")
                }
            },
            {
                // 是否取消上传，通过回调检查
                val shouldCancel = onCancelled()
                if (shouldCancel) {
                    Log.d(TAG, "上传操作被取消，将保存断点续传记录")
                    // 为了确保断点记录被保存，这里做一些操作
                    try {
                        /*val recorderPath = "${Utils.sdkDirectory()}/recorder"
                        val recorderDir = java.io.File(recorderPath)
                        if (recorderDir.exists()) {
                            Log.d(TAG, "断点续传目录存在: $recorderPath")
                            if (key != null) {
                                // 检查是否已有断点记录文件
                                val files = recorderDir.listFiles()
                                if (files != null) {
                                    var foundRecord = false
                                    for (file in files) {
                                        if (file.name.contains(key)) {
                                            Log.d(TAG, "找到断点记录文件: ${file.name}, 大小: ${file.length()}")
                                            foundRecord = true
                                        }
                                    }
                                    if (!foundRecord) {
                                        Log.w(TAG, "未找到断点记录文件，可能是取消得太快或断点续传未启用")
                                    }
                                }
                            }
                        }*/
                        
                        // 强制等待一些时间，确保断点记录文件被正确保存
                        Thread.sleep(500)
                        
                        // 额外检查，如果仍然需要取消，输出日志确认
                        if (onCancelled()) {
                            Log.d(TAG, "确认取消上传操作")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "检查断点记录文件时出错", e)
                    }
                }
                shouldCancel
            }
        )
        
        Log.d(TAG, "上传Token: $token")
        
        // 文件key (七牛云存储的文件名)
        if (key == null) {
            Log.w(TAG, "警告: 没有提供key参数，将由七牛自动生成。这会导致断点续传失效，暂停后无法恢复上传进度")
        } else {
            Log.d(TAG, "使用指定的key: $key")
            Log.d(TAG, "此key将用于断点续传标识，暂停后可恢复上传")
            
            // 检查是否存在断点续传记录
            val recorderPath = "${Utils.sdkDirectory()}/recorder"
            val recordFile = java.io.File(recorderPath)
            if (recordFile.exists()) {
                val files = recordFile.listFiles()
                if (files != null && files.isNotEmpty()) {
                    Log.d(TAG, "检测到断点续传记录目录，包含 ${files.size} 个文件")
                    for (file in files) {
                        if (file.name.contains(key)) {
                            Log.d(TAG, "找到当前上传任务的断点续传记录: ${file.name}, 大小: ${file.length()} 字节")
                        }
                    }
                } else {
                    Log.d(TAG, "断点续传记录目录存在，但没有文件")
                }
            } else {
                Log.d(TAG, "断点续传记录目录不存在: $recorderPath")
            }
        }
        Log.d(TAG, "开始上传文件: key=$key ${if(key == null) "(null表示由七牛自动生成)" else ""}")
        
        try {
            // 判断filePath是否是content URI字符串
            if (filePath.startsWith("content://")) {
                Log.d(TAG, "检测到content URI，使用七牛云SDK原生Uri上传方法")
                
                try {
                    // 使用SDK原生的Uri上传方法，支持断点续传
                    uploadManager!!.put(
                        uri,  // 直接传递Uri
                        context.contentResolver,  // 提供ContentResolver
                        key,  // 使用唯一标识作为key
                        token,
                        UpCompletionHandler { k, info, response ->
                            if (info != null && info.isOK) {
                                // 上传成功
                                Log.i(TAG, "上传成功: key=$k, responseInfo=${info.toString()}")
                                Log.d(TAG, "上传响应: $response")
                                val finalKey = if (response != null) {
                                    try {
                                        response.optString("key", k)
                                    } catch (e: Exception) {
                                        k
                                    }
                                } else {
                                    k
                                }
                                onComplete(true, "文件上传成功：$finalKey")
                            } else {
                                // 上传失败
                                val errorMsg = info?.error ?: "未知错误"
                                Log.e(TAG, "上传失败: key=$k, error=$errorMsg")
                                if (info != null) {
                                    Log.e(TAG, "响应码: ${info.statusCode}, 请求ID: ${info.reqId}")
                                }
                                if (response != null) {
                                    Log.e(TAG, "错误响应: $response")
                                }
                                onComplete(false, "上传失败：$errorMsg")
                            }
                        },
                        options
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "使用URI上传过程中发生异常", e)
                    onComplete(false, "上传异常：${e.message}")
                }
            } else {
                // 对于普通文件路径，直接使用文件路径上传
                Log.d(TAG, "使用文件路径上传: $filePath")
                
                // 执行上传
                uploadManager!!.put(filePath, key, token, 
                    UpCompletionHandler { k, info, response ->
                        if (info != null && info.isOK) {
                            // 上传成功
                            Log.i(TAG, "上传成功: key=$k, responseInfo=${info.toString()}")
                            Log.d(TAG, "上传响应: $response")
                            val finalKey = if (response != null) {
                                try {
                                    response.optString("key", k)
                                } catch (e: Exception) {
                                    k
                                }
                            } else {
                                k
                            }
                            onComplete(true, "文件上传成功：$finalKey")
                        } else {
                            // 上传失败
                            val errorMsg = info?.error ?: "未知错误"
                            Log.e(TAG, "上传失败: key=$k, error=$errorMsg")
                            if (info != null) {
                                Log.e(TAG, "响应码: ${info.statusCode}, 请求ID: ${info.reqId}")
                            }
                            if (response != null) {
                                Log.e(TAG, "错误响应: $response")
                            }
                            onComplete(false, "上传失败：$errorMsg")
                        }
                    }, 
                    options
                )
            }
            Log.d(TAG, "上传请求已发送")
        } catch (e: Exception) {
            Log.e(TAG, "发送上传请求时发生异常", e)
            onComplete(false, "上传异常：${e.message}")
        }
    }
    
    /**
     * 生成分层目录结构的文件key
     * 将SHA256哈希值分段，形成多级目录结构
     * @param sha256 文件的SHA256哈希值
     * @param prefix 可选的前缀目录，默认为"files"
     * @return 形如 files/526b6e29/94afa9ab/a33c02d4/c468a037/526b6e2994afa9aba33c02d4c468a03708feaaeb073b545e014a3d11633170ac0 的key
     */
    fun generateLayeredKey(sha256: String, prefix: String = "files"): String {
        // 确保SHA256哈希值正确
        if (sha256.length < 64) {
            Log.w(TAG, "SHA256哈希值长度不足: $sha256")
            // 如果哈希值有问题，返回一个带时间戳的替代路径
            val timestamp = System.currentTimeMillis()
            return "$prefix/unknown_${timestamp}"
        }
        
        // 获取前8个字符作为一级目录
        val firstDir = sha256.substring(0, 8)
        // 获取次8个字符作为二级目录
        val secondDir = sha256.substring(8, 16)
        // 获取再次8个字符作为三级目录
        val thirdDir = sha256.substring(16, 24)
        // 获取再次8个字符作为四级目录
        val fourthDir = sha256.substring(24, 32)
        
        // 构建完整目录结构
        return "$prefix/$firstDir/$secondDir/$thirdDir/$fourthDir/$sha256"
    }
    
    /**
     * 从Uri获取文件名
     */
    fun getFileName(context: Context, uri: Uri): String {
        Log.v(TAG, "开始获取文件名, URI: $uri")
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                    if (columnIndex != -1) {
                        result = it.getString(columnIndex)
                        Log.v(TAG, "从内容提供者获取文件名: $result")
                    } else {
                        Log.w(TAG, "无法从内容提供者获取DISPLAY_NAME列")
                    }
                } else {
                    Log.w(TAG, "内容提供者游标为空")
                }
            }
        }
        if (result == null) {
            result = uri.path
            Log.v(TAG, "使用URI路径作为文件名: $result")
            val cut = result?.lastIndexOf('/')
            if (cut != -1 && cut != null) {
                result = result?.substring(cut + 1)
                Log.v(TAG, "从路径提取文件名: $result")
            }
        }
        val fileName = result ?: "unknown_file"
        Log.d(TAG, "最终文件名: $fileName")
        return fileName
    }

    /**
     * 从Uri获取文件路径
     */
    private fun getFilePathFromUri(context: Context, uri: Uri): String? {
        Log.v(TAG, "开始获取文件路径, URI: $uri")
        
        // 对于file://开头的URI，直接返回路径
        if (uri.scheme == "file") {
            val path = uri.path
            Log.d(TAG, "文件URI，直接返回路径: $path")
            return path
        }
        
        // 对于content://开头的URI，我们直接返回原始URI字符串
        // 而不是创建临时文件
        if (uri.scheme == "content") {
            Log.d(TAG, "内容URI，返回URI字符串: $uri")
            return uri.toString()
        }
        
        // 如果都不是，尝试返回URI路径
        Log.d(TAG, "使用URI路径: ${uri.path}")
        return uri.path
    }

    /**
     * 删除指定key的断点续传记录
     * @param key 上传使用的key
     * @return 是否删除成功
     */
    fun deleteUploadRecord(key: String): Boolean {
        try {
            Log.d(TAG, "================ 开始删除断点续传记录 ================")
            Log.d(TAG, "需要删除的记录key: $key")
            val recorderPath = "${Utils.sdkDirectory()}/recorder"
            Log.d(TAG, "断点续传记录目录: $recorderPath")
            
            // 记录删除前的文件列表
            val recordDirBefore = java.io.File(recorderPath)
            if (recordDirBefore.exists()) {
                val filesBefore = recordDirBefore.listFiles()
                if (filesBefore != null) {
                    Log.d(TAG, "删除前的文件列表 (${filesBefore.size} 个文件):")
                    filesBefore.forEachIndexed { index, file ->
                        Log.d(TAG, "$index. ${file.name} (${file.length()} 字节)")
                        if (file.name.contains(key)) {
                            Log.d(TAG, "✓ 找到目标文件: ${file.name}, 大小: ${file.length()} 字节")
                        }
                    }
                } else {
                    Log.d(TAG, "删除前文件列表为空或无法读取")
                }
            } else {
                Log.d(TAG, "记录目录不存在: $recorderPath")
                return false
            }
            
            // 调用SDK删除方法
            Log.d(TAG, "调用SDK的断点记录删除方法: fileRecorder.del($key)")
            val fileRecorder = FileRecorder(recorderPath)
            fileRecorder.del(key)
            Log.d(TAG, "SDK删除方法调用完成")
            
            // 记录删除后的文件列表并验证是否成功删除
            val recordDirAfter = java.io.File(recorderPath)
            if (recordDirAfter.exists()) {
                val filesAfter = recordDirAfter.listFiles()
                if (filesAfter != null) {
                    Log.d(TAG, "删除后的文件列表 (${filesAfter.size} 个文件):")
                    
                    var targetFileStillExists = false
                    var targetFilePath = ""
                    
                    filesAfter.forEachIndexed { index, file ->
                        Log.d(TAG, "$index. ${file.name} (${file.length()} 字节)")
                        if (file.name.contains(key)) {
                            targetFileStillExists = true
                            targetFilePath = file.absolutePath
                            Log.w(TAG, "! 目标文件仍然存在: ${file.name}, 大小: ${file.length()} 字节")
                        }
                    }
                    
                    if (targetFileStillExists) {
                        Log.w(TAG, "断点续传记录未被SDK方法完全删除")
                        
                        // 检查文件是否可读写
                        val targetFile = java.io.File(targetFilePath)
                        Log.d(TAG, "文件权限检查: 可读=${targetFile.canRead()}, 可写=${targetFile.canWrite()}, 可执行=${targetFile.canExecute()}")
                        
                        // 尝试使用文件系统直接删除
                        Log.d(TAG, "尝试使用文件系统直接删除: ${targetFile.name}")
                        val deleted = targetFile.delete()
                        Log.d(TAG, "文件系统删除结果: $deleted")
                        
                        // 再次验证删除结果
                        if (deleted) {
                            Log.d(TAG, "文件已通过文件系统方法成功删除")
                        } else {
                            Log.e(TAG, "文件系统方法删除失败，尝试调用deleteOnExit")
                            targetFile.deleteOnExit()
                            
                            // 检查文件是否仍然存在
                            val stillExists = targetFile.exists()
                            Log.d(TAG, "文件在deleteOnExit后仍然存在: $stillExists")
                        }
                        
                        return deleted
                    } else {
                        Log.d(TAG, "目标文件已成功删除，未在文件列表中找到")
                    }
                } else {
                    Log.d(TAG, "删除后文件列表为空或无法读取")
                }
            }
            
            Log.d(TAG, "断点续传记录已成功删除")
            Log.d(TAG, "================ 删除断点续传记录完成 ================")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "删除断点续传记录时出错", e)
            Log.e(TAG, "异常类型: ${e.javaClass.simpleName}")
            Log.e(TAG, "异常信息: ${e.message}")
            e.printStackTrace()
            return false
        }
    }
}
