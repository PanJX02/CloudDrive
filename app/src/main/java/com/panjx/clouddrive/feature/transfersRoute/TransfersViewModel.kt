package com.panjx.clouddrive.feature.transfersRoute

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panjx.clouddrive.data.database.TransferEntity
import com.panjx.clouddrive.data.database.TransferType
import com.panjx.clouddrive.data.repository.TransferRepository
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
} 