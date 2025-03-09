package com.panjx.clouddrive.core.modle

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.serialization.Serializable

/**
 * 文件/目录实体类
 *
 * 用于表示云存储系统中的文件或目录元数据信息
 *
 * @property id 文件唯一标识符，通常为UUID格式
 * @property name 文件/目录名称（包含扩展名）
 * @property size 文件大小（字节单位），目录时通常为0
 * @property type 文件类型标识（如：text/plain, image/png），目录可设为"directory"
 * @property parentId 所属父目录ID，根目录时为空字符串
 * @property path 文件完整存储路径（虚拟路径，非物理路径）
 * @property createTime 创建时间戳（毫秒级）
 * @property updateTime 最后修改时间戳（毫秒级）
 * @property isDir 是否为目录标识，true表示目录，false表示文件
 * @property isSelected 选择状态（主要用于UI交互），默认未选中
 */
@Serializable
data class File(
    val id: String,
    val name: String,
    val size: Long,
    val type: String,
    val parentId: String,
    val path: String,
    val createTime: Long,
    val updateTime: Long,
    val isDir: Boolean,
)
