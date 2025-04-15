package com.panjx.clouddrive.core.modle.response

import kotlinx.serialization.Serializable

/**
 * 文件下载响应
 */
@Serializable
data class DownloadResponse(
    val folderType: Boolean,
    val totalSize: Long?,
    val downloadFiles: List<DownloadFileInfo>
)

/**
 * 下载文件信息
 */
@Serializable
data class DownloadFileInfo(
    val url: String?,
    val folderType: Boolean,
    val size: Long?,
    val filePath: String,
    val fileName: String,
    val fileExtension: String?,
    val sha256: String?
) 