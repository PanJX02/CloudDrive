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
    
    fun getInProgressUploadTasks(): Flow<List<TransferEntity>> {
        return transferDao.getTransfersByTypeExcludeStatus(TransferType.UPLOAD, TransferStatus.COMPLETED.name)
    }
    
    fun getCompletedUploadTasks(): Flow<List<TransferEntity>> {
        return transferDao.getTransfersByTypeAndStatus(TransferType.UPLOAD, TransferStatus.COMPLETED.name)
    }
    
    fun getInProgressDownloadTasks(): Flow<List<TransferEntity>> {
        return transferDao.getTransfersByTypeExcludeStatus(TransferType.DOWNLOAD, TransferStatus.COMPLETED.name)
    }
    
    fun getCompletedDownloadTasks(): Flow<List<TransferEntity>> {
        return transferDao.getTransfersByTypeAndStatus(TransferType.DOWNLOAD, TransferStatus.COMPLETED.name)
    }
    
    suspend fun addTransfer(
        fileName: String,
        progress: Int = 0,
        status: TransferStatus = TransferStatus.WAITING,
        type: TransferType,
        filePath: String,
        remoteUrl: String = "",
        fileSize: Long = 0,
        userId: Long? = null,
        fileId: Long? = null,
        fileExtension: String? = null,
        fileCategory: String? = null,
        filePid: Long? = null,
        folderType: Int = 0,
        deleteFlag: Int = 2,
        fileMD5: String? = null,
        fileSHA1: String? = null,
        fileSHA256: String? = null,
        storageId: Int? = null,
        fileCover: String? = null,
        referCount: Int? = null,
        fileStatus: Int? = 1,
        transcodeStatus: Int? = 0,
        domain: String? = null,
        uploadToken: String? = null
    ): Long {
        val transfer = TransferEntity(
            fileName = fileName,
            progress = progress,
            status = status,
            type = type,
            filePath = filePath,
            remoteUrl = remoteUrl,
            fileSize = fileSize,
            userId = userId,
            fileId = fileId,
            fileExtension = fileExtension,
            fileCategory = fileCategory,
            filePid = filePid,
            folderType = folderType,
            deleteFlag = deleteFlag,
            fileMD5 = fileMD5,
            fileSHA1 = fileSHA1,
            fileSHA256 = fileSHA256,
            storageId = storageId,
            fileCover = fileCover,
            referCount = referCount,
            fileStatus = fileStatus,
            transcodeStatus = transcodeStatus,
            domain = domain,
            uploadToken = uploadToken
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