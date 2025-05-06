package com.panjx.clouddrive.core.modle.request

import kotlinx.serialization.Serializable

@Serializable
data class UserInfoRequest(
    /** 用户昵称 */
    val nickname: String,
    /** 用户邮箱 */
    val email: String,
    /** 用户头像URL */
    val avatar: String
)
