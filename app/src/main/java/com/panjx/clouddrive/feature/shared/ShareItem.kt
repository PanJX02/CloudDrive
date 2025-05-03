package com.panjx.clouddrive.feature.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 分享列表项组件
 */
@Composable
fun ShareListItem(
    shareItem: SharedItem,
    onClick: () -> Unit,
    onMoreClick: (SharedItem) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧图标
        val icon = if (shareItem.type.contains("文件夹")) {
            Icons.Default.Folder
        } else {
            Icons.Default.InsertDriveFile
        }
        
        // 根据文件夹类型设置不同颜色
        val iconTint = if (shareItem.type.contains("文件夹")) {
            MaterialTheme.colorScheme.secondary
        } else {
            MaterialTheme.colorScheme.tertiary
        }
        
        Icon(
            imageVector = icon,
            contentDescription = shareItem.type,
            modifier = Modifier.size(40.dp),
            tint = iconTint
        )
        
        // 中间内容
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // 第一行：文件名
            Text(
                text = shareItem.name,
                style = MaterialTheme.typography.bodyLarge
            )
            
            // 第二行：分享时间和过期信息
            val validTypeText = when (shareItem.validType) {
                0 -> "1天有效"
                1 -> "7天有效"
                2 -> "30天有效"
                3 -> "永久有效"
                else -> ""
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "分享于 ${shareItem.shareTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (validTypeText.isNotEmpty()) {
                    Text(
                        text = " · $validTypeText",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = " · ${shareItem.views}次浏览",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // 右侧更多菜单
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "更多操作",
            modifier = Modifier
                .size(24.dp)
                .clickable { onMoreClick(shareItem) }
        )
    }
} 