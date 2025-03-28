package com.panjx.clouddrive.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.panjx.clouddrive.feature.transfersRoute.TransferStatus

@Entity(tableName = "transfers")
data class TransferEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileName: String,
    val progress: Int,
    val status: TransferStatus,
    val type: TransferType, // 上传或下载
    val filePath: String, // 本地文件路径
    val remoteUrl: String = "", // 远程URL
    val fileSize: Long = 0, // 文件大小(字节)
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class TransferType {
    UPLOAD,
    DOWNLOAD
} 