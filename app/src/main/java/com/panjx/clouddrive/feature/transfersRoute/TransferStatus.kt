package com.panjx.clouddrive.feature.transfersRoute

enum class TransferStatus {
    WAITING,              // 等待上传/下载
    IN_PROGRESS,          // 上传/下载中
    PAUSED,               // 已暂停
    COMPLETED,            // 已完成
    FAILED,               // 失败
    CALCULATING_HASH,     // 计算哈希中 (只用于上传)
    HASH_CALCULATED,      // 哈希计算完成 (只用于上传)
    UPLOAD_STORAGE_COMPLETED, // 上传到存储完成，但未通知服务器完成
    CANCELLING,           // 正在取消中 (等待上传/下载线程结束)
    WAITING_FOR_PERMISSION,
} 