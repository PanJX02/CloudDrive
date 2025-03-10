package com.panjx.clouddrive.core.modle.response

import kotlinx.serialization.Serializable

@Serializable
data class LoginData(
    val userId: String,
    val token: String
) 