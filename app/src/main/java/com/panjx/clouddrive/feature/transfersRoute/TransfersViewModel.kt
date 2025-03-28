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
                fileName = "文档1.doc",
                progress = 75,
                status = TransferStatus.IN_PROGRESS,
                type = TransferType.UPLOAD,
                filePath = "/storage/emulated/0/Documents/文档1.doc"
            )
            
            transferRepository.addTransfer(
                fileName = "图片1.jpg",
                progress = 100,
                status = TransferStatus.COMPLETED,
                type = TransferType.UPLOAD,
                filePath = "/storage/emulated/0/Pictures/图片1.jpg"
            )
            
            transferRepository.addTransfer(
                fileName = "视频1.mp4",
                progress = 30,
                status = TransferStatus.PAUSED,
                type = TransferType.UPLOAD,
                filePath = "/storage/emulated/0/Movies/视频1.mp4"
            )
            
            // 添加下载任务
            transferRepository.addTransfer(
                fileName = "文档2.doc",
                progress = 50,
                status = TransferStatus.IN_PROGRESS,
                type = TransferType.DOWNLOAD,
                filePath = "/storage/emulated/0/Download/文档2.doc",
                remoteUrl = "https://example.com/files/文档2.doc"
            )
            
            transferRepository.addTransfer(
                fileName = "图片2.jpg",
                progress = 0,
                status = TransferStatus.WAITING,
                type = TransferType.DOWNLOAD,
                filePath = "/storage/emulated/0/Download/图片2.jpg",
                remoteUrl = "https://example.com/files/图片2.jpg"
            )
            
            transferRepository.addTransfer(
                fileName = "视频2.mp4",
                progress = 100,
                status = TransferStatus.COMPLETED,
                type = TransferType.DOWNLOAD,
                filePath = "/storage/emulated/0/Download/视频2.mp4",
                remoteUrl = "https://example.com/files/视频2.mp4"
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