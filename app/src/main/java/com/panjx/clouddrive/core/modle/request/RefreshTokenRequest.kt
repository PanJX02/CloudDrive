package com.panjx.clouddrive.core.modle.request

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
) 