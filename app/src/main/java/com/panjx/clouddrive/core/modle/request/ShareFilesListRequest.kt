package com.panjx.clouddrive.core.modle.request

data class ShareFilesListRequest(
    val shareKey: String,
    val code: String,
    val folderId: Long? = null,
)
