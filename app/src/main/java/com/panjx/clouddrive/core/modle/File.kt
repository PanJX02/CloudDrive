package com.panjx.clouddrive.core.modle

import kotlinx.serialization.Serializable

/**
 * 文件/目录实体类
 *
 * 用于表示云存储系统中的文件或目录元数据信息
 *
 * @property id 关联ID
 * @property userId 用户ID
 * @property fileId 文件ID，文件夹ID为null
 * @property fileName 用户定义的文件名，不包含扩展名
 * @property fileExtension 文件后缀，如 pdf, doc, mp4 等
 * @property fileCategory MIME 类型
 * @property filePid 父目录ID，根目录为0
 * @property folderType 0:文件 1:目录
 * @property deleteFlag 0:删除 1:回收站 2:正常
 * @property recoveryTime 回收站过期时间
 * @property createTime 关联创建时间
 * @property lastUpdateTime 最后更新时间
 * @property fileMD5 文件MD5值
 * @property fileSHA1 文件SHA1值
 * @property fileSHA256 文件SHA256值
 * @property storageId 关联存储配置ID（通过storage_config表获取endpoint/region/bucket）
 * @property fileSize 文件大小（字节）
 * @property fileCover 文件封面
 * @property referCount 引用计数
 * @property status 1:正常 2:待删除
 * @property transcodeStatus 转码状态: 0-未转码 1-转码中 2-转码成功 3-转码失败
 * @property fileCreateTime 首次上传时间
 * @property lastReferTime 最后引用时间
 */
@Serializable
data class File(
    val id: Long,
    val userId: Long?,
    val fileId: Long?,
    val fileName: String,
    val fileExtension: String?,
    val fileCategory: String?,
    val filePid: Long?,
    val folderType: Int,
    val deleteFlag: Int,
    val recoveryTime: Long?,
    val createTime: Long?,
    val lastUpdateTime: Long?,
    val fileMD5: String?,
    val fileSHA1: String?,
    val fileSHA256: String?,
    val storageId: Int?,
    val fileSize: Long?,
    val fileCover: String?,
    val referCount: Int?,
    val status: Int?,
    val transcodeStatus: Int?,
    val fileCreateTime: Long?,
    val lastReferTime: Long?
)