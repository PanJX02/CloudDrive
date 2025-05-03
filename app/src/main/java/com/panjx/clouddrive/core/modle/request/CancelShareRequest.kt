package com.panjx.clouddrive.core.modle.request

import kotlinx.serialization.Serializable

@Serializable
data class CancelShareRequest(
    val shareKey: String,
    val code: String? = null
)