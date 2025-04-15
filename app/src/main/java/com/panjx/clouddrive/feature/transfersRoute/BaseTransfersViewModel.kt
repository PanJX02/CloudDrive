package com.panjx.clouddrive.feature.transfersRoute

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panjx.clouddrive.data.database.TransferEntity
import com.panjx.clouddrive.data.repository.TransferRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * 传输任务基础ViewModel类
 * 提供了传输任务相关的共用功能
 */
abstract class BaseTransfersViewModel(
    protected val transferRepository: TransferRepository
) : ViewModel() {

    // 使用ConcurrentHashMap来跟踪活跃的传输任务
    // Key是传输任务的ID，Value是取消标志
    protected val activeTransferTasks = ConcurrentHashMap<Long, Boolean>()

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

    /**
     * 暂停或恢复传输
     */
    abstract fun pauseOrResumeTransfer(transfer: TransferEntity, context: Context? = null)

    /**
     * 取消传输任务
     */
    open fun cancelTransfer(transfer: TransferEntity) {
        viewModelScope.launch {
            // 首先检查是否有活跃传输
            if (activeTransferTasks.containsKey(transfer.id)) {
                // 设置取消标志为true
                activeTransferTasks[transfer.id] = true
                Log.d("TransfersViewModel", "设置任务取消标志: ${transfer.id}")

                // 重要：先更新任务状态为取消中，避免传输线程继续更新
                // 更新数据库中的状态为取消中
                val updatedTask = transfer.copy(status = TransferStatus.CANCELLING)
                transferRepository.updateTransfer(updatedTask)
                Log.d("TransfersViewModel", "更新任务状态为取消中: ${transfer.id}")

                // 如果任务正在进行中，先等待一段时间让传输操作有机会响应取消标志
                if (transfer.status == TransferStatus.IN_PROGRESS) {
                    Log.d("TransfersViewModel", "等待传输操作响应取消标志...")
                    // 等待3秒，让传输操作有足够的时间响应取消标志并停止尝试更新数据库
                    kotlinx.coroutines.delay(3000)
                }
            }

            // 清理与此任务相关的取消标志
            activeTransferTasks.remove(transfer.id)

            // 最后从数据库中删除任务
            transferRepository.deleteTransfer(transfer)
            Log.d("TransfersViewModel", "从数据库中删除任务: ${transfer.id}")
        }
    }

    /**
     * 清除已完成的传输
     */
    fun clearCompletedTransfers() {
        viewModelScope.launch {
            transferRepository.deleteCompletedTransfers()
        }
    }
}
