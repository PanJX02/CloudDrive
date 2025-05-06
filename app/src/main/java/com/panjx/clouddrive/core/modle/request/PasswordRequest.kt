package com.panjx.clouddrive.core.modle.request

import kotlinx.serialization.Serializable

@Serializable
data class PasswordRequest(
    val oldPassword: String,
    val newPassword: String
)
