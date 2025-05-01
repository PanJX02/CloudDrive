package com.panjx.clouddrive.feature.fileRoute

import android.content.Context
import android.util.Log
import com.panjx.clouddrive.feature.transfersRoute.DownloadTransfersViewModel

/**
 * 文件操作实现类
 * 负责处理所有文件相关操作的实际逻辑
 */
class FileOperations(
    private val viewModel: FileViewModel,
    private val downloadViewModel: DownloadTransfersViewModel,
    private val context: Context,
    private val clearSelection: () -> Unit
) {
    /**
     * 下载选中的文件
     */
    fun downloadFiles(selectedFileIds: List<Long>) {
        Log.d(TAG, "================== 下载流程开始 ==================")
        Log.d(TAG, "用户点击下载按钮，选中文件数量: ${selectedFileIds.size}")
        Log.d(TAG, "选中的文件ID: $selectedFileIds")
        
        // 获取选中的文件对象
        val filesToDownload = viewModel.getSelectedFiles(selectedFileIds)
        Log.d(TAG, "获取到的文件对象数量: ${filesToDownload.size}")
        filesToDownload.forEachIndexed { index, file ->
            Log.d(TAG, "文件[$index]: id=${file.id}, 名称=${file.fileName}, 类型=${file.folderType}")
        }
        
        // 调用下载ViewModel进行下载
        Log.d(TAG, "调用DownloadTransfersViewModel.addDownloadTasks开始下载...")
        downloadViewModel.addDownloadTasks(filesToDownload, context)
        
        // 清空选中
        clearSelection()
        Log.d(TAG, "已清空选中状态")
    }

    /**
     * 移动选中的文件
     */
    fun moveFiles(selectedFileIds: List<Long>) {
        Log.d(TAG, "移动操作: $selectedFileIds")
        // TODO: 实现移动文件逻辑
        clearSelection()
    }

    /**
     * 复制选中的文件
     */
    fun copyFiles(selectedFileIds: List<Long>) {
        Log.d(TAG, "复制操作: $selectedFileIds")
        // TODO: 实现复制文件逻辑
        clearSelection()
    }

    /**
     * 添加/移除收藏
     */
    fun toggleFavorite(selectedFileIds: List<Long>) {
        Log.d(TAG, "收藏操作: $selectedFileIds")
        // TODO: 实现收藏文件逻辑
        clearSelection()
    }

    /**
     * 重命名文件/文件夹
     */
    fun renameFile(selectedFileIds: List<Long>) {
        Log.d(TAG, "重命名操作: $selectedFileIds")
        // TODO: 实现重命名文件逻辑
        clearSelection()
    }

    /**
     * 删除文件/文件夹
     */
    fun deleteFiles(selectedFileIds: List<Long>) {
        Log.d(TAG, "删除操作: $selectedFileIds")
        // TODO: 实现删除文件逻辑
        clearSelection()
    }

    /**
     * 分享文件
     */
    fun shareFiles(selectedFileIds: List<Long>) {
        Log.d(TAG, "分享操作: $selectedFileIds")
        // TODO: 实现分享文件逻辑
        clearSelection()
    }

    /**
     * 查看文件详情
     */
    fun showFileDetails(selectedFileIds: List<Long>) {
        Log.d(TAG, "查看详情操作: $selectedFileIds")
        // TODO: 实现查看文件详情逻辑
        clearSelection()
    }

    companion object {
        private const val TAG = "FileOperations"
    }
} 