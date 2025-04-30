package com.panjx.clouddrive.feature.fileRoute.component

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.panjx.clouddrive.feature.transfersRoute.TransfersViewModel
import com.panjx.clouddrive.util.FileUtils
import kotlinx.coroutines.launch

/**
 * 文件选择器组件
 */
class FilePicker(
    private val transfersViewModel: TransfersViewModel,
    private val currentDirId: Long,
    private val context: Context
) {
    /**
     * 启动文件选择器
     */
    @Composable
    fun createLauncher(onFilePicked: () -> Unit = {}): () -> Unit {
        val coroutineScope = rememberCoroutineScope()
        
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
            onResult = { uri: Uri? ->
                uri?.let {
                    handleFileSelection(it, coroutineScope)
                }
                onFilePicked()
            }
        )
        
        return { launcher.launch(arrayOf("*/*")) }
    }
    
    /**
     * 处理文件选择结果
     */
    private fun handleFileSelection(uri: Uri, coroutineScope: kotlinx.coroutines.CoroutineScope) {
        try {
            val contentResolver = context.contentResolver
            val takeFlags: Int = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uri, takeFlags)
            Log.d("FilePicker", "成功获取持久化URI权限: $uri")
        } catch (e: SecurityException) {
            Log.e("FilePicker", "无法获取持久化URI权限: $uri", e)
            return
        }
        
        val fileInfo = FileUtils.getFileInfoFromUri(context, uri)
        val fullFileName = fileInfo["name"] as? String ?: "未知文件"
        val extension = fileInfo["extension"] as? String ?: ""
        val fileName = if (extension.isNotEmpty() && fullFileName.endsWith(".$extension", ignoreCase = true)) {
            fullFileName.substring(0, fullFileName.length - extension.length - 1)
        } else {
            fullFileName
        }
        
        coroutineScope.launch {
            transfersViewModel.autoUploadProcess(
                uri = uri,
                fileName = fileName,
                fileSize = fileInfo["size"] as? Long ?: 0L,
                fileExtension = extension,
                fileCategory = fileInfo["mimeType"] as? String ?: "",
                filePid = currentDirId,
                context = context
            )
        }
    }
} 