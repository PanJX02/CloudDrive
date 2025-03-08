package com.panjx.clouddrive.core.modle.response

// 解析网络响应
data class NetworkResponse<T> (
    // 状态码
    val code: Int? = null,
    // 消息
    val message: String? = null,
    // 数据
    val data: T? = null
)