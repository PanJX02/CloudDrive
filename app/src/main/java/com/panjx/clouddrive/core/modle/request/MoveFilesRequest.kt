package com.panjx.clouddrive.core.modle.request

import kotlinx.serialization.Serializable

/**
 * 移动文件请求模型
 */
@Serializable
data class MoveFilesRequest(
    val ids: List<Long>,       // 要移动的文件ID列表
    val targetFolderId: Long   // 目标文件夹ID
) 