package com.panjx.clouddrive.core.modle

import kotlinx.serialization.Serializable

/**
 * 文件/目录实体类
 *
 * 用于表示云存储系统中的文件或目录元数据信息
 *
 * @property id 文件记录唯一标识符
 * @property userId 用户ID
 * @property fileId 文件ID
 * @property fileSHA256 文件SHA256值
 * @property fileName 文件名称
 * @property fileExtension 文件扩展名
 * @property fileCategory 文件类别
 * @property fileSize 文件大小
 * @property filePid 父文件ID
 * @property folderType 文件夹类型
 * @property deleteFlag 删除标记
 * @property recoveryTime 恢复时间
 * @property createTime 创建时间戳（毫秒级）
 * @property lastUpdateTime 最后更新时间戳（毫秒级）
 */
@Serializable
data class File(
    val id: Long,
    val userId: Long,
    val fileId: Long,
    val fileSHA256: String?,
    val fileName: String,
    val fileExtension: String?,
    val fileCategory: String?,
    val fileSize: String?,
    val filePid: Long,
    val folderType: Int,
    val deleteFlag: Int,
    val recoveryTime: Long,
    val createTime: Long,
    val lastUpdateTime: Long
)
