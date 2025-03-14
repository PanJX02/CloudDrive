package com.panjx.clouddrive.core.modle.request

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val email: String,
    val password: String,
    val nickname: String = "" // 注册时使用，登录时可为空
) 