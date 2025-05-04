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
    val hasSelection: Boolean = false, // 表示是否处于选择模式，即使没有选择文件
    val selectedFileIds: List<Long> = emptyList() // 选中的文件ID列表
) 