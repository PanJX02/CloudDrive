package com.panjx.clouddrive.data.repository

import com.panjx.clouddrive.data.database.TransferDao
import com.panjx.clouddrive.data.database.TransferEntity
import com.panjx.clouddrive.data.database.TransferType
import com.panjx.clouddrive.feature.transfersRoute.TransferStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransferRepository @Inject constructor(
    private val transferDao: TransferDao
) {
    fun getUploadTasks(): Flow<List<TransferEntity>> {
        return transferDao.getTransfersByType(TransferType.UPLOAD)
    }
    
    fun getDownloadTasks(): Flow<List<TransferEntity>> {
        return transferDao.getTransfersByType(TransferType.DOWNLOAD)
    }
    
    suspend fun addTransfer(
        fileName: String,
        progress: Int = 0,
        status: TransferStatus = TransferStatus.WAITING,
        type: TransferType,
        filePath: String,
        remoteUrl: String = "",
        fileSize: Long = 0
    ): Long {
        val transfer = TransferEntity(
            fileName = fileName,
            progress = progress,
            status = status,
            type = type,
            filePath = filePath,
            remoteUrl = remoteUrl,
            fileSize = fileSize
        )
        return transferDao.insertTransfer(transfer)
    }
    
    suspend fun updateTransfer(transfer: TransferEntity) {
        val updatedTransfer = transfer.copy(updatedAt = System.currentTimeMillis())
        transferDao.updateTransfer(updatedTransfer)
    }
    
    suspend fun updateTransferStatus(id: Long, status: TransferStatus, progress: Int) {
        // 这里需要先查询，再更新，实际实现中可以优化为单个语句
        // 此处简化处理
    }
    
    suspend fun deleteTransfer(transfer: TransferEntity) {
        transferDao.deleteTransfer(transfer)
    }
    
    suspend fun deleteCompletedTransfers() {
        transferDao.deleteTransfersByStatus(TransferStatus.COMPLETED.name)
    }
} 