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
    val transferRepository: TransferRepository,
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

                    // 重要：先更新数据库状态，再给断点续传足够的时间来保存记录
                    // 更新数据库中的状态为暂停
                    transferRepository.updateTransfer(
                        transfer.copy(status = TransferStatus.PAUSED)
                    )

                    // 为了确保断点续传记录保存，需要等待一定时间
                    Log.d("TransfersViewModel", "等待保存断点续传记录...")

                    // 等待更长时间，增加至2000毫秒，给断点续传足够的时间来保存记录
                    kotlinx.coroutines.delay(2000)

                    // 检查断点续传记录是否保存成功
                    try {
                        // 使用与上传相同的key格式 - 支持新的SHA256分层结构
                        var uploadKey: String
                        if (transfer.fileSHA256 != null && transfer.fileSHA256.isNotEmpty()) {
                            val kodoUtils = KodoUtils()
                            uploadKey = kodoUtils.generateLayeredKey(transfer.fileSHA256)
                        } else {
                            uploadKey = "tr${transfer.id}${transfer.fileMD5?.take(8) ?: ""}"
                        }

                        val recorderPath =
                            "${com.qiniu.android.utils.Utils.sdkDirectory()}/recorder"
                        val recordDir = java.io.File(recorderPath)
                        if (recordDir.exists()) {
                            val files = recordDir.listFiles()
                            if (files != null) {
                                var foundRecord = false
                                for (file in files) {
                                    if (file.name.contains(uploadKey)) {
                                        Log.d(
                                            "TransfersViewModel",
                                            "暂停时检测到断点续传记录: ${file.name}, 大小: ${file.length()}"
                                        )
                                        foundRecord = true
                                    }
                                }
                                if (!foundRecord) {
                                    Log.w(
                                        "TransfersViewModel",
                                        "未找到断点续传记录，可能无法恢复上传进度"
                                    )
                                }
                            }
                        }

                        // 使用七牛SDK原生方法处理Content URI，不再需要检查临时文件
                        if (transfer.filePath.startsWith("content://") && context != null) {
                            Log.d(
                                "TransfersViewModel",
                                "使用Content URI上传，七牛SDK原生支持断点续传"
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("TransfersViewModel", "检查断点续传记录失败", e)
                    }

                    // 此处不再需要更新状态，因为在尝试取消上传前已经更新了
                    return@launch  // 直接返回，因为已经在开始处理暂停
                }
                TransferStatus.PAUSED
            }

            // 更新数据库中的状态（仅在从暂停恢复为进行中时执行，或者其他状态切换）
            transferRepository.updateTransfer(
                transfer.copy(status = newStatus)
            )

            // 如果从暂停恢复为进行中，需要重新开始上传
            if (currentStatus == TransferStatus.PAUSED &&
                newStatus == TransferStatus.IN_PROGRESS &&
                transfer.type == TransferType.UPLOAD &&
                !transfer.uploadToken.isNullOrEmpty() &&
                context != null
            ) {
                // 移除旧的取消标志（如果存在）
                activeUploadTasks.remove(transfer.id)
                // 重新上传
                Log.d(
                    "TransfersViewModel",
                    "恢复上传任务: ${transfer.id}, 文件名: ${transfer.fileName}"
                )
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

                // 重要：先更新任务状态为取消中，避免上传线程继续更新
                // 更新数据库中的状态为取消中
                val updatedTask = transfer.copy(status = TransferStatus.CANCELLING)
                transferRepository.updateTransfer(updatedTask)
                Log.d("TransfersViewModel", "更新任务状态为取消中: ${transfer.id}")

                // 如果任务正在上传中，先等待一段时间让上传操作有机会响应取消标志
                if (transfer.status == TransferStatus.IN_PROGRESS) {
                    Log.d("TransfersViewModel", "等待上传操作响应取消标志...")
                    // 等待3秒，让上传操作有足够的时间响应取消标志并停止尝试更新数据库
                    kotlinx.coroutines.delay(3000)
                }
            }

            // 清理与此任务相关的取消标志
            activeUploadTasks.remove(transfer.id)

            // 清理断点续传记录
            try {
                // 生成上传key（保持与上传时相同的逻辑）
                var uploadKey: String
                if (transfer.fileSHA256 != null && transfer.fileSHA256.isNotEmpty()) {
                    val kodoUtils = KodoUtils()
                    uploadKey = kodoUtils.generateLayeredKey(transfer.fileSHA256)
                } else {
                    uploadKey = "tr${transfer.id}${transfer.fileMD5?.take(8) ?: ""}"
                }

                // 删除断点续传记录
                val recorderPath = "${com.qiniu.android.utils.Utils.sdkDirectory()}/recorder"
                val recordDir = java.io.File(recorderPath)
                if (recordDir.exists()) {
                    val files = recordDir.listFiles()
                    if (files != null) {
                        for (file in files) {
                            if (file.name.contains(uploadKey)) {
                                val deleted = file.delete()
                                Log.d(
                                    "TransfersViewModel",
                                    "删除断点续传记录: ${file.name}, 结果: $deleted"
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("TransfersViewModel", "清理断点续传记录失败", e)
            }

            // 最后从数据库中删除任务
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
        Log.d(
            "TransfersViewModel",
            "添加传输任务: $fileName, 大小: $fileSize bytes, 扩展名: $fileExtension"
        )
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
            Log.d(
                "TransfersViewModel",
                "正在更新传输任务，ID: ${updatedTask.id}, 新状态: ${updatedTask.status}"
            )
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
            Log.d(
                "TransfersViewModel",
                "找到传输任务: ID=${task.id}, 状态=${task.status}, 文件名=${task.fileName}"
            )
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

        Log.d(
            "TransfersViewModel",
            "找到传输任务，ID: ${task.id}, 当前状态: ${task.status}, 文件名: ${task.fileName}"
        )

        // 更新状态
        val updatedTask = task.copy(
            status = status
        )

        // 保存更新
        try {
            Log.d(
                "TransfersViewModel",
                "正在更新传输任务状态，ID: ${updatedTask.id}, 新状态: ${updatedTask.status}"
            )
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
                    Log.d(
                        "TransfersViewModel",
                        "找到任务，准备更新哈希值，ID: $id, 状态: ${task.status}"
                    )
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
                    Log.d(
                        "TransfersViewModel",
                        "找到任务，ID: $transferId, 当前状态: ${task.status}"
                    )
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
                Log.e(
                    "TransfersViewModel",
                    "错误: 任务状态不正确，当前: ${task.status}, 需要: HASH_CALCULATED"
                )
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
                storageId = task.storageId,
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
                Log.d("TransfersViewModel", "- 存储ID: ${response.data?.storageId}")

                // 将domain列表转换为字符串存储
                val domainString = response.data?.domain?.joinToString(",")

                // 更新任务
                val updatedTask = task.copy(
                    domain = domainString,
                    uploadToken = response.data?.uploadToken,
                    storageId = response.data?.storageId,
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
     * 自动上传流程
     * 将addUploadTask、requestUploadToken、startUploadFile和uploadComplete步骤集成在一起自动执行
     * 上传整个流程将自动进行，完成一步后自动进入下一步
     */
    fun autoUploadProcess(
        uri: Uri,
        fileName: String,
        fileSize: Long,
        fileExtension: String,
        fileCategory: String,
        filePid: Long,
        context: Context
    ) {
        Log.d("TransfersViewModel", "========== 开始自动上传流程 ==========")
        Log.d("TransfersViewModel", "文件名: $fileName")

        viewModelScope.launch {
            try {
                // 1. 添加上传任务和计算哈希
                Log.d("TransfersViewModel", "步骤1: 添加上传任务和计算哈希")
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

                // 2. 计算哈希值
                Log.d("TransfersViewModel", "步骤2: 计算文件哈希值")
                val hashes = FileUtils.calculateFileHashesAsync(context, uri)
                val md5Hash = hashes["md5"]?.first
                val sha1Hash = hashes["sha1"]?.first
                val sha256Hash = hashes["sha256"]?.first

                Log.d("TransfersViewModel", "哈希计算完成")
                Log.d("TransfersViewModel", "MD5: $md5Hash")

                // 3. 获取任务并更新哈希值
                val task = transferRepository.getTransferById(id)

                if (task != null) {
                    // 更新哈希值和状态
                    val updatedTask = task.copy(
                        fileMD5 = md5Hash,
                        fileSHA1 = sha1Hash,
                        fileSHA256 = sha256Hash,
                        status = TransferStatus.HASH_CALCULATED
                    )

                    // 更新数据库
                    transferRepository.updateTransfer(updatedTask)
                    Log.d("TransfersViewModel", "哈希值已更新，状态: HASH_CALCULATED")

                    // 4. 请求上传令牌
                    Log.d("TransfersViewModel", "步骤3: 请求上传令牌")
                    val tokenTask = transferRepository.getTransferById(id)

                    if (tokenTask != null && tokenTask.status == TransferStatus.HASH_CALCULATED) {
                        // 创建要发送的File对象
                        val fileToUpload = File(
                            id = null,
                            userId = null,
                            fileId = null,
                            fileName = tokenTask.fileName,
                            fileExtension = tokenTask.fileExtension,
                            fileCategory = null,
                            filePid = tokenTask.filePid,
                            folderType = null,
                            deleteFlag = null,
                            recoveryTime = null,
                            createTime = null,
                            lastUpdateTime = null,
                            fileMD5 = null,
                            fileSHA1 = null,
                            fileSHA256 = tokenTask.fileSHA256,
                            storageId = tokenTask.storageId,
                            fileSize = null,
                            fileCover = null,
                            referCount = null,
                            status = null,
                            transcodeStatus = null,
                            fileCreateTime = null,
                            lastReferTime = null
                        )

                        // 发送网络请求获取令牌
                        val response = myRetrofitDatasource.uploadFile(fileToUpload)

                        if (response.code == 1) {
                            // 请求成功
                            Log.d("TransfersViewModel", "上传令牌请求成功")

                            // 将domain列表转换为字符串存储
                            val domainString = response.data?.domain?.joinToString(",")

                            // 更新任务
                            val tokenUpdatedTask = tokenTask.copy(
                                domain = domainString,
                                uploadToken = response.data?.uploadToken,
                                storageId = response.data?.storageId,
                                status = TransferStatus.WAITING // 更新状态为等待上传
                            )

                            // 保存更新
                            transferRepository.updateTransfer(tokenUpdatedTask)
                            Log.d("TransfersViewModel", "令牌信息已更新，状态: WAITING")

                            // 5. 开始上传文件
                            Log.d("TransfersViewModel", "步骤4: 开始上传文件")
                            val uploadTask = transferRepository.getTransferById(id)

                            if (uploadTask != null && uploadTask.status == TransferStatus.WAITING) {
                                // 更新状态为上传中
                                transferRepository.updateTransfer(
                                    uploadTask.copy(status = TransferStatus.IN_PROGRESS)
                                )

                                // 初始化KodoUtils，确保断点续传管理器已创建
                                val kodoUtils = KodoUtils()
                                val initResult = kodoUtils.init()

                                // 使用SHA256哈希值生成分层目录结构的key
                                var uploadKey: String

                                if (uploadTask.fileSHA256 != null && uploadTask.fileSHA256.isNotEmpty()) {
                                    // 使用SHA256生成分层目录结构
                                    uploadKey = kodoUtils.generateLayeredKey(uploadTask.fileSHA256)
                                } else {
                                    // 如果没有SHA256，则使用原来的方式作为备选方案
                                    uploadKey =
                                        "tr${uploadTask.id}${uploadTask.fileMD5?.take(8) ?: ""}"
                                }

                                // 将此上传任务添加到活跃任务中，并设置取消标志为false
                                activeUploadTasks[uploadTask.id] = false

                                // 上传文件，使用断点续传
                                kodoUtils.uploadFile(
                                    context = context,
                                    uri = Uri.parse(uploadTask.filePath),
                                    token = uploadTask.uploadToken!!,
                                    key = uploadKey,
                                    onProgress = { progress ->
                                        // 检查取消标志
                                        if (activeUploadTasks[uploadTask.id] == true) {
                                            // 任务被暂停或取消，不继续更新进度
                                            Log.d(
                                                "TransfersViewModel",
                                                "任务已被暂停或取消，不更新进度: ${uploadTask.id}"
                                            )
                                        } else {
                                            // 再次检查任务状态，确保没有被暂停或取消
                                            viewModelScope.launch {
                                                try {
                                                    val currentTask =
                                                        transferRepository.getTransferById(
                                                            uploadTask.id
                                                        )
                                                    if (currentTask == null) {
                                                        // 任务已被删除，设置取消标志并停止尝试更新
                                                        Log.d(
                                                            "TransfersViewModel",
                                                            "任务已被删除，设置取消标志: ${uploadTask.id}"
                                                        )
                                                        activeUploadTasks[uploadTask.id] = true
                                                        return@launch
                                                    }

                                                    if (currentTask.status == TransferStatus.PAUSED ||
                                                        currentTask.status == TransferStatus.FAILED ||
                                                        currentTask.status == TransferStatus.CANCELLING
                                                    ) {
                                                        // 任务已被暂停、失败或正在取消，设置取消标志
                                                        Log.d(
                                                            "TransfersViewModel",
                                                            "检测到任务状态为 ${currentTask.status}，设置取消标志: ${uploadTask.id}"
                                                        )
                                                        activeUploadTasks[uploadTask.id] = true
                                                        return@launch
                                                    }

                                                    // 更新进度
                                                    val progressInt = (progress * 100).toInt()
                                                    if (progressInt != uploadTask.progress) {
                                                        Log.d(
                                                            "TransfersViewModel",
                                                            "更新任务进度: ${uploadTask.id}, $progressInt%"
                                                        )
                                                        transferRepository.updateTransfer(
                                                            uploadTask.copy(progress = progressInt)
                                                        )
                                                    }
                                                } catch (e: Exception) {
                                                    // 处理异常，可能是由于任务已被删除
                                                    Log.e(
                                                        "TransfersViewModel",
                                                        "更新进度时出错: ${e.message}"
                                                    )
                                                    activeUploadTasks[uploadTask.id] = true
                                                }
                                            }
                                        }
                                    },
                                    onComplete = { success, message ->
                                        // 上传完成后自动进入下一步
                                        viewModelScope.launch {
                                            // 从活跃任务中移除
                                            activeUploadTasks.remove(uploadTask.id)

                                            try {
                                                // 检查此任务是否在完成前被暂停或取消
                                                val currentTask =
                                                    transferRepository.getTransferById(uploadTask.id)
                                                if (currentTask == null) {
                                                    // 任务已被删除，不需要更新
                                                    Log.d(
                                                        "TransfersViewModel",
                                                        "任务已被删除，不更新状态: ${uploadTask.id}"
                                                    )
                                                    return@launch
                                                }

                                                // 如果任务已被暂停或正在取消，保持当前状态
                                                if (currentTask.status == TransferStatus.PAUSED ||
                                                    currentTask.status == TransferStatus.CANCELLING
                                                ) {
                                                    Log.d(
                                                        "TransfersViewModel",
                                                        "任务状态为 ${currentTask.status}，保持当前状态: ${uploadTask.id}"
                                                    )
                                                    return@launch
                                                }

                                                // 文件上传到存储成功，更新状态
                                                if (success) {
                                                    // 更新状态为上传到存储完成
                                                    val updatedCurrentTask = currentTask.copy(
                                                        status = TransferStatus.UPLOAD_STORAGE_COMPLETED,
                                                        progress = 100
                                                    )
                                                    transferRepository.updateTransfer(
                                                        updatedCurrentTask
                                                    )
                                                    Log.d(
                                                        "TransfersViewModel",
                                                        "文件已上传到存储，状态: UPLOAD_STORAGE_COMPLETED"
                                                    )

                                                    // 6. 自动完成上传（通知服务器）
                                                    Log.d(
                                                        "TransfersViewModel",
                                                        "步骤5: 自动完成上传（通知服务器）"
                                                    )
                                                    val finalTask =
                                                        transferRepository.getTransferById(
                                                            uploadTask.id
                                                        )

                                                    if (finalTask != null && finalTask.status == TransferStatus.UPLOAD_STORAGE_COMPLETED) {
                                                        // 创建要发送的File对象
                                                        val fileToUpload = File(
                                                            id = null,
                                                            userId = null,
                                                            fileId = null,
                                                            fileName = finalTask.fileName,
                                                            fileExtension = finalTask.fileExtension,
                                                            fileCategory = finalTask.fileCategory,
                                                            filePid = finalTask.filePid,
                                                            folderType = null,
                                                            deleteFlag = null,
                                                            recoveryTime = null,
                                                            createTime = null,
                                                            lastUpdateTime = null,
                                                            fileMD5 = finalTask.fileMD5,
                                                            fileSHA1 = finalTask.fileSHA1,
                                                            fileSHA256 = finalTask.fileSHA256,
                                                            storageId = finalTask.storageId,
                                                            fileSize = finalTask.fileSize,
                                                            fileCover = null,
                                                            referCount = null,
                                                            status = null,
                                                            transcodeStatus = null,
                                                            fileCreateTime = null,
                                                            lastReferTime = null
                                                        )

                                                        // 发送上传完成请求
                                                        val completeResponse =
                                                            myRetrofitDatasource.uploadComplete(
                                                                fileToUpload
                                                            )

                                                        if (completeResponse.code == 1) {
                                                            // 完成请求成功，更新状态为COMPLETED
                                                            transferRepository.updateTransfer(
                                                                finalTask.copy(status = TransferStatus.COMPLETED)
                                                            )
                                                            Log.d(
                                                                "TransfersViewModel",
                                                                "上传完成请求成功，状态: COMPLETED"
                                                            )
                                                        } else {
                                                            // 请求失败
                                                            Log.e(
                                                                "TransfersViewModel",
                                                                "上传完成请求失败: ${completeResponse.message}"
                                                            )
                                                        }
                                                    }
                                                } else {
                                                    // 上传失败
                                                    transferRepository.updateTransfer(
                                                        currentTask.copy(status = TransferStatus.FAILED)
                                                    )
                                                    Log.e(
                                                        "TransfersViewModel",
                                                        "文件上传失败: $message"
                                                    )
                                                }
                                            } catch (e: Exception) {
                                                // 处理异常，可能是任务已被删除
                                                Log.e(
                                                    "TransfersViewModel",
                                                    "上传完成后更新状态出错: ${e.message}"
                                                )
                                            }
                                        }
                                    },
                                    onCancelled = {
                                        // 检查此任务是否需要被取消
                                        val shouldCancel = activeUploadTasks[uploadTask.id] == true
                                        shouldCancel
                                    }
                                )
                            }
                        } else {
                            // 请求失败
                            Log.e("TransfersViewModel", "上传令牌请求失败: ${response.message}")
                            transferRepository.updateTransfer(
                                tokenTask.copy(status = TransferStatus.FAILED)
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("TransfersViewModel", "自动上传流程中捕获到异常: ${e.message}")
                e.printStackTrace()
            } finally {
                Log.d("TransfersViewModel", "========== 自动上传流程结束 ==========")
            }
        }
    }

    /**
     * 设置传输状态为等待并请求上传令牌
     * 用于支持自动化流程，将setTransferStatusToWaiting和requestUploadToken合并
     */
    fun setTransferStatusToWaitingAndRequestToken(transferId: Long) {
        Log.d("TransfersViewModel", "开始请求上传令牌，ID: $transferId")
        viewModelScope.launch {
            try {
                Log.d("TransfersViewModel", "启动协程，准备调用requestUploadToken方法")
                // 直接调用requestUploadToken方法
                requestUploadToken(transferId)
                Log.d("TransfersViewModel", "requestUploadToken方法调用完成")
            } catch (e: Exception) {
                Log.e("TransfersViewModel", "请求上传令牌失败: ${e.message}")
                e.printStackTrace()
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

                // 检查是否已经在activeUploadTasks中且标记为取消
                if (activeUploadTasks.containsKey(task.id) && activeUploadTasks[task.id] == true) {
                    Log.w("TransfersViewModel", "任务已被标记为取消或暂停，不会开始上传: ${task.id}")
                    return@launch
                }

                // 确保任务状态合理：等待状态、暂停状态或者已经是进行中状态
                if (task.status != TransferStatus.WAITING &&
                    task.status != TransferStatus.PAUSED &&
                    task.status != TransferStatus.IN_PROGRESS
                ) {
                    Log.e(
                        "TransfersViewModel",
                        "错误: 任务状态不正确，当前: ${task.status}, 需要: WAITING, PAUSED 或 IN_PROGRESS"
                    )
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
                                Log.d(
                                    "TransfersViewModel",
                                    "找到断点续传记录文件: ${file.name}, 大小: ${file.length()}"
                                )
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
                    key = uploadKey,
                    onProgress = { progress ->
                        // 检查取消标志
                        if (activeUploadTasks[task.id] == true) {
                            // 任务被暂停或取消，不继续更新进度
                            Log.d("TransfersViewModel", "任务已被暂停或取消，不更新进度: ${task.id}")
                        } else {
                            // 再次检查任务状态，确保没有被暂停或取消
                            viewModelScope.launch {
                                try {
                                    val currentTask = transferRepository.getTransferById(task.id)
                                    if (currentTask == null) {
                                        // 任务已被删除，设置取消标志并停止尝试更新
                                        Log.d(
                                            "TransfersViewModel",
                                            "任务已被删除，设置取消标志: ${task.id}"
                                        )
                                        activeUploadTasks[task.id] = true
                                        return@launch
                                    }

                                    if (currentTask.status == TransferStatus.PAUSED ||
                                        currentTask.status == TransferStatus.FAILED ||
                                        currentTask.status == TransferStatus.CANCELLING
                                    ) {
                                        // 任务已被暂停、失败或正在取消，设置取消标志
                                        Log.d(
                                            "TransfersViewModel",
                                            "检测到任务状态为 ${currentTask.status}，设置取消标志: ${task.id}"
                                        )
                                        activeUploadTasks[task.id] = true
                                        return@launch
                                    }

                                    // 更新进度
                                    val progressInt = (progress * 100).toInt()
                                    if (progressInt != task.progress) {
                                        Log.d(
                                            "TransfersViewModel",
                                            "更新任务进度: ${task.id}, $progressInt%"
                                        )
                                        transferRepository.updateTransfer(
                                            task.copy(progress = progressInt)
                                        )
                                    }
                                } catch (e: Exception) {
                                    // 处理异常，可能是由于任务已被删除
                                    Log.e("TransfersViewModel", "更新进度时出错: ${e.message}")
                                    activeUploadTasks[task.id] = true
                                }
                            }
                        }
                    },
                    onComplete = { success, message ->
                        // 上传完成后更新状态
                        viewModelScope.launch {
                            // 从活跃任务中移除
                            activeUploadTasks.remove(task.id)

                            try {
                                // 检查此任务是否在完成前被暂停或取消
                                val currentTask = transferRepository.getTransferById(task.id)
                                if (currentTask == null) {
                                    // 任务已被删除，不需要更新
                                    Log.d(
                                        "TransfersViewModel",
                                        "任务已被删除，不更新状态: ${task.id}"
                                    )
                                    return@launch
                                }

                                // 如果任务已被暂停或正在取消，保持当前状态
                                if (currentTask.status == TransferStatus.PAUSED ||
                                    currentTask.status == TransferStatus.CANCELLING
                                ) {
                                    Log.d(
                                        "TransfersViewModel",
                                        "任务状态为 ${currentTask.status}，保持当前状态: ${task.id}"
                                    )
                                    return@launch
                                }

                                // 修改为新增的状态 UPLOAD_STORAGE_COMPLETED，表示上传到存储完成，但未通知服务器
                                val newStatus =
                                    if (success) TransferStatus.UPLOAD_STORAGE_COMPLETED else TransferStatus.FAILED
                                Log.d(
                                    "TransfersViewModel",
                                    "上传${if (success) "到存储成功" else "失败"}: $message"
                                )
                                transferRepository.updateTransfer(
                                    currentTask.copy(
                                        status = newStatus,
                                        progress = if (success) 100 else currentTask.progress
                                    )
                                )
                            } catch (e: Exception) {
                                // 处理异常，可能是任务已被删除
                                Log.e("TransfersViewModel", "上传完成后更新状态出错: ${e.message}")
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

    /**
     * 发送上传完成请求
     * 在文件上传到七牛云成功后调用，通知服务器更新文件状态
     * @param transferId 传输任务ID
     */
    fun uploadComplete(transferId: Long) {
        Log.d("TransfersViewModel", "========== 开始处理上传完成请求 ==========")
        Log.d("TransfersViewModel", "传输ID: $transferId")

        viewModelScope.launch {
            try {
                // 获取传输记录
                val task = transferRepository.getTransferById(transferId)

                if (task == null) {
                    Log.e("TransfersViewModel", "错误: 找不到传输任务，ID: $transferId")
                    return@launch
                }

                // 确保任务状态是已上传到存储
                if (task.status != TransferStatus.UPLOAD_STORAGE_COMPLETED) {
                    Log.e(
                        "TransfersViewModel",
                        "错误: 任务状态不正确，当前: ${task.status}, 需要: UPLOAD_STORAGE_COMPLETED"
                    )
                    return@launch
                }

                Log.d("TransfersViewModel", "准备发送上传完成请求:")
                Log.d("TransfersViewModel", "- ID: ${task.id}")
                Log.d("TransfersViewModel", "- 文件名: ${task.fileName}")
                Log.d("TransfersViewModel", "- 文件大小: ${task.fileSize}")

                // 创建要发送的File对象
                val fileToUpload = File(
                    id = null,
                    userId = null,
                    fileId = null,
                    fileName = task.fileName,
                    fileExtension = task.fileExtension,
                    fileCategory = task.fileCategory,
                    filePid = task.filePid,
                    folderType = null,
                    deleteFlag = null,
                    recoveryTime = null,
                    createTime = null,
                    lastUpdateTime = null,
                    fileMD5 = task.fileMD5,
                    fileSHA1 = task.fileSHA1,
                    fileSHA256 = task.fileSHA256,
                    storageId = task.storageId,
                    fileSize = task.fileSize,
                    fileCover = null,
                    referCount = null,
                    status = null,
                    transcodeStatus = null,
                    fileCreateTime = null,
                    lastReferTime = null
                )

                Log.d("TransfersViewModel", "准备发送上传完成请求，要发送的数据:")
                Log.d("TransfersViewModel", "- 文件名: ${fileToUpload.fileName}")
                Log.d("TransfersViewModel", "- 扩展名: ${fileToUpload.fileExtension}")
                Log.d("TransfersViewModel", "- 文件大小: ${fileToUpload.fileSize}")
                Log.d("TransfersViewModel", "- MD5: ${fileToUpload.fileMD5}")
                Log.d("TransfersViewModel", "- SHA1: ${fileToUpload.fileSHA1}")
                Log.d("TransfersViewModel", "- SHA256: ${fileToUpload.fileSHA256}")
                Log.d("TransfersViewModel", "- 父目录ID: ${fileToUpload.filePid}")
                Log.d("TransfersViewModel", "- 文件夹类型: ${fileToUpload.folderType}")

                // 发送网络请求
                Log.d("TransfersViewModel", "正在调用网络接口 uploadComplete()...")
                val response = myRetrofitDatasource.uploadComplete(fileToUpload)

                Log.d("TransfersViewModel", "网络请求完成，响应码: ${response.code}")
                Log.d("TransfersViewModel", "响应消息: ${response.message}")

                if (response.code == 1) {
                    // 请求成功，更新状态为COMPLETED
                    Log.d("TransfersViewModel", "上传完成请求成功")
                    transferRepository.updateTransfer(
                        task.copy(status = TransferStatus.COMPLETED)
                    )
                    Log.d("TransfersViewModel", "文件状态已更新为COMPLETED")
                } else {
                    // 请求失败
                    Log.e("TransfersViewModel", "上传完成请求失败:")
                    Log.e("TransfersViewModel", "- 错误码: ${response.code}")
                    Log.e("TransfersViewModel", "- 错误消息: ${response.message}")
                }
            } catch (e: Exception) {
                Log.e("TransfersViewModel", "上传完成请求过程中捕获到异常:")
                Log.e("TransfersViewModel", "- 异常类型: ${e.javaClass.simpleName}")
                Log.e("TransfersViewModel", "- 异常信息: ${e.message}")
                Log.e("TransfersViewModel", "- 堆栈跟踪:")
                e.printStackTrace()
            } finally {
                Log.d("TransfersViewModel", "========== 上传完成请求处理结束 ==========")
            }
        }
    }
} 