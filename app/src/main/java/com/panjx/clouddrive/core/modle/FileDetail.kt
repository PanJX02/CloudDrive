package com.panjx.clouddrive.core.modle

import kotlinx.serialization.Serializable

/**
 * 文件详情实体类
 */
@Serializable
data class FileDetail(
    val id: Int,
    val fileName: String,
    val fileExtension: String?,
    val fileCategory: Int?,
    val fileSize: Long,
    val folderType: Int,
    val filePath: String?,
    val createTime: Long,
    val lastUpdateTime: Long,
    val fileCount: Int,
    val folderCount: Int,
    val favoriteFlag: Int,
    val fileMd5: String?,
    val fileSha1: String?,
    val fileSha256: String?
)
