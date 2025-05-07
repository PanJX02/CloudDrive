package com.panjx.clouddrive.core.modle.request

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val username: String,
    val password: String,
    val inviteCode: String? = null
) 