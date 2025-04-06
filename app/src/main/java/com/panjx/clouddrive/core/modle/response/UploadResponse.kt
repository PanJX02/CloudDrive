package com.panjx.clouddrive.core.modle.response

import kotlinx.serialization.Serializable

@Serializable
data class UploadResponse(
    val fileExists: Boolean,
    val domain: List<String>?=null,
    val storageId: Int?=null,
    val uploadToken:String?=null,
)