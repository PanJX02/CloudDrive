package com.panjx.clouddrive.core.modle.request

import kotlinx.serialization.Serializable

/**
 * 创建文件夹请求模型
 */
@Serializable
data class CreateFolderRequest(
    val folderName: String,  // 文件夹名称
    val parentId: Long       // 父文件夹ID
) 