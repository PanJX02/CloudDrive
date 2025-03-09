package com.panjx.clouddrive.core.modle.response

import kotlinx.serialization.Serializable

// 解析网络响应
@Serializable
data class NetworkResponse<T> (
    // 状态码
    val code: Int? = null,
    // 消息
    val message: String? = null,
    // 数据
    val data: T? = null
)