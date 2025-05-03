package com.panjx.clouddrive.core.modle.response

data class Announcement (
    val id: Int,
    val title: String,
    val content: String,
    val publishTime: Long,
    val expiryTime: Long?,
    val adminId: Int?,
    val importance: Int,
    val status: Int?,
    val viewCount: Int,
    val createdAt: Long?,
    val updatedAt: Long?
)