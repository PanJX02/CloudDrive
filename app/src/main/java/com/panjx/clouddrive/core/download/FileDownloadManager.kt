package com.panjx.clouddrive.core.download

import android.content.Context
import android.os.Environment
import android.util.Log
import com.panjx.clouddrive.core.modle.response.DownloadFileInfo
import com.panjx.clouddrive.core.modle.response.DownloadResponse
import com.panjx.clouddrive.core.network.di.TokenRefreshOkHttpClient
import com.panjx.clouddrive.data.database.TransferEntity
import com.panjx.clouddrive.data.database.TransferType
import com.panjx.clouddrive.data.repository.TransferRepository
import com.panjx.clouddrive.feature.transfersRoute.TransferStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 文件下载管理器
 * 处理文件下载的核心逻辑
 */
@Singleton
class FileDownloadManager @Inject constructor(
    private val transferRepository: TransferRepository,
    @TokenRefreshOkHttpClient private val okHttpClient: OkHttpClient
) {
    private val TAG = "FileDownloadManager"
    
    // 当前活跃的下载任务
    private val activeDownloadTasks = ConcurrentHashMap<Long, Boolean>()
    
    // 下载进度更新
    val downloadProgress = MutableStateFlow<Map<Long, Int>>(emptyMap())
    
    // 获取应用下载目录的基础路径
    private fun getBaseDownloadPath(context: Context): String {
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val cloudDriveDir = File(downloadDir, "clouddrive")
        if (!cloudDriveDir.exists()) {
            cloudDriveDir.mkdirs()
        }
        return cloudDriveDir.absolutePath
    }
    
    /**
     * 检查是否有存储权限
     */
    fun hasStoragePermission(context: Context): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // Android 11+使用Environment.isExternalStorageManager()
            Environment.isExternalStorageManager()
        } else {
            // Android 10及以下使用传统权限
            context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == 
                android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * 添加文件夹下载任务
     */
    suspend fun addFolderDownloadTask(
        fileId: Long,
        downloadResponse: DownloadResponse,
        context: Context
    ) {
        Log.d(TAG, "添加文件夹下载任务: fileId=$fileId")
        
        // 处理文件夹内的文件
        downloadResponse.downloadFiles.forEach { fileInfo ->
            if (fileInfo.folderType) {
                // 创建文件夹
                createLocalFolder(context, fileInfo.filePath, fileInfo.fileName)
            } else {
                // 添加文件下载任务
                val taskId = addFileDownloadTask(
                    fileInfo = fileInfo,
                    context = context
                )
                
                // 如果URL不为空，立即开始下载
                if (!fileInfo.url.isNullOrEmpty()) {
                    startDownload(taskId, context)
                }
            }
        }
    }
    
    /**
     * 添加单文件下载任务
     */
    suspend fun addFileDownloadTask(
        fileInfo: DownloadFileInfo,
        context: Context
    ): Long {
        Log.d(TAG, "================ 添加单文件下载任务 ================")
        Log.d(TAG, "添加文件下载任务: ${fileInfo.fileName}")
        Log.d(TAG, "文件URL (前50字符): ${fileInfo.url?.take(50)}")
        Log.d(TAG, "文件路径: ${fileInfo.filePath}")
        Log.d(TAG, "文件大小: ${fileInfo.size}")
        Log.d(TAG, "是否文件夹: ${fileInfo.folderType}")
        
        // 计算本地文件路径
        val localFilePath = getLocalFilePath(context, fileInfo)
        Log.d(TAG, "本地保存路径: $localFilePath")
        
        // 创建目录
        createDirectoryForFile(localFilePath)
        Log.d(TAG, "目录已创建")
        
        // 添加到数据库
        val taskId = transferRepository.addTransfer(
            fileName = fileInfo.fileName,
            progress = 0,
            status = TransferStatus.WAITING,
            type = TransferType.DOWNLOAD,
            filePath = localFilePath,
            remoteUrl = fileInfo.url ?: "",
            fileSize = fileInfo.size ?: 0,
            folderType = if (fileInfo.folderType) 1 else 0,
            fileExtension = fileInfo.fileExtension,
            fileSHA256 = fileInfo.sha256
        )
        
        Log.d(TAG, "数据库记录已创建，任务ID: $taskId")
        return taskId
    }
    
    /**
     * 获取本地文件保存路径
     */
    private fun getLocalFilePath(context: Context, fileInfo: DownloadFileInfo): String {
        val baseDir = getBaseDownloadPath(context)
        
        // 获取服务器返回的路径（不包含文件名）
        val dirPath = fileInfo.filePath.trim('/')
        
        // 构建文件名（包含扩展名）
        val fileName = if (fileInfo.fileExtension.isNullOrEmpty()) {
            fileInfo.fileName
        } else {
            "${fileInfo.fileName}.${fileInfo.fileExtension}"
        }
        
        // 组合完整的本地文件路径
        val localDirPath = if (dirPath.isEmpty()) {
            baseDir
        } else {
            "$baseDir/$dirPath"
        }
        
        // 确保目录存在
        val dir = File(localDirPath)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        
        // 返回包含文件名的完整路径
        val fullPath = "$localDirPath/$fileName"
        Log.d(TAG, "构建本地文件路径: 基础目录=$baseDir, 服务器路径=$dirPath, 文件名=$fileName, 完整路径=$fullPath")
        
        return fullPath
    }
    
    /**
     * 创建目录结构
     */
    private fun createDirectoryForFile(filePath: String) {
        val file = File(filePath)
        val parentDir = file.parentFile
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs()
        }
    }
    
    /**
     * 创建本地文件夹
     */
    private fun createLocalFolder(context: Context, filePath: String, folderName: String) {
        val baseDir = getBaseDownloadPath(context)
        val dirPath = filePath.trim('/') // 去除前后斜杠
        
        // 构建完整的文件夹路径
        val localDirPath = if (dirPath.isEmpty()) {
            // 如果没有路径，直接放在根目录
            "$baseDir/$folderName"
        } else {
            // 如果有路径，拼接路径和文件夹名
            "$baseDir/$dirPath/$folderName"
        }
        
        // 创建文件夹
        val folder = File(localDirPath)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        
        Log.d(TAG, "创建本地文件夹: 路径=$localDirPath, 是否成功=${folder.exists()}")
    }
    
    /**
     * 开始下载
     */
    suspend fun startDownload(taskId: Long, context: Context) {
        Log.d(TAG, "================ 开始下载任务 ================")
        Log.d(TAG, "开始下载任务: $taskId")
        
        // 从数据库获取任务
        val task = transferRepository.getTransferById(taskId)
        if (task == null) {
            Log.e(TAG, "找不到下载任务: $taskId")
            return
        }
        
        Log.d(TAG, "获取到任务信息: 文件名=${task.fileName}, 本地路径=${task.filePath}")
        
        // 如果没有URL，无法下载
        if (task.remoteUrl.isEmpty()) {
            Log.e(TAG, "下载URL为空: $taskId")
            transferRepository.updateTransfer(
                task.copy(status = TransferStatus.FAILED)
            )
            return
        }
        
        // 检查并修复无效的文件路径
        val baseDir = getBaseDownloadPath(context)
        var updatedTask = task
        
        if (!task.filePath.startsWith(baseDir)) {
            Log.w(TAG, "检测到无效的文件路径: ${task.filePath}，修正为应用可写目录")
            
            // 提取文件名
            val fileName = task.filePath.substringAfterLast('/')
            val newFilePath = "$baseDir/$fileName"
            
            Log.d(TAG, "新文件路径: $newFilePath")
            
            // 更新任务信息
            updatedTask = task.copy(filePath = newFilePath)
            transferRepository.updateTransfer(updatedTask)
        }
        
        Log.d(TAG, "下载URL (前50字符): ${updatedTask.remoteUrl.take(50)}")
        
        // 设置取消标志
        activeDownloadTasks[taskId] = false
        
        // 更新状态为进行中
        transferRepository.updateTransfer(
            updatedTask.copy(status = TransferStatus.IN_PROGRESS)
        )
        
        try {
            Log.d(TAG, "开始实际下载过程...")
            withContext(Dispatchers.IO) {
                downloadFile(updatedTask, context)
            }
        } catch (e: CancellationException) {
            Log.d(TAG, "下载任务被取消: $taskId")
            // 协程被取消，不做额外处理
        } catch (e: Exception) {
            Log.e(TAG, "下载异常: ${e.message}", e)
            // 更新状态为失败
            transferRepository.updateTransfer(
                updatedTask.copy(status = TransferStatus.FAILED)
            )
        } finally {
            // 移除活跃任务
            activeDownloadTasks.remove(taskId)
            Log.d(TAG, "下载任务结束: $taskId")
        }
    }
    
    /**
     * 下载文件
     */
    private suspend fun downloadFile(task: TransferEntity, context: Context) {
        Log.d(TAG, "================ 开始文件下载过程 ================")
        Log.d(TAG, "文件名: ${task.fileName}")
        Log.d(TAG, "URL (前50字符): ${task.remoteUrl.take(50)}")
        Log.d(TAG, "保存路径: ${task.filePath}")
        
        // 检查文件路径，确保目标路径在应用可写目录中
        val baseDir = getBaseDownloadPath(context)
        if (!task.filePath.startsWith(baseDir)) {
            Log.e(TAG, "无效的文件路径，不在应用可写目录中: ${task.filePath}")
            // 更新状态为失败
            transferRepository.updateTransfer(
                task.copy(status = TransferStatus.FAILED)
            )
            throw IOException("无效的文件路径，不在应用可写目录中: ${task.filePath}")
        }
        
        val file = File(task.filePath)
        val tempFile = File("${task.filePath}.tmp")
        
        // 确保父目录存在且可写
        val parentDir = tempFile.parentFile
        if (parentDir != null && !parentDir.exists()) {
            val mkdirSuccess = parentDir.mkdirs()
            Log.d(TAG, "创建父目录: ${parentDir.absolutePath}, 结果: $mkdirSuccess")
            
            if (!mkdirSuccess) {
                Log.e(TAG, "无法创建目录: ${parentDir.absolutePath}")
                transferRepository.updateTransfer(
                    task.copy(status = TransferStatus.FAILED)
                )
                throw IOException("无法创建目录: ${parentDir.absolutePath}")
            }
        }
        
        // 检查目录是否可写
        if (parentDir != null && (!parentDir.exists() || !parentDir.canWrite())) {
            Log.e(TAG, "目录不可写: ${parentDir.absolutePath}")
            transferRepository.updateTransfer(
                task.copy(status = TransferStatus.FAILED)
            )
            throw IOException("目录不可写: ${parentDir.absolutePath}")
        }
        
        // 获取已下载的字节数（用于断点续传）
        var downloadedBytes = if (tempFile.exists()) tempFile.length() else 0L
        Log.d(TAG, "临时文件已存在: ${tempFile.exists()}, 已下载字节: $downloadedBytes")
        
        try {
            // 创建请求
            val request = Request.Builder()
                .url(task.remoteUrl)
                .apply {
                    // 如果有已下载的部分，设置Range头
                    if (downloadedBytes > 0) {
                        header("Range", "bytes=$downloadedBytes-")
                        Log.d(TAG, "设置续传Range头: bytes=$downloadedBytes-")
                    }
                }
                .build()
            
            Log.d(TAG, "开始执行HTTP请求...")
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "HTTP请求失败: ${response.code}")
                    throw IOException("下载失败: ${response.code}")
                }
                
                Log.d(TAG, "HTTP请求成功: ${response.code}")
                
                // 获取文件总大小
                val contentLength = response.header("Content-Length")?.toLongOrNull() ?: 0L
                val totalBytes = if (downloadedBytes > 0) {
                    // 如果是断点续传，总大小是已下载+剩余大小
                    downloadedBytes + contentLength
                } else {
                    contentLength
                }
                
                Log.d(TAG, "Content-Length: $contentLength, 总字节数: $totalBytes")
                
                // 如果总大小有效，更新任务信息
                if (totalBytes > 0 && task.fileSize != totalBytes) {
                    Log.d(TAG, "更新文件大小: ${task.fileSize} -> $totalBytes")
                    transferRepository.updateTransfer(
                        task.copy(fileSize = totalBytes)
                    )
                }
                
                // 打开输出流，追加模式
                Log.d(TAG, "开始下载数据流...")
                FileOutputStream(tempFile, downloadedBytes > 0).use { outputStream ->
                    response.body?.let { body ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        val inputStream = body.byteStream()
                        
                        var bytesRead: Int
                        var lastProgressUpdateTime = System.currentTimeMillis()
                        
                        // 读取数据
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            // 检查是否取消
                            if (activeDownloadTasks[task.id] == true) {
                                Log.d(TAG, "检测到取消标志，中断下载")
                                throw CancellationException("下载被取消")
                            }
                            
                            // 写入文件
                            outputStream.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead
                            
                            // 计算进度
                            val progress = if (totalBytes > 0) {
                                ((downloadedBytes * 100) / totalBytes).toInt()
                            } else {
                                0
                            }
                            
                            // 每500毫秒更新一次进度
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastProgressUpdateTime >= 500) {
                                updateDownloadProgress(task.id, progress)
                                lastProgressUpdateTime = currentTime
                                
                                if (progress % 10 == 0) {
                                    Log.d(TAG, "下载进度: $progress%, 已下载: ${downloadedBytes/1024}KB / ${totalBytes/1024}KB")
                                }
                            }
                        }
                        
                        // 最后更新一次进度
                        updateDownloadProgress(task.id, 100)
                        Log.d(TAG, "数据流下载完成，已写入临时文件")
                    }
                }
                
                // 下载完成后，重命名文件
                if (tempFile.exists()) {
                    if (file.exists()) {
                        Log.d(TAG, "目标文件已存在，删除旧文件")
                        file.delete()
                    }
                    val renamed = tempFile.renameTo(file)
                    Log.d(TAG, "临时文件重命名为正式文件: $renamed")
                } else {
                    Log.e(TAG, "临时文件不存在，无法完成下载")
                }
                
                // 更新状态为完成
                transferRepository.updateTransfer(
                    task.copy(status = TransferStatus.COMPLETED, progress = 100)
                )
                
                Log.d(TAG, "下载完成: ${task.fileName}")
            }
        } catch (e: CancellationException) {
            // 取消异常，传递给上层
            Log.d(TAG, "下载取消: ${e.message}")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "下载异常: ${e.message}", e)
            
            // 如果因为暂停而异常，不做处理
            if (task.status == TransferStatus.PAUSED) {
                // 已经是暂停状态，不修改
                Log.d(TAG, "下载已暂停，保留当前状态")
                return
            }
            
            // 其他异常，更新状态为失败
            transferRepository.updateTransfer(
                task.copy(status = TransferStatus.FAILED)
            )
            
            throw e
        }
    }
    
    /**
     * 更新下载进度
     */
    private suspend fun updateDownloadProgress(taskId: Long, progress: Int) {
        // 更新数据库中的进度
        val task = transferRepository.getTransferById(taskId) ?: return
        transferRepository.updateTransfer(task.copy(progress = progress))
        
        // 更新进度流
        val currentProgress = downloadProgress.value.toMutableMap()
        currentProgress[taskId] = progress
        downloadProgress.value = currentProgress
    }
    
    /**
     * 暂停下载
     */
    fun pauseDownload(taskId: Long) {
        Log.d(TAG, "暂停下载: $taskId")
        activeDownloadTasks[taskId] = true
    }
    
    /**
     * 取消下载
     */
    fun cancelDownload(taskId: Long) {
        Log.d(TAG, "取消下载: $taskId")
        activeDownloadTasks[taskId] = true
    }
    
    companion object {
        private const val DEFAULT_BUFFER_SIZE = 8192
    }
} 