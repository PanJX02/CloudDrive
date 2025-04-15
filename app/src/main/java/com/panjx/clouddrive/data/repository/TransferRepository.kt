package com.panjx.clouddrive.data.repository

import android.util.Log
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
        Log.d("TransferRepository", "创建传输任务: $fileName, 状态: $status")
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
        val id = transferDao.insertTransfer(transfer)
        Log.d("TransferRepository", "传输任务已创建，ID: $id")
        return id
    }
    
    suspend fun updateTransfer(transfer: TransferEntity) {
        Log.d("TransferRepository", "更新传输任务，ID: ${transfer.id}, 状态: ${transfer.status}")
        val updatedTransfer = transfer.copy(updatedAt = System.currentTimeMillis())
        transferDao.updateTransfer(updatedTransfer)
        Log.d("TransferRepository", "传输任务已更新")
    }
    
    suspend fun updateTransferStatus(id: Long, status: TransferStatus, progress: Int) {
        Log.d("TransferRepository", "更新传输任务状态，ID: $id, 状态: $status, 进度: $progress")
        try {
            // 获取当前的传输任务
            val transfers = transferDao.getTransferListByIds(listOf(id))
            if (transfers.isNotEmpty()) {
                val transfer = transfers[0]
                // 创建更新的传输任务对象
                val updatedTransfer = transfer.copy(
                    status = status,
                    progress = progress,
                    updatedAt = System.currentTimeMillis()
                )
                transferDao.updateTransfer(updatedTransfer)
                Log.d("TransferRepository", "传输任务状态已更新，ID: $id")
            } else {
                Log.e("TransferRepository", "找不到ID为 $id 的传输任务，无法更新状态")
            }
        } catch (e: Exception) {
            Log.e("TransferRepository", "更新传输任务状态出错: ${e.message}")
        }
    }
    
    suspend fun deleteTransfer(transfer: TransferEntity) {
        transferDao.deleteTransfer(transfer)
    }
    
    suspend fun deleteCompletedTransfers() {
        transferDao.deleteTransfersByStatus(TransferStatus.COMPLETED.name)
    }
    
    /**
     * 直接通过ID获取传输记录（不通过Flow）
     * 用于解决Flow更新延迟问题
     */
    suspend fun getTransferById(id: Long): TransferEntity? {
        Log.d("TransferRepository", "直接从数据库获取传输任务，ID: $id")
        try {
            // 使用Room的DAO执行一个直接查询
            val transfers = transferDao.getTransferListByIds(listOf(id))
            if (transfers.isNotEmpty()) {
                Log.d("TransferRepository", "成功获取到传输任务，ID: $id, 状态: ${transfers[0].status}")
                return transfers[0]
            } else {
                Log.e("TransferRepository", "数据库中找不到ID为 $id 的传输任务")
                return null
            }
        } catch (e: Exception) {
            Log.e("TransferRepository", "获取传输任务出错: ${e.message}")
            return null
        }
    }
} 