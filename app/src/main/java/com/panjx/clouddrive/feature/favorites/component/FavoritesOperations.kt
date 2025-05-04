package com.panjx.clouddrive.feature.favorites.component

import android.content.Context
import android.util.Log
import com.panjx.clouddrive.feature.favorites.FavoritesViewModel
import com.panjx.clouddrive.feature.transfersRoute.DownloadTransfersViewModel

/**
 * 收藏夹操作实现类
 * 负责处理收藏夹相关操作的实际逻辑
 */
class FavoritesOperations(
    private val viewModel: FavoritesViewModel,
    private val downloadViewModel: DownloadTransfersViewModel,
    private val context: Context,
    private val exitSelectionMode: () -> Unit // 退出选择模式并清除选中文件
) {
    /**
     * 下载选中的文件
     */
    fun downloadFiles(selectedFileIds: List<Long>) {
        Log.d(TAG, "================== 收藏夹下载流程开始 ==================")
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
        
        // 退出选择模式
        exitSelectionMode()
        Log.d(TAG, "已退出选择模式")
    }

    /**
     * 从收藏夹中移除文件
     */
    fun removeFromFavorites(selectedFileIds: List<Long>) {
        Log.d(TAG, "移出收藏操作: $selectedFileIds")
        
        if (selectedFileIds.isEmpty()) {
            Log.d(TAG, "无选中文件，取消移出收藏操作")
            return
        }
        
        Log.d(TAG, "开始从收藏夹移除文件，数量: ${selectedFileIds.size}")
        
        // 调用ViewModel执行移出收藏
        viewModel.removeFromFavorites(selectedFileIds) { success, message ->
            if (success) {
                Log.d(TAG, "移出收藏成功: $message")
            } else {
                Log.e(TAG, "移出收藏失败: $message")
            }
        }
        
        // 退出选择模式
        exitSelectionMode()
    }

    /**
     * 移动文件
     */
    fun moveFiles(selectedFileIds: List<Long>) {
        Log.d(TAG, "移动操作: $selectedFileIds")
        // TODO: 实现移动文件逻辑
        exitSelectionMode()
    }
    
    /**
     * 复制文件
     */
    fun copyFiles(selectedFileIds: List<Long>) {
        Log.d(TAG, "复制操作: $selectedFileIds")
        // TODO: 实现复制文件逻辑
        exitSelectionMode()
    }
    
    /**
     * 添加/移除收藏
     * 根据文件的收藏状态决定是添加收藏还是取消收藏
     */
    fun toggleFavorite(selectedFileIds: List<Long>) {
        Log.d(TAG, "收藏操作: $selectedFileIds")
        
        if (selectedFileIds.isEmpty()) {
            Log.d(TAG, "没有选中的文件，取消收藏操作")
            return
        }
        
        // 获取选中的文件
        val selectedFiles = viewModel.getSelectedFiles(selectedFileIds)
        
        // 检查是否所有选中文件都已收藏
        val allFavorited = selectedFiles.all { it.favoriteFlag == 1 }
        
        Log.d(TAG, "选中的${selectedFiles.size}个文件中，所有文件都已收藏: $allFavorited")
        
        if (allFavorited) {
            // 如果所有文件都已收藏，则取消收藏
            Log.d(TAG, "所有文件都已收藏，执行取消收藏操作")
            viewModel.removeFromFavorites(selectedFileIds) { success, message ->
                if (success) {
                    Log.d(TAG, "取消收藏成功: $message")
                } else {
                    Log.e(TAG, "取消收藏失败: $message")
                }
            }
        } else {
            // 收藏夹内的文件已经是收藏状态，不需要进行额外操作
            Log.d(TAG, "文件已在收藏夹中")
        }
        
        // 退出选择模式
        exitSelectionMode()
    }
    
    /**
     * 重命名文件/文件夹
     * @param selectedFileIds 选中的文件ID列表（只能包含单个文件ID）
     * @param newName 可选参数，如果提供则直接使用该名称重命名，否则只返回当前文件信息
     * @return 返回当前文件信息和名称，用于构建UI对话框
     */
    fun renameFile(selectedFileIds: List<Long>, newName: String? = null): Pair<Long, String>? {
        Log.d(TAG, "重命名操作: $selectedFileIds, 新名称: $newName")
        
        // 只能重命名单个文件
        if (selectedFileIds.size != 1) {
            Log.d(TAG, "重命名操作需要选择单个文件，当前选中: ${selectedFileIds.size}个")
            return null
        }
        
        val fileId = selectedFileIds[0]
        // 获取当前文件名
        val currentFile = viewModel.getSelectedFiles(selectedFileIds).firstOrNull()
        val currentFileName = currentFile?.fileName ?: ""
        
        // 如果提供了新名称，则执行重命名操作
        if (!newName.isNullOrBlank() && newName != currentFileName) {
            Log.d(TAG, "执行重命名操作: fileId=$fileId, 原名称=$currentFileName, 新名称=$newName")
            
            // TODO: 实现重命名API调用
            
            // 重命名成功后退出选择模式
            exitSelectionMode()
        }
        
        // 返回当前文件ID和文件名，用于构建重命名对话框
        return Pair(fileId, currentFileName)
    }
    
    /**
     * 删除文件/文件夹
     */
    fun deleteFiles(selectedFileIds: List<Long>) {
        Log.d(TAG, "删除操作: $selectedFileIds")
        
        if (selectedFileIds.isEmpty()) {
            Log.d(TAG, "无选中文件，取消删除操作")
            return
        }
        
        Log.d(TAG, "开始删除文件，数量: ${selectedFileIds.size}")
        
        // 对于收藏夹，删除操作实际上是移出收藏
        viewModel.removeFromFavorites(selectedFileIds) { success, message ->
            if (success) {
                Log.d(TAG, "从收藏夹移除成功: $message")
            } else {
                Log.e(TAG, "从收藏夹移除失败: $message")
            }
        }
        
        // 退出选择模式
        exitSelectionMode()
    }
    
    /**
     * 分享文件
     */
    fun shareFiles(selectedFileIds: List<Long>, onShowShareDialog: (List<Long>) -> Unit) {
        Log.d(TAG, "分享操作: $selectedFileIds")
        
        if (selectedFileIds.isEmpty()) {
            Log.d(TAG, "未选择任何文件，取消分享操作")
            return
        }
        
        Log.d(TAG, "显示分享有效期选择对话框")
        
        // 调用回调函数显示分享对话框
        onShowShareDialog(selectedFileIds)
        
        // 不清空选中，由对话框处理完成后清空
    }
    
    /**
     * 查看文件详情
     */
    fun showFileDetails(selectedFileIds: List<Long>) {
        Log.d(TAG, "查看详情操作: $selectedFileIds")
        
        if (selectedFileIds.isEmpty()) {
            Log.d(TAG, "未选择任何文件，取消查看详情操作")
            return
        }
        
        if (selectedFileIds.size > 1) {
            Log.d(TAG, "选择了多个文件，暂只支持查看单个文件详情")
            // 这里可以添加提示用户只能查看单个文件详情的逻辑
            return
        }
        
        Log.d(TAG, "开始获取文件详情，fileId: ${selectedFileIds[0]}")
        
        // TODO: 实现获取文件详情API调用
        
        // 不清空选中，让用户可以继续操作
    }
    
    companion object {
        private const val TAG = "FavoritesOperations"
    }
} 