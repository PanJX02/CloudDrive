package com.panjx.clouddrive.core.modle.response

import kotlinx.serialization.Serializable

/**
 * 用户信息响应数据类
 */
@Serializable
data class UserInfoResponse(
    /** 用户ID */
    val userId: Int,
    /** 用户名 */
    val username: String,
    /** 昵称 */
    val nickname: String,
    /** 邮箱（可能为空） */
    val email: String? = null,
    /** 用户身份类型（0表示管理员） */
    val identity: Int,
    /** 头像URL（可能为空） */
    val avatar: String? = null,
    /** 密码（服务器返回通常为空） */
    val password: String? = null,
    /** 注册时间戳（可能为空） */
    val registerTime: Long? = null,
    /** 最后登录时间戳 */
    val lastLoginTime: Long,
    /** 用户状态（1表示正常状态） */
    val status: Int,
    /** 已使用的存储空间（字节） */
    val usedSpace: Long,
    /** 总存储空间（字节） */
    val totalSpace: Long,
    /** 可用存储空间（字节） */
    val availableSpace: Long
) 