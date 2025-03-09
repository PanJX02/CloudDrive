package com.panjx.clouddrive.core.modle.response

import kotlinx.serialization.Serializable

// 分页元数据
@Serializable
data class NetworkPageMeta(
    // 总数量
    val total: Int? = null,
    // 总页数
    val totalPage: Int? = null,
    // 每页数量
    val pageSize: Int? = null,
    // 当前页码
    val page: Int? = null,
)
