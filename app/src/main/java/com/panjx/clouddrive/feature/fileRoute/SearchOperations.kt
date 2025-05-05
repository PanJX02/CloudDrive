package com.panjx.clouddrive.feature.fileRoute

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import com.panjx.clouddrive.core.modle.FileDetail
import com.panjx.clouddrive.core.modle.response.ShareResponse
import com.panjx.clouddrive.feature.fileRoute.viewmodel.FileOperationViewModel
import com.panjx.clouddrive.feature.fileRoute.viewmodel.SearchViewModel
import com.panjx.clouddrive.feature.transfersRoute.DownloadTransfersViewModel

/**
 * 搜索页面文件操作实现类
 */
class SearchOperations(
    private val searchViewModel: SearchViewModel,
    private val operationViewModel: FileOperationViewModel,
    private val downloadViewModel: DownloadTransfersViewModel,
    private val context: Context,
    private val exitSelectionMode: () -> Unit,
    // 对话框状态管理 - 从外部传入MutableState以控制对话框显示
    private val showRenameDialog: MutableState<Boolean>,
    private val fileToRename: MutableState<Pair<Long, String>?>,
    private val newFileName: MutableState<String>,
    private val showFileDetailDialog: MutableState<Boolean>,
    private val fileDetail: MutableState<FileDetail?>,
    private val isLoadingFileDetail: MutableState<Boolean>,
    private val fileDetailErrorMessage: MutableState<String>,
    private val showShareOptionsDialog: MutableState<Boolean>,
    private val filesToShare: MutableState<List<Long>>,
    private val showShareResultDialog: MutableState<Boolean>,
    private val shareResponse: MutableState<ShareResponse?>,
    // 消息显示
    private val showMessage: (String) -> Unit
) {

    private fun getSelectedFileIds(): List<Long> = searchViewModel.uiState.value.selectedFiles

    private fun getSelectedFiles(selectedIds: List<Long>): List<com.panjx.clouddrive.core.modle.File> {
        val currentFiles = searchViewModel.uiState.value.currentFiles
        return currentFiles.filter { selectedIds.contains(it.id) }
    }
    
    // 刷新当前视图（重新执行搜索或加载文件夹）
    private fun refreshCurrentView() {
        val currentDirId = searchViewModel.uiState.value.navigationPath.lastOrNull()?.first ?: 0L
        if (currentDirId == 0L) {
            searchViewModel.performSearch() // 刷新搜索结果
        } else {
            // TODO: 实现加载文件夹内容的逻辑，如果SearchViewModel支持的话
            Log.w(TAG, "刷新文件夹内容的功能尚未在SearchOperations中完全实现")
             searchViewModel.performSearch() // 暂时重新搜索
        }
    }

    fun downloadFiles() {
        val selectedFileIds = getSelectedFileIds()
        Log.d(TAG, "下载操作: $selectedFileIds")
        if (selectedFileIds.isEmpty()) return

        val filesToDownload = getSelectedFiles(selectedFileIds)
        Log.d(TAG, "开始下载 ${filesToDownload.size} 个文件")
        downloadViewModel.addDownloadTasks(filesToDownload, context)
        showMessage("已添加到下载列表")
        exitSelectionMode()
    }

    fun moveFiles() {
        val selectedFileIds = getSelectedFileIds()
        Log.d(TAG, "移动操作: $selectedFileIds")
        // TODO: 实现移动文件逻辑，需要选择目标文件夹
         showMessage("移动功能暂未实现")
        // exitSelectionMode()
    }

    fun copyFiles() {
        val selectedFileIds = getSelectedFileIds()
        Log.d(TAG, "复制操作: $selectedFileIds")
        // TODO: 实现复制文件逻辑，需要选择目标文件夹
         showMessage("复制功能暂未实现")
        // exitSelectionMode()
    }

    fun toggleFavorite() {
        val selectedFileIds = getSelectedFileIds()
        Log.d(TAG, "收藏操作: $selectedFileIds")
        if (selectedFileIds.isEmpty()) return

        val selectedFiles = getSelectedFiles(selectedFileIds)
        val allFavorited = selectedFiles.all { it.favoriteFlag == 1 }

        val operation: (List<Long>, (Boolean, String) -> Unit) -> Unit = if (allFavorited) {
            Log.d(TAG, "执行取消收藏操作")
            operationViewModel::removeFromFavorites
        } else {
            Log.d(TAG, "执行添加收藏操作")
            operationViewModel::addToFavorites
        }

        operation(selectedFileIds) { success, message ->
            showMessage(message)
            if (success) {
                refreshCurrentView() // 刷新视图以更新收藏状态
                 exitSelectionMode()
            }
        }
       
    }

    fun renameFile(fileId: Long? = null, newName: String? = null) {
        val selectedFileIds = getSelectedFileIds()
        val targetFileId = fileId ?: selectedFileIds.firstOrNull()

        if (targetFileId == null) {
             showMessage("未选择文件")
            return
        }
        
        if (selectedFileIds.size > 1 && fileId == null) {
             showMessage("一次只能重命名一个文件")
            return
        }

        if (newName == null) {
            // 触发显示重命名对话框
            val fileToRenameInfo = getSelectedFiles(listOf(targetFileId)).firstOrNull()
            if (fileToRenameInfo?.id != null && fileToRenameInfo.fileName != null) {
                this.fileToRename.value = Pair(fileToRenameInfo.id, fileToRenameInfo.fileName)
                this.newFileName.value = fileToRenameInfo.fileName
                this.showRenameDialog.value = true
                Log.d(TAG,"准备重命名: ${this.fileToRename.value}")
            } else {
                 showMessage("无法获取文件信息进行重命名")
            }
        } else {
            // 执行重命名
            operationViewModel.renameFile(targetFileId, newName) { success, message ->
                showMessage(message)
                if (success) {
                    this.showRenameDialog.value = false // 关闭对话框
                    refreshCurrentView()
                    exitSelectionMode()
                }
            }
        }
    }

    fun deleteFiles() {
        val selectedFileIds = getSelectedFileIds()
        Log.d(TAG, "删除操作: $selectedFileIds")
        if (selectedFileIds.isEmpty()) return

        operationViewModel.deleteFiles(selectedFileIds) { success, message ->
            showMessage(message)
            if (success) {
                refreshCurrentView()
                exitSelectionMode()
            }
        }
    }

    fun shareFiles() {
        val selectedFileIds = getSelectedFileIds()
        Log.d(TAG, "分享操作: $selectedFileIds")
        if (selectedFileIds.isEmpty()) return

        filesToShare.value = selectedFileIds
        showShareOptionsDialog.value = true
        // 注意：退出选择模式的操作现在由对话框完成时触发
    }

    fun showFileDetails() {
        val selectedFileIds = getSelectedFileIds()
        Log.d(TAG, "查看详情操作: $selectedFileIds")
        if (selectedFileIds.isEmpty()) {
             showMessage("请选择一个文件查看详情")
             return
        }
         if (selectedFileIds.size > 1) {
             showMessage("一次只能查看一个文件的详情")
             return
         }

        fileDetailErrorMessage.value = ""
        fileDetail.value = null
        isLoadingFileDetail.value = true
        showFileDetailDialog.value = true

        operationViewModel.getFileDetails(selectedFileIds) { success, message, detail ->
            isLoadingFileDetail.value = false
            if (success && detail != null) {
                fileDetail.value = detail
            } else {
                fileDetailErrorMessage.value = message
            }
             // 保持选择模式，允许用户继续操作或关闭详情
        }
    }

    // --- 分享相关回调 ---
    fun performShare(validType: Int) {
         val ids = filesToShare.value
         if (ids.isEmpty()) return

         Log.d(TAG, "执行分享: fileIds=${ids.joinToString()}, validType=$validType")
         operationViewModel.shareFile(ids, validType) { success, message, response ->
             if (success && response != null) {
                 this.shareResponse.value = response
                 this.showShareResultDialog.value = true // 显示分享结果对话框
                 Log.d(TAG, "分享成功，显示结果对话框")
             } else {
                 showMessage("分享失败: $message")
                 Log.e(TAG, "分享失败: $message")
             }
             this.showShareOptionsDialog.value = false // 关闭选项对话框
         }
     }
    
     // 复制分享链接到剪贴板
     fun copyToClipboard(text: String, isWithCode: Boolean) {
         val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
         val clip = android.content.ClipData.newPlainText("分享链接", text)
         clipboard.setPrimaryClip(clip)
         val message = if (isWithCode) "已复制自带提取码的链接到剪贴板" else "已复制链接和提取码到剪贴板"
         Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
     }
    
     // 分享结果对话框关闭时的处理
     fun onShareResultDialogDismiss() {
         showShareResultDialog.value = false
         shareResponse.value = null
         exitSelectionMode() // 分享流程结束，退出选择模式
     }


    companion object {
        private const val TAG = "SearchOperations"
    }
} 