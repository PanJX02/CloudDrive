package com.panjx.clouddrive.core.modle.request

import kotlinx.serialization.Serializable

/**
 * 分享请求
 */
@Serializable
data class ShareRequest(
    val userFileIds: List<Long>,
    val validType: Int
)
