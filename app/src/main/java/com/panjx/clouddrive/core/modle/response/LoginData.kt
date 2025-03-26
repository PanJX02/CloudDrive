package com.panjx.clouddrive.core.modle.response

import kotlinx.serialization.Serializable

@Serializable
data class LoginData(
    val accessToken: String,
    val refreshToken: String? = null
) 