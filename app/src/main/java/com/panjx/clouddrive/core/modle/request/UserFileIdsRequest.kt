package com.panjx.clouddrive.core.modle.request

import kotlinx.serialization.Serializable

@Serializable
data class UserFileIdsRequest(
    val ids: List<Long>   // 文件ID列表
)
