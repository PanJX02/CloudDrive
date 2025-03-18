package com.panjx.clouddrive.core.modle.response

import kotlinx.serialization.Serializable

// 解析网络响应
@Serializable
data class NetworkResponse<T>(
    val code: Int,
    val message: String? = null,
    val data: T? = null
)