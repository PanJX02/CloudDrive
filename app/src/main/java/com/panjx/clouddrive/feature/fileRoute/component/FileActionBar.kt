package com.panjx.clouddrive.feature.fileRoute.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 文件选中后显示的操作栏 (两行)
 */
@Composable
fun FileActionBar(
    modifier: Modifier = Modifier,
    onDownloadClick: () -> Unit,
    onMoveClick: () -> Unit,
    onCopyClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareClick: () -> Unit,
    onDetailsClick: () -> Unit,
    showSaveButton: Boolean = false,
    onSaveClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 8.dp)
    ) {
        // First Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionItem(icon = Icons.Default.Download, label = "下载", onClick = onDownloadClick)
            ActionItem(icon = Icons.AutoMirrored.Filled.DriveFileMove, label = "移动", onClick = onMoveClick)
            ActionItem(icon = Icons.Default.ContentCopy, label = "复制", onClick = onCopyClick)
            ActionItem(icon = Icons.Default.FavoriteBorder, label = "收藏", onClick = onFavoriteClick)
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Second Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showSaveButton) {
                // 在分享内容页面添加保存按钮替换重命名
                ActionItem(icon = Icons.Default.SaveAlt, label = "保存", onClick = onSaveClick)
            } else {
                ActionItem(icon = Icons.Default.DriveFileRenameOutline, label = "重命名", onClick = onRenameClick)
            }
            ActionItem(icon = Icons.Default.DeleteOutline, label = "删除", onClick = onDeleteClick)
            ActionItem(icon = Icons.Default.Share, label = "分享", onClick = onShareClick)
            ActionItem(icon = Icons.Default.Info, label = "详情", onClick = onDetailsClick)
        }
    }
}

/**
 * Helper composable for an action item with icon and text label.
 */
@Composable
private fun ActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
} 