package com.panjx.clouddrive.core.modle.request

import kotlinx.serialization.Serializable

/**
 * 文件下载请求
 */
@Serializable
data class DownloadRequest(
    val id: Long, // 文件ID
) 