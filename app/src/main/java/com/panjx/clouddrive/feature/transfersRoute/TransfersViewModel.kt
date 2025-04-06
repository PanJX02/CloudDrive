package com.panjx.clouddrive.feature.transfersRoute

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panjx.clouddrive.core.modle.File
import com.panjx.clouddrive.core.network.datasource.MyRetrofitDatasource
import com.panjx.clouddrive.data.database.TransferEntity
import com.panjx.clouddrive.data.database.TransferType
import com.panjx.clouddrive.data.repository.TransferRepository
import com.panjx.clouddrive.util.FileUtils
import com.panjx.clouddrive.util.KodoUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@HiltViewModel
class TransfersViewModel @Inject constructor(
    private val transferRepository: TransferRepository,
    private val myRetrofitDatasource: MyRetrofitDatasource
) : ViewModel() {

    // 使用ConcurrentHashMap来跟踪活跃的上传任务
    // Key是传输任务的ID，Value是取消标志
    private val activeUploadTasks = ConcurrentHashMap<Long, Boolean>()

    val uploadTasks: StateFlow<List<TransferEntity>> = transferRepository
        .getUploadTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val downloadTasks: StateFlow<List<TransferEntity>> = transferRepository
        .getDownloadTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val inProgressUploadTasks: StateFlow<List<TransferEntity>> = transferRepository
        .getInProgressUploadTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val completedUploadTasks: StateFlow<List<TransferEntity>> = transferRepository
        .getCompletedUploadTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val inProgressDownloadTasks: StateFlow<List<TransferEntity>> = transferRepository
        .getInProgressDownloadTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val completedDownloadTasks: StateFlow<List<TransferEntity>> = transferRepository
        .getCompletedDownloadTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 添加模拟数据用于测试
    fun addSampleData() {
        viewModelScope.launch {
            // 添加上传任务
            transferRepository.addTransfer(
                fileName = "文档1",
                progress = 75,
                status = TransferStatus.IN_PROGRESS,
                type = TransferType.UPLOAD,
                filePath = "/storage/emulated/0/Documents/文档1.doc",
                fileExtension = "doc",
                fileCategory = "application/msword",
                fileSize = 15360,
                userId = 1001,
                fileMD5 = "d41d8cd98f00b204e9800998ecf8427e",
                folderType = 0,
                domain = "https://storage.example.com",
                uploadToken = "upload_token_123456"
            )
            
            transferRepository.addTransfer(
                fileName = "图片1",
                progress = 100,
                status = TransferStatus.COMPLETED,
                type = TransferType.UPLOAD,
                filePath = "/storage/emulated/0/Pictures/图片1.jpg",
                fileExtension = "jpg",
                fileCategory = "image/jpeg",
                fileSize = 102400,
                userId = 1001,
                fileMD5 = "e99a18c428cb38d5f260853678922e03",
                folderType = 0,
                fileCover = "/storage/emulated/0/Pictures/图片1_thumbnail.jpg",
                domain = "https://img.example.com",
                uploadToken = "upload_token_789012"
            )
            
            transferRepository.addTransfer(
                fileName = "视频1",
                progress = 30,
                status = TransferStatus.PAUSED,
                type = TransferType.UPLOAD,
                filePath = "/storage/emulated/0/Movies/视频1.mp4",
                fileExtension = "mp4",
                fileCategory = "video/mp4",
                fileSize = 10485760,
                userId = 1001,
                fileMD5 = "a87ff679a2f3e71d9181a67b7542122c",
                folderType = 0,
                transcodeStatus = 0,
                domain = "https://video.example.com",
                uploadToken = "upload_token_345678"
            )
            
            // 添加下载任务
            transferRepository.addTransfer(
                fileName = "文档2",
                progress = 50,
                status = TransferStatus.IN_PROGRESS,
                type = TransferType.DOWNLOAD,
                filePath = "/storage/emulated/0/Download/文档2.doc",
                remoteUrl = "https://example.com/files/文档2.doc",
                fileExtension = "doc",
                fileCategory = "application/msword",
                fileSize = 25600,
                userId = 1001,
                fileId = 2001,
                folderType = 0
            )
            
            transferRepository.addTransfer(
                fileName = "图片2",
                progress = 0,
                status = TransferStatus.WAITING,
                type = TransferType.DOWNLOAD,
                filePath = "/storage/emulated/0/Download/图片2.jpg",
                remoteUrl = "https://example.com/files/图片2.jpg",
                fileExtension = "jpg",
                fileCategory = "image/jpeg",
                fileSize = 51200,
                userId = 1001,
                fileId = 2002,
                folderType = 0,
                fileCover = "https://example.com/files/图片2_thumbnail.jpg"
            )
            
            transferRepository.addTransfer(
                fileName = "视频2",
                progress = 100,
                status = TransferStatus.COMPLETED,
                type = TransferType.DOWNLOAD,
                filePath = "/storage/emulated/0/Download/视频2.mp4",
                remoteUrl = "https://example.com/files/视频2.mp4",
                fileExtension = "mp4",
                fileCategory = "video/mp4",
                fileSize = 20971520,
                userId = 1001,
                fileId = 2003,
                folderType = 0,
                transcodeStatus = 2
            )
        }
    }
    
    fun pauseOrResumeTransfer(transfer: TransferEntity, context: Context? = null) {
        viewModelScope.launch {
            // 获取当前状态，用于比较是否为暂停->恢复的操作
            val currentStatus = transfer.status
            
            // 确定新状态
            val newStatus = if (currentStatus == TransferStatus.PAUSED) {
                // 如果当前是暂停状态，改为进行中并重新开始上传
                TransferStatus.IN_PROGRESS
            } else {
                // 如果当前是其他状态，改为暂停并设置取消标志
                if (activeUploadTasks.containsKey(transfer.id)) {
                    // 设置取消标志为true，这将在上传过程中通过取消回调检查
                    activeUploadTasks[transfer.id] = true
                    Log.d("TransfersViewModel", "设置任务暂停标志: ${transfer.id}")
                    // 为了确保断点续传记录保存，需要等待一小段时间
                    Log.d("TransfersViewModel", "等待保存断点续传记录...")
                    
                    // 等待500毫秒，给断点续传足够的时间来保存记录
                    kotlinx.coroutines.delay(500)
                    
                    // 检查断点续传记录是否保存成功
                    try {
                        // 使用与上传相同的key格式
                        val uploadKey = "tr${transfer.id}${transfer.fileMD5?.take(8) ?: ""}"
                        val recorderPath = "${com.qiniu.android.utils.Utils.sdkDirectory()}/recorder"
                        val recordDir = java.io.File(recorderPath)
                        if (recordDir.exists()) {
                            val files = recordDir.listFiles()
                            if (files != null) {
                                for (file in files) {
                                    if (file.name.contains(uploadKey)) {
                                        Log.d("TransfersViewModel", "暂停时检测到断点续传记录: ${file.name}, 大小: ${file.length()}")
                                    }
                                }
                            }
                        }
                        
                        // 使用七牛SDK原生方法处理Content URI，不再需要检查临时文件
                        if (transfer.filePath.startsWith("content://") && context != null) {
                            Log.d("TransfersViewModel", "使用Content URI上传，七牛SDK原生支持断点续传")
                        }
                    } catch (e: Exception) {
                        Log.e("TransfersViewModel", "检查断点续传记录失败", e)
                    }
                }
                TransferStatus.PAUSED
            }
            
            // 更新数据库中的状态
            transferRepository.updateTransfer(
                transfer.copy(status = newStatus)
            )
            
            // 如果从暂停恢复为进行中，需要重新开始上传
            if (currentStatus == TransferStatus.PAUSED && 
                newStatus == TransferStatus.IN_PROGRESS && 
                transfer.type == TransferType.UPLOAD && 
                !transfer.uploadToken.isNullOrEmpty() &&
                context != null) {
                // 移除旧的取消标志（如果存在）
                activeUploadTasks.remove(transfer.id)
                // 重新上传
                Log.d("TransfersViewModel", "恢复上传任务: ${transfer.id}, 文件名: ${transfer.fileName}")
                Log.d("TransfersViewModel", "使用断点续传功能，将继续之前的上传进度")
                
                // 尝试开始上传处理，startUploadFile方法已修改以支持IN_PROGRESS状态
                startUploadFile(transfer.id, context)
            }
        }
    }
    
    fun cancelTransfer(transfer: TransferEntity) {
        viewModelScope.launch {
            // 首先检查是否有活跃上传
            if (activeUploadTasks.containsKey(transfer.id)) {
                // 设置取消标志为true
                activeUploadTasks[transfer.id] = true
                Log.d("TransfersViewModel", "设置任务取消标志: ${transfer.id}")
            }
            
            // 然后从数据库中删除任务
            transferRepository.deleteTransfer(transfer)
            Log.d("TransfersViewModel", "从数据库中删除任务: ${transfer.id}")
        }
    }
    
    fun clearCompletedTransfers() {
        viewModelScope.launch {
            transferRepository.deleteCompletedTransfers()
        }
    }

    /**
     * 添加上传任务
     * @return 返回创建的传输记录ID
     */
    suspend fun addTransferTask(
        fileName: String,
        filePath: String,
        fileSize: Long,
        fileExtension: String,
        fileCategory: String,
        filePid: Long
    ): Long {
        Log.d("TransfersViewModel", "添加传输任务: $fileName, 大小: $fileSize bytes, 扩展名: $fileExtension")
        val id = transferRepository.addTransfer(
            fileName = fileName,
            progress = 0,
            status = TransferStatus.CALCULATING_HASH,  // 初始状态为计算哈希中
            type = TransferType.UPLOAD,
            filePath = filePath,
            fileSize = fileSize,
            fileExtension = fileExtension,
            fileCategory = fileCategory,
            filePid = filePid
        )
        Log.d("TransfersViewModel", "传输任务添加成功，ID: $id, 状态: CALCULATING_HASH")
        return id
    }
    
    /**
     * 更新传输记录的哈希值
     */
    suspend fun updateTransferHashes(
        transferId: Long,
        md5Hash: String?,
        sha1Hash: String?,
        sha256Hash: String?
    ) {
        Log.d("TransfersViewModel", "准备更新传输任务哈希值，ID: $transferId")
        // 首先获取现有的传输记录
        val existingTasks = uploadTasks.value
        val task = existingTasks.find { it.id == transferId }
        
        if (task == null) {
            Log.e("TransfersViewModel", "找不到传输任务，ID: $transferId")
            Log.d("TransfersViewModel", "当前所有上传任务: ${existingTasks.map { it.id }}")
            return
        }
        
        Log.d("TransfersViewModel", "找到传输任务，ID: ${task.id}, 当前状态: ${task.status}")
        
        // 更新哈希值并将状态改为哈希计算完成
        val updatedTask = task.copy(
            fileMD5 = md5Hash,
            fileSHA1 = sha1Hash,
            fileSHA256 = sha256Hash,
            status = TransferStatus.HASH_CALCULATED  // 计算完哈希后改为计算完成状态
        )
        
        // 保存更新
        try {
            Log.d("TransfersViewModel", "正在更新传输任务，ID: ${updatedTask.id}, 新状态: ${updatedTask.status}")
            transferRepository.updateTransfer(updatedTask)
            Log.d("TransfersViewModel", "传输任务更新成功")
        } catch (e: Exception) {
            Log.e("TransfersViewModel", "更新传输任务失败: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * 获取传输记录
     * 用于在计算完哈希后显示文件信息
     */
    fun getTransferById(transferId: Long): TransferEntity? {
        Log.d("TransfersViewModel", "获取传输任务，ID: $transferId")
        val tasks = uploadTasks.value
        Log.d("TransfersViewModel", "当前上传任务列表: ${tasks.map { "${it.id}(${it.status})" }}")
        
        val task = tasks.find { it.id == transferId }
        if (task == null) {
            Log.e("TransfersViewModel", "找不到传输任务，ID: $transferId")
        } else {
            Log.d("TransfersViewModel", "找到传输任务: ID=${task.id}, 状态=${task.status}, 文件名=${task.fileName}")
        }
        return task
    }

    /**
     * 更新传输记录的状态
     */
    suspend fun updateTransferStatus(
        transferId: Long,
        status: TransferStatus
    ) {
        Log.d("TransfersViewModel", "准备更新传输任务状态，ID: $transferId, 新状态: $status")
        // 获取传输记录
        val existingTasks = uploadTasks.value
        val task = existingTasks.find { it.id == transferId }
        
        if (task == null) {
            Log.e("TransfersViewModel", "找不到传输任务，ID: $transferId")
            Log.d("TransfersViewModel", "当前所有上传任务: ${existingTasks.map { it.id }}")
            return
        }
        
        Log.d("TransfersViewModel", "找到传输任务，ID: ${task.id}, 当前状态: ${task.status}, 文件名: ${task.fileName}")
        
        // 更新状态
        val updatedTask = task.copy(
            status = status
        )
        
        // 保存更新
        try {
            Log.d("TransfersViewModel", "正在更新传输任务状态，ID: ${updatedTask.id}, 新状态: ${updatedTask.status}")
            transferRepository.updateTransfer(updatedTask)
            Log.d("TransfersViewModel", "传输任务状态更新成功")
        } catch (e: Exception) {
            Log.e("TransfersViewModel", "更新传输任务状态失败: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 添加上传任务并计算哈希值
     * 整个过程在ViewModel中完成，保证数据一致性
     */
    fun addUploadTask(
        uri: Uri,
        fileName: String,
        fileSize: Long,
        fileExtension: String,
        fileCategory: String,
        filePid: Long,
        context: Context
    ) {
        Log.d("TransfersViewModel", "开始添加上传任务和计算哈希: $fileName")
        
        viewModelScope.launch {
            try {
                // 1. 先添加传输记录到数据库
                val id = transferRepository.addTransfer(
                    fileName = fileName,
                    progress = 0,
                    status = TransferStatus.CALCULATING_HASH,
                    type = TransferType.UPLOAD,
                    filePath = uri.toString(),
                    fileSize = fileSize,
                    fileExtension = fileExtension,
                    fileCategory = fileCategory,
                    filePid = filePid
                )
                
                Log.d("TransfersViewModel", "成功添加传输任务，ID: $id, 状态: CALCULATING_HASH")
                
                // 2. 计算哈希值
                Log.d("TransfersViewModel", "开始计算文件哈希值")
                val hashes = FileUtils.calculateFileHashesAsync(context, uri)
                val md5Hash = hashes["md5"]?.first
                val sha1Hash = hashes["sha1"]?.first
                val sha256Hash = hashes["sha256"]?.first
                
                Log.d("TransfersViewModel", "哈希计算完成")
                Log.d("TransfersViewModel", "MD5: $md5Hash")
                Log.d("TransfersViewModel", "SHA1: $sha1Hash")
                Log.d("TransfersViewModel", "SHA256: $sha256Hash")
                
                // 3. 直接从数据库获取最新记录
                Log.d("TransfersViewModel", "直接从数据库获取任务，ID: $id")
                val task = transferRepository.getTransferById(id)
                
                if (task != null) {
                    Log.d("TransfersViewModel", "找到任务，准备更新哈希值，ID: $id, 状态: ${task.status}")
                    // 更新哈希值和状态
                    val updatedTask = task.copy(
                        fileMD5 = md5Hash,
                        fileSHA1 = sha1Hash,
                        fileSHA256 = sha256Hash,
                        status = TransferStatus.HASH_CALCULATED
                    )
                    
                    // 更新数据库
                    transferRepository.updateTransfer(updatedTask)
                    Log.d("TransfersViewModel", "更新完成，状态: HASH_CALCULATED")
                } else {
                    Log.e("TransfersViewModel", "在数据库中找不到刚添加的任务，ID: $id")
                }
            } catch (e: Exception) {
                Log.e("TransfersViewModel", "添加上传任务或计算哈希时出错: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * 设置传输任务状态为等待上传
     */
    fun setTransferStatusToWaiting(transferId: Long) {
        Log.d("TransfersViewModel", "开始设置任务为等待状态，ID: $transferId")
        viewModelScope.launch {
            try {
                // 直接从数据库获取任务
                val task = transferRepository.getTransferById(transferId)
                
                if (task != null) {
                    Log.d("TransfersViewModel", "找到任务，ID: $transferId, 当前状态: ${task.status}")
                    // 更新状态
                    val updatedTask = task.copy(status = TransferStatus.WAITING)
                    
                    // 保存更新
                    transferRepository.updateTransfer(updatedTask)
                    Log.d("TransfersViewModel", "任务状态已更新为WAITING")
                } else {
                    Log.e("TransfersViewModel", "数据库中找不到任务，ID: $transferId")
                }
            } catch (e: Exception) {
                Log.e("TransfersViewModel", "更新任务状态失败: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * 请求上传令牌
     * 在文件哈希计算完成后调用，获取上传所需的token和domain
     */
    suspend fun requestUploadToken(transferId: Long) {
        Log.d("TransfersViewModel", "========== 开始请求上传令牌 ==========")
        Log.d("TransfersViewModel", "传输ID: $transferId")
        try {
            // 获取传输记录
            val task = transferRepository.getTransferById(transferId)
            
            if (task == null) {
                Log.e("TransfersViewModel", "错误: 找不到传输任务，ID: $transferId")
                return
            }
            
            Log.d("TransfersViewModel", "成功找到传输任务:")
            Log.d("TransfersViewModel", "- ID: ${task.id}")
            Log.d("TransfersViewModel", "- 文件名: ${task.fileName}")
            Log.d("TransfersViewModel", "- 大小: ${task.fileSize} 字节")
            Log.d("TransfersViewModel", "- 状态: ${task.status}")
            Log.d("TransfersViewModel", "- SHA256: ${task.fileSHA256}")
            
            // 确保任务处于哈希计算完成状态
            if (task.status != TransferStatus.HASH_CALCULATED) {
                Log.e("TransfersViewModel", "错误: 任务状态不正确，当前: ${task.status}, 需要: HASH_CALCULATED")
                return
            }
            
            // 创建要发送的File对象
            val fileToUpload = File(
                id = null,
                userId = null,
                fileId = null,
                fileName = task.fileName,
                fileExtension = task.fileExtension,
                fileCategory = null,
                filePid = task.filePid,
                folderType = null,
                deleteFlag = null,
                recoveryTime = null,
                createTime = null,
                lastUpdateTime = null,
                fileMD5 = null,
                fileSHA1 = null,
                fileSHA256 = task.fileSHA256,
                storageId = null,
                fileSize = null,
                fileCover = null,
                referCount = null,
                status = null,
                transcodeStatus = null,
                fileCreateTime = null,
                lastReferTime = null
            )
            
            Log.d("TransfersViewModel", "准备请求上传令牌，要发送的数据:")
            Log.d("TransfersViewModel", "- 文件名: ${fileToUpload.fileName}")
            Log.d("TransfersViewModel", "- 扩展名: ${fileToUpload.fileExtension}")
            Log.d("TransfersViewModel", "- SHA256: ${fileToUpload.fileSHA256}")
            Log.d("TransfersViewModel", "- 父目录ID: ${fileToUpload.filePid}")
            
            // 发送网络请求
            Log.d("TransfersViewModel", "正在调用网络接口 uploadFile()...")
            val response = myRetrofitDatasource.uploadFile(fileToUpload)
            
            Log.d("TransfersViewModel", "网络请求完成，响应码: ${response.code}")
            Log.d("TransfersViewModel", "响应消息: ${response.message}")
            
            if (response.code == 1) {
                // 请求成功
                Log.d("TransfersViewModel", "上传令牌请求成功:")
                Log.d("TransfersViewModel", "- 文件是否已存在: ${response.data?.fileExists}")
                Log.d("TransfersViewModel", "- 域名列表: ${response.data?.domain}")
                Log.d("TransfersViewModel", "- 上传令牌: ${response.data?.uploadToken}")
                
                // 将domain列表转换为字符串存储
                val domainString = response.data?.domain?.joinToString(",")
                
                // 更新任务
                val updatedTask = task.copy(
                    domain = domainString,
                    uploadToken = response.data?.uploadToken,
                    status = TransferStatus.WAITING // 更新状态为等待上传
                )
                
                // 保存更新
                Log.d("TransfersViewModel", "正在更新数据库中的传输任务...")
                transferRepository.updateTransfer(updatedTask)
                Log.d("TransfersViewModel", "数据库更新成功，状态已更新为: WAITING")
                Log.d("TransfersViewModel", "- 存储的域名: ${updatedTask.domain}")
                Log.d("TransfersViewModel", "- 存储的令牌: ${updatedTask.uploadToken}")
            } else {
                // 请求失败
                Log.e("TransfersViewModel", "上传令牌请求失败:")
                Log.e("TransfersViewModel", "- 错误码: ${response.code}")
                Log.e("TransfersViewModel", "- 错误消息: ${response.message}")
                
                // 更新状态为失败
                val updatedTask = task.copy(
                    status = TransferStatus.FAILED
                )
                Log.d("TransfersViewModel", "正在将任务状态更新为: FAILED")
                transferRepository.updateTransfer(updatedTask)
                Log.d("TransfersViewModel", "数据库更新完成")
            }
        } catch (e: Exception) {
            Log.e("TransfersViewModel", "请求上传令牌时发生异常:")
            Log.e("TransfersViewModel", "- 异常类型: ${e.javaClass.simpleName}")
            Log.e("TransfersViewModel", "- 异常信息: ${e.message}")
            Log.e("TransfersViewModel", "- 堆栈跟踪:")
            e.printStackTrace()
        } finally {
            Log.d("TransfersViewModel", "========== 上传令牌请求流程结束 ==========")
        }
    }

    /**
     * 设置传输任务状态为等待上传并请求上传令牌
     * 修改原有方法，增加令牌获取功能
     */
    fun setTransferStatusToWaitingAndRequestToken(transferId: Long) {
        Log.d("TransfersViewModel", "========== 开始处理上传令牌请求 ==========")
        Log.d("TransfersViewModel", "传输ID: $transferId")
        viewModelScope.launch {
            try {
                Log.d("TransfersViewModel", "启动协程，准备调用requestUploadToken方法")
                // 调用获取上传令牌的方法
                requestUploadToken(transferId)
                Log.d("TransfersViewModel", "requestUploadToken方法调用完成")
            } catch (e: Exception) {
                Log.e("TransfersViewModel", "获取上传令牌过程中捕获到异常:")
                Log.e("TransfersViewModel", "- 异常类型: ${e.javaClass.simpleName}")
                Log.e("TransfersViewModel", "- 异常信息: ${e.message}")
                Log.e("TransfersViewModel", "- 堆栈跟踪:")
                e.printStackTrace()
            } finally {
                Log.d("TransfersViewModel", "========== 上传令牌请求处理结束 ==========")
            }
        }
    }

    /**
     * 开始上传文件到七牛云
     * @param transferId 传输任务ID
     * @param context 上下文
     */
    fun startUploadFile(transferId: Long, context: Context) {
        Log.d("TransfersViewModel", "========== 开始处理文件上传 ==========")
        Log.d("TransfersViewModel", "传输ID: $transferId")
        
        viewModelScope.launch {
            try {
                // 获取传输记录
                val task = transferRepository.getTransferById(transferId)
                
                if (task == null) {
                    Log.e("TransfersViewModel", "错误: 找不到传输任务，ID: $transferId")
                    return@launch
                }
                
                // 确保任务状态合理：等待状态、暂停状态或者已经是进行中状态
                if (task.status != TransferStatus.WAITING && 
                    task.status != TransferStatus.PAUSED && 
                    task.status != TransferStatus.IN_PROGRESS) {
                    Log.e("TransfersViewModel", "错误: 任务状态不正确，当前: ${task.status}, 需要: WAITING, PAUSED 或 IN_PROGRESS")
                    return@launch
                }
                
                // 确保有上传令牌
                if (task.uploadToken.isNullOrEmpty()) {
                    Log.e("TransfersViewModel", "错误: 上传令牌为空")
                    return@launch
                }
                
                Log.d("TransfersViewModel", "开始上传文件:")
                Log.d("TransfersViewModel", "- ID: ${task.id}")
                Log.d("TransfersViewModel", "- 文件名: ${task.fileName}")
                Log.d("TransfersViewModel", "- 路径: ${task.filePath}")
                Log.d("TransfersViewModel", "- 令牌: ${task.uploadToken}")
                
                // 只有当任务不是IN_PROGRESS状态时才更新为IN_PROGRESS
                if (task.status != TransferStatus.IN_PROGRESS) {
                    Log.d("TransfersViewModel", "更新任务状态为上传中")
                    transferRepository.updateTransfer(
                        task.copy(status = TransferStatus.IN_PROGRESS)
                    )
                } else {
                    Log.d("TransfersViewModel", "任务已经是上传中状态，继续断点续传")
                }
                
                // 初始化KodoUtils，确保断点续传管理器已创建
                val kodoUtils = KodoUtils()
                val initResult = kodoUtils.init()
                Log.d("TransfersViewModel", "初始化KodoUtils结果: $initResult")
                
                // 使用SHA256哈希值生成分层目录结构的key
                var uploadKey: String
                
                if (task.fileSHA256 != null && task.fileSHA256.isNotEmpty()) {
                    // 使用SHA256生成分层目录结构
                    uploadKey = kodoUtils.generateLayeredKey(task.fileSHA256)
                    Log.d("TransfersViewModel", "使用SHA256生成的分层目录结构key: $uploadKey")
                } else {
                    // 如果没有SHA256，则使用原来的方式作为备选方案
                    uploadKey = "tr${task.id}${task.fileMD5?.take(8) ?: ""}"
                    Log.d("TransfersViewModel", "没有SHA256，使用兼容的key: $uploadKey")
                }
                
                // 检查断点续传记录是否存在
                val recorderPath = "${com.qiniu.android.utils.Utils.sdkDirectory()}/recorder"
                val recorderDir = java.io.File(recorderPath)
                if (recorderDir.exists()) {
                    val files = recorderDir.listFiles()
                    var hasRecordFile = false
                    if (files != null) {
                        for (file in files) {
                            if (file.name.contains(uploadKey)) {
                                Log.d("TransfersViewModel", "找到断点续传记录文件: ${file.name}, 大小: ${file.length()}")
                                hasRecordFile = true
                                break
                            }
                        }
                    }
                    if (!hasRecordFile) {
                        Log.d("TransfersViewModel", "找不到断点续传记录文件，将从头开始上传")
                    }
                } else {
                    Log.d("TransfersViewModel", "断点续传记录目录不存在")
                    val created = recorderDir.mkdirs()
                    Log.d("TransfersViewModel", "创建断点续传记录目录: $created")
                }
                
                // 对Content URI类型的文件，不再需要检查临时文件
                // 七牛SDK原生支持Uri断点续传
                if (task.filePath.startsWith("content://")) {
                    Log.d("TransfersViewModel", "使用Content URI上传，七牛SDK原生支持断点续传")
                }
                
                // 将此上传任务添加到活跃任务中，并设置取消标志为false
                activeUploadTasks[task.id] = false
                
                // 上传文件，使用断点续传
                kodoUtils.uploadFile(
                    context = context,
                    uri = Uri.parse(task.filePath),
                    token = task.uploadToken!!,
                    key = uploadKey,  // 使用唯一标识作为key
                    onProgress = { progress ->
                        // 检查取消标志
                        if (activeUploadTasks[task.id] == true) {
                            // 任务被暂停或取消，不继续更新进度
                            Log.d("TransfersViewModel", "任务已被暂停或取消，不更新进度: ${task.id}")
                        } else {
                            // 更新进度
                            viewModelScope.launch {
                                val progressInt = (progress * 100).toInt()
                                if (progressInt != task.progress) {
                                    Log.d("TransfersViewModel", "更新任务进度: ${task.id}, $progressInt%")
                                    transferRepository.updateTransfer(
                                        task.copy(progress = progressInt)
                                    )
                                }
                            }
                        }
                    },
                    onComplete = { success, message ->
                        // 上传完成后更新状态
                        viewModelScope.launch {
                            // 从活跃任务中移除
                            activeUploadTasks.remove(task.id)
                            
                            // 检查此任务是否在完成前被暂停或取消
                            val currentTask = transferRepository.getTransferById(task.id)
                            if (currentTask != null) {
                                // 如果任务已被暂停，保持暂停状态
                                if (currentTask.status == TransferStatus.PAUSED) {
                                    Log.d("TransfersViewModel", "任务已被暂停，保持暂停状态: ${task.id}")
                                    return@launch
                                }
                                
                                val newStatus = if (success) TransferStatus.COMPLETED else TransferStatus.FAILED
                                Log.d("TransfersViewModel", "上传${if (success) "成功" else "失败"}: $message")
                                transferRepository.updateTransfer(
                                    currentTask.copy(
                                        status = newStatus,
                                        progress = if (success) 100 else currentTask.progress
                                    )
                                )
                            }
                        }
                    },
                    // 添加取消检查回调
                    onCancelled = {
                        // 检查此任务是否需要被取消
                        val shouldCancel = activeUploadTasks[task.id] == true
                        if (shouldCancel) {
                            Log.d("TransfersViewModel", "任务被请求取消: ${task.id}")
                        }
                        shouldCancel
                    }
                )
            } catch (e: Exception) {
                Log.e("TransfersViewModel", "上传过程中捕获到异常:")
                Log.e("TransfersViewModel", "- 异常类型: ${e.javaClass.simpleName}")
                Log.e("TransfersViewModel", "- 异常信息: ${e.message}")
                Log.e("TransfersViewModel", "- 堆栈跟踪:")
                e.printStackTrace()
                
                // 从活跃任务中移除
                activeUploadTasks.remove(transferId)
                
                // 尝试将任务状态更新为失败
                try {
                    val task = transferRepository.getTransferById(transferId)
                    if (task != null && task.status != TransferStatus.PAUSED) {
                        transferRepository.updateTransfer(
                            task.copy(status = TransferStatus.FAILED)
                        )
                    }
                } catch (updateEx: Exception) {
                    Log.e("TransfersViewModel", "更新任务状态失败: ${updateEx.message}")
                }
            } finally {
                Log.d("TransfersViewModel", "========== 文件上传处理结束 ==========")
            }
        }
    }
} 