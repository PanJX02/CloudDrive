package com.panjx.clouddrive.core.modle.response

import kotlinx.serialization.Serializable

@Serializable
data class UploadResponse(
    val fileExists: Boolean,
    val uploadToken:String?=null,
)
