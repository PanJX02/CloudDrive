package com.panjx.clouddrive.feature.favorites.component

/**
 * 收藏夹操作集合，包含收藏夹页面所有可执行的操作
 */
data class FavoritesActions(
    // 下载文件
    val onDownloadClick: () -> Unit,
    // 移动文件
    val onMoveClick: () -> Unit,
    // 复制文件
    val onCopyClick: () -> Unit,
    // 收藏/取消收藏
    val onFavoriteClick: () -> Unit,
    // 重命名文件
    val onRenameClick: () -> Unit,
    // 从收藏夹移除/删除
    val onRemoveFromFavoritesClick: () -> Unit,
    // 分享文件
    val onShareClick: () -> Unit,
    // 查看文件详情
    val onDetailsClick: () -> Unit,
    // 选择状态
    val hasSelection: Boolean,
    // 当前选中的文件ID列表
    val selectedFileIds: List<Long>
) 