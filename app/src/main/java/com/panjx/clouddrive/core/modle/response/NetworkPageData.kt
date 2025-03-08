package com.panjx.clouddrive.core.modle.response

// 网络效应分页模型
data class NetworkPageData<T>(
    // 分页数据
    val list: List<T>? = null,
    // 分页信息
    val pageData: NetworkPageMeta? = null,
)
