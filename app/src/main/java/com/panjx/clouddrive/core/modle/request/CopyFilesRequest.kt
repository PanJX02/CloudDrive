package com.panjx.clouddrive.core.modle.request

import kotlinx.serialization.Serializable

/**
 * 复制文件请求模型
 */
@Serializable
data class CopyFilesRequest(
    val ids: List<Long>,       // 要复制的文件ID列表
    val targetFolderId: Long   // 目标文件夹ID
) 