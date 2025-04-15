package com.panjx.clouddrive.feature.transfersRoute

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.panjx.clouddrive.core.download.FileDownloadManager
import com.panjx.clouddrive.core.modle.File
import com.panjx.clouddrive.core.network.datasource.MyRetrofitDatasource
import com.panjx.clouddrive.data.database.TransferEntity
import com.panjx.clouddrive.data.database.TransferType
import com.panjx.clouddrive.data.repository.TransferRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 下载功能专用ViewModel
 * 处理所有与下载相关的功能
 */
@HiltViewModel
class DownloadTransfersViewModel @Inject constructor(
    transferRepository: TransferRepository,
    private val myRetrofitDatasource: MyRetrofitDatasource,
    private val fileDownloadManager: FileDownloadManager
) : BaseTransfersViewModel(transferRepository) {

    private val TAG = "DownloadTransfersVM"

    /**
     * 添加下载任务（单个文件）
     */
    fun addDownloadTask(file: File, context: Context) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "================== 单个文件下载流程 ==================")
                Log.d(TAG, "开始添加下载任务: 文件名=${file.fileName}, 文件ID=${file.id}, 是否文件夹=${file.folderType}")
                
                // 检查文件ID
                if (file.id == null) {
                    Log.e(TAG, "文件ID为空，无法下载: ${file.fileName}")
                    return@launch
                }
                
                // 根据文件夹类型确定处理方式
                if (file.folderType == 1) {
                    // 文件夹类型 - 创建一个等待获取下载链接的记录
                    Log.d(TAG, "文件夹下载: ${file.fileName}")
                    val taskId = transferRepository.addTransfer(
                        fileName = file.fileName ?: "未知文件夹",
                        progress = 0,
                        status = TransferStatus.WAITING,  // 使用WAITING状态表示等待获取下载链接
                        type = TransferType.DOWNLOAD,
                        filePath = "",  // 暂时为空，后续更新
                        remoteUrl = "",  // 暂时为空，后续更新
                        fileSize = 0,  // 暂时为0，后续更新
                        folderType = 1,
                        fileExtension = "",
                        fileSHA256 = file.fileSHA256 ?: ""
                    )
                    
                    // 异步获取下载链接并更新状态
                    fetchDownloadUrlAndStart(file.id, taskId, context, isFolder = true)
                    
                } else {
                    // 普通文件类型 - 创建一个等待获取下载链接的记录
                    // 计算文件名（包含扩展名）
                    val fileName = if (file.fileExtension.isNullOrEmpty()) {
                        file.fileName ?: "未知文件"
                    } else {
                        "${file.fileName}.${file.fileExtension}"
                    }
                    
                    // 初始时只创建一个基本路径，后续会更新为完整路径
                    val baseDir = getBaseDownloadPath(context)
                    val initialPath = "$baseDir/$fileName"
                    
                    Log.d(TAG, "文件下载: $fileName, 初始路径: $initialPath")
                    
                    val taskId = transferRepository.addTransfer(
                        fileName = fileName,
                        progress = 0,
                        status = TransferStatus.WAITING,  // 使用WAITING状态表示等待获取下载链接
                        type = TransferType.DOWNLOAD,
                        filePath = initialPath,  // 初始路径，将在获取到服务器响应后更新
                        remoteUrl = "",  // 暂时为空，后续更新
                        fileSize = file.fileSize ?: 0,
                        folderType = 0,
                        fileExtension = file.fileExtension ?: "",
                        fileSHA256 = file.fileSHA256 ?: ""
                    )
                    
                    // 异步获取下载链接并更新状态
                    fetchDownloadUrlAndStart(file.id, taskId, context, isFolder = false)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "添加下载任务异常: ${e.message}", e)
            }
        }
    }
    
    /**
     * 生成本地文件保存路径
     */
    private fun generateLocalFilePath(context: Context, file: File): String {
        // 使用FileDownloadManager的下载目录路径
        val baseDir = getBaseDownloadPath(context)
        
        // 确保文件名包含扩展名
        val fileName = if (file.fileExtension.isNullOrEmpty()) {
            file.fileName
        } else {
            "${file.fileName}.${file.fileExtension}"
        }
        
        // 如果有路径信息，使用它；否则直接放在下载根目录
        val filePidPath = if (file.filePid != null && file.filePid != 0L) {
            "/${file.filePid}"
        } else {
            ""
        }
        
        return "$baseDir$filePidPath/$fileName"
    }
    
    /**
     * 获取基础下载目录
     */
    private fun getBaseDownloadPath(context: Context): String {
        val downloadDir = android.os.Environment.getExternalStoragePublicDirectory(
            android.os.Environment.DIRECTORY_DOWNLOADS
        )
        val cloudDriveDir = java.io.File(downloadDir, "clouddrive")
        if (!cloudDriveDir.exists()) {
            cloudDriveDir.mkdirs()
        }
        return cloudDriveDir.absolutePath
    }
    
    /**
     * 异步获取下载链接并开始下载
     */
    private fun fetchDownloadUrlAndStart(fileId: Long, taskId: Long, context: Context, isFolder: Boolean) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "开始获取下载链接，请求参数: id=$fileId, taskId=$taskId")
                
                // 更新状态为获取链接中
                transferRepository.updateTransferStatus(taskId, TransferStatus.WAITING, 0)
                
                // 获取下载链接
                val response = myRetrofitDatasource.getDownloadUrl(fileId)
                
                if (response.code != 1 || response.data == null) {
                    Log.e(TAG, "获取下载链接失败: 错误码=${response.code}, 错误消息=${response.message}")
                    // 更新状态为失败
                    transferRepository.updateTransferStatus(taskId, TransferStatus.FAILED, 0)
                    return@launch
                }
                
                // 处理下载响应
                val downloadResponse = response.data
                Log.d(TAG, "获取下载链接成功: 是否文件夹=${downloadResponse.folderType}, 文件数量=${downloadResponse.downloadFiles.size}")
                
                // 根据是否为文件夹决定处理方式
                if (downloadResponse.folderType) {
                    // 处理文件夹下载 - 使用已创建的任务ID作为文件夹标识
                    Log.d(TAG, "开始处理文件夹下载...")
                    
                    val firstFileInfo = downloadResponse.downloadFiles.firstOrNull()
                    
                    // 计算本地文件夹完整路径
                    val baseDir = getBaseDownloadPath(context)
                    
                    // 获取文件夹的路径和名称
                    var folderPath = ""
                    var folderName = firstFileInfo?.fileName ?: "未知文件夹"
                    
                    if (firstFileInfo != null) {
                        val path = firstFileInfo.filePath.trim('/')
                        // 文件路径可能是 "parent/folder/file.ext" 
                        // 我们需要提取 "parent/folder" 作为文件夹路径
                        val lastSlashIndex = path.lastIndexOf('/')
                        if (lastSlashIndex >= 0) {
                            folderPath = path.substring(0, lastSlashIndex)
                        }
                    }
                    
                    // 构建本地文件夹路径
                    val localFolderPath = if (folderPath.isEmpty()) {
                        "$baseDir/$folderName" // 直接在根目录下
                    } else {
                        "$baseDir/$folderPath/$folderName" // 带有父路径
                    }
                    
                    // 确保文件夹存在
                    val folderDir = java.io.File(localFolderPath)
                    if (!folderDir.exists()) {
                        folderDir.mkdirs()
                    }
                    
                    Log.d(TAG, "文件夹下载路径: 目录=$folderPath, 文件夹名=$folderName, 完整路径=$localFolderPath")
                    
                    // 更新文件夹任务信息
                    transferRepository.updateTransfer(
                        TransferEntity(
                            id = taskId,
                            fileName = firstFileInfo?.fileName ?: "未知文件夹",
                            progress = 0,
                            status = TransferStatus.IN_PROGRESS,
                            type = TransferType.DOWNLOAD,
                            filePath = localFolderPath,  // 保存本地文件夹完整路径
                            remoteUrl = "",  // 文件夹类型没有具体URL
                            fileSize = downloadResponse.totalSize ?: 0,
                            folderType = 1,
                            fileExtension = "",
                            fileSHA256 = ""
                        )
                    )
                    
                    // 调用下载管理器处理文件夹子文件
                    fileDownloadManager.addFolderDownloadTask(
                        fileId = fileId,
                        downloadResponse = downloadResponse,
                        context = context
                    )
                } else {
                    // 单文件下载，应该只有一个文件
                    val fileInfo = downloadResponse.downloadFiles.firstOrNull()
                    if (fileInfo != null) {
                        Log.d(TAG, "下载信息: 文件名=${fileInfo.fileName}, URL=${fileInfo.url?.take(50)}...")
                        Log.d(TAG, "服务器返回的文件路径: ${fileInfo.filePath}")
                        
                        if (!fileInfo.url.isNullOrEmpty()) {
                            // 构建本地保存的完整路径
                            val baseDir = getBaseDownloadPath(context)
                            
                            // 获取服务器返回的目录路径（不包含文件名）
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
                            val dir = java.io.File(localDirPath)
                            if (!dir.exists()) {
                                dir.mkdirs()
                            }
                            
                            // 完整文件路径
                            val localFilePath = "$localDirPath/$fileName"
                            
                            Log.d(TAG, "本地保存路径: 目录=$localDirPath, 文件名=$fileName, 完整路径=$localFilePath")
                            
                            // 更新已创建的下载任务记录
                            transferRepository.updateTransfer(
                                TransferEntity(
                                    id = taskId,
                                    fileName = fileName,  // 使用含扩展名的文件名
                                    progress = 0,
                                    status = TransferStatus.IN_PROGRESS,
                                    type = TransferType.DOWNLOAD,
                                    filePath = localFilePath,  // 保存完整的本地文件路径
                                    remoteUrl = fileInfo.url,
                                    fileSize = fileInfo.size ?: 0,
                                    folderType = 0,
                                    fileExtension = fileInfo.fileExtension ?: "",
                                    fileSHA256 = fileInfo.sha256 ?: ""
                                )
                            )
                            
                            // 开始下载
                            Log.d(TAG, "开始下载，任务ID=$taskId")
                            startDownloadFile(taskId, context)
                        } else {
                            Log.e(TAG, "下载URL为空")
                            transferRepository.updateTransferStatus(taskId, TransferStatus.FAILED, 0)
                        }
                    } else {
                        Log.e(TAG, "下载信息不完整或为空")
                        transferRepository.updateTransferStatus(taskId, TransferStatus.FAILED, 0)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "获取下载链接异常: ${e.message}", e)
                transferRepository.updateTransferStatus(taskId, TransferStatus.FAILED, 0)
            }
        }
    }
    
    /**
     * 批量添加下载任务
     */
    fun addDownloadTasks(files: List<File>, context: Context) {
        viewModelScope.launch {
            Log.d(TAG, "================== 批量下载流程 ==================")
            Log.d(TAG, "开始批量添加下载任务，文件数量: ${files.size}")
            
            files.forEachIndexed { index, file ->
                Log.d(TAG, "处理第 ${index+1}/${files.size} 个文件: ${file.fileName}")
                addDownloadTask(file, context)
            }
        }
    }
    
    /**
     * 开始下载文件
     */
    fun startDownloadFile(taskId: Long, context: Context) {
        viewModelScope.launch {
            Log.d(TAG, "开始执行下载任务，任务ID=$taskId")
            
            withContext(Dispatchers.IO) {
                try {
                    fileDownloadManager.startDownload(taskId, context)
                } catch (e: Exception) {
                    Log.e(TAG, "启动下载异常: ${e.message}", e)
                    transferRepository.updateTransferStatus(taskId, TransferStatus.FAILED, 0)
                }
            }
        }
    }

    // 暂停或恢复下载
    override fun pauseOrResumeTransfer(transfer: TransferEntity, context: Context?) {
        viewModelScope.launch {
            // 获取当前状态，用于比较是否为暂停->恢复的操作
            val currentStatus = transfer.status

            // 确定新状态
            val newStatus = if (currentStatus == TransferStatus.PAUSED) {
                // 如果当前是暂停状态，改为进行中并重新开始下载
                TransferStatus.IN_PROGRESS
            } else {
                // 如果当前是其他状态，改为暂停并设置取消标志
                if (activeTransferTasks.containsKey(transfer.id)) {
                    // 设置取消标志为true，这将在下载过程中通过取消回调检查
                    activeTransferTasks[transfer.id] = true
                    Log.d(TAG, "设置任务暂停标志: ${transfer.id}")
                    
                    // 通知下载管理器暂停
                    fileDownloadManager.pauseDownload(transfer.id)

                    // 更新数据库中的状态为暂停
                    transferRepository.updateTransfer(
                        transfer.copy(status = TransferStatus.PAUSED)
                    )

                    // 等待一段时间确保下载操作响应暂停标志
                    kotlinx.coroutines.delay(1000)
                    return@launch
                }
                TransferStatus.PAUSED
            }

            // 更新数据库中的状态（仅在从暂停恢复为进行中时执行，或者其他状态切换）
            transferRepository.updateTransfer(
                transfer.copy(status = newStatus)
            )

            // 如果从暂停恢复为进行中，需要重新开始下载
            if (currentStatus == TransferStatus.PAUSED &&
                newStatus == TransferStatus.IN_PROGRESS &&
                transfer.type == TransferType.DOWNLOAD &&
                context != null
            ) {
                // 移除旧的取消标志（如果存在）
                activeTransferTasks.remove(transfer.id)
                // 重新下载
                Log.d(TAG, "恢复下载任务: ${transfer.id}, 文件名: ${transfer.fileName}")
                
                // 开始下载
                startDownloadFile(transfer.id, context)
            }
        }
    }
    
    /**
     * 取消下载并清理资源
     */
    override fun cancelTransfer(transfer: TransferEntity) {
        viewModelScope.launch {
            // 如果是下载任务，通知下载管理器取消
            if (transfer.type == TransferType.DOWNLOAD) {
                fileDownloadManager.cancelDownload(transfer.id)
            }
            
            // 调用基类的取消逻辑
            super.cancelTransfer(transfer)
        }
    }
}

