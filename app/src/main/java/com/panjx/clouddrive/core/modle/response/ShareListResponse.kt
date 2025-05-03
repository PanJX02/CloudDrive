package com.panjx.clouddrive.core.modle.response

import kotlinx.serialization.Serializable

@Serializable
data class ShareListResponse(
    val shareId: Long,
    val shareName: String? = null,
    val fileCount: Int,
    val shareTime: Long,
    val validType: Int,
    val expireTime: Long? = null,
    val isExpired: Int,
    val showCount: Int,
    val code: String,
    val shareKey: String,
    val shareKeyWithCode: String
)
