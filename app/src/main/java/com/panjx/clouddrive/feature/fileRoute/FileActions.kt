package com.panjx.clouddrive.feature.fileRoute

// Data class to hold action callbacks
data class FileActions(
    val onDownloadClick: () -> Unit = {},
    val onMoveClick: () -> Unit = {},
    val onCopyClick: () -> Unit = {},
    val onFavoriteClick: () -> Unit = {},
    val onRenameClick: () -> Unit = {},
    val onDeleteClick: () -> Unit = {},
    val onShareClick: () -> Unit = {},
    val onDetailsClick: () -> Unit = {},
    val hasSelection: Boolean = false, // Indicates if there are selected items
    val selectedFileIds: List<Long> = emptyList() // 选中的文件ID列表
) 