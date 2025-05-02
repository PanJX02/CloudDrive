package com.panjx.clouddrive.core.modle.request

import kotlinx.serialization.Serializable

/**
 * 保存分享文件请求
 */
@Serializable
data class SaveShareFilesRequest(
    val ids: List<Long>,   // 文件ID列表
    val targetFolderId: Long,  // 目标文件夹ID
    val shareKey: String,   // 分享密钥
    val code: String? = null   // 验证码，可选
)
