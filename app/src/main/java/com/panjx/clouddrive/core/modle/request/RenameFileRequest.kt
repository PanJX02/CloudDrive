package com.panjx.clouddrive.core.modle.request

import kotlinx.serialization.Serializable

@Serializable
data class RenameFileRequest (
    val id: Long,
    val newFileName: String
)