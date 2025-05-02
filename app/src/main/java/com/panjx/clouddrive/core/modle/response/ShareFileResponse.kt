package com.panjx.clouddrive.core.modle.response

import kotlinx.serialization.Serializable

/**
 * 分享文件响应
 */
@Serializable
data class ShareResponse(
    val shareKey: String,
    val shareKeyWithCode: String,
    val code: String,
    val shareName: String
)
