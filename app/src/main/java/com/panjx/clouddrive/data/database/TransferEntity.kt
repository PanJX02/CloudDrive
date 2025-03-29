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
    val createdAt: Long = System.currentTimeMillis(), // 创建时间
    val updatedAt: Long = System.currentTimeMillis(), // 更新时间
    
    // 添加来自File.kt的字段
    val userId: Long? = null, // 用户ID
    val fileId: Long? = null, // 文件ID
    val fileExtension: String? = null, // 文件扩展名
    val fileCategory: String? = null, // 文件分类
    val filePid: Long? = null, // 文件父ID
    val folderType: Int = 0, // 默认为文件类型
    val deleteFlag: Int = 2, // 默认为正常
    val fileMD5: String? = null, // 文件MD5
    val fileSHA1: String? = null, // 文件SHA1
    val fileSHA256: String? = null, // 文件SHA256
    val storageId: Int? = null, // 存储ID
    val fileCover: String? = null, // 文件封面
    val referCount: Int? = null, // 引用计数
    val fileStatus: Int? = 1, // 默认为正常
    val transcodeStatus: Int? = 0, // 默认未转码
    
    // 添加来自UploadResponse.kt的字段
    val domain: String? = null, // 存储域名信息，Room不支持List<String>，所以这里用String存储并用分隔符分割
    val uploadToken: String? = null // 上传令牌
)

enum class TransferType {
    UPLOAD,
    DOWNLOAD
} 