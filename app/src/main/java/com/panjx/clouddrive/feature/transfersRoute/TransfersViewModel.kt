package com.panjx.clouddrive.feature.transfersRoute

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panjx.clouddrive.data.database.TransferEntity
import com.panjx.clouddrive.data.database.TransferType
import com.panjx.clouddrive.data.repository.TransferRepository
import com.panjx.clouddrive.util.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransfersViewModel @Inject constructor(
    private val transferRepository: TransferRepository
) : ViewModel() {

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
    
    fun pauseOrResumeTransfer(transfer: TransferEntity) {
        viewModelScope.launch {
            val newStatus = if (transfer.status == TransferStatus.PAUSED) {
                TransferStatus.IN_PROGRESS
            } else {
                TransferStatus.PAUSED
            }
            
            transferRepository.updateTransfer(
                transfer.copy(status = newStatus)
            )
        }
    }
    
    fun cancelTransfer(transfer: TransferEntity) {
        viewModelScope.launch {
            transferRepository.deleteTransfer(transfer)
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
} 