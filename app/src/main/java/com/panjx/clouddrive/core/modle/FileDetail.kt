package com.panjx.clouddrive.core.modle

import kotlinx.serialization.Serializable

/**
 * 文件详情实体类
 */
@Serializable
data class FileDetail(
    val id: Int? = null,
    val fileName: String? = null,
    val fileExtension: String? = null,
    val fileCategory: String? = null,
    val fileSize: Long? = null,
    val folderType: Int? = null,
    val filePath: String? = null,
    val createTime: Long? = null,
    val lastUpdateTime: Long? = null,
    val fileCount: Int? = null,
    val folderCount: Int? = null,
    val favoriteFlag: Int? = null,
    val fileMd5: String? = null,
    val fileSha1: String? = null,
    val fileSha256: String? = null
)
