package com.panjx.clouddrive.feature.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 分享操作底部菜单
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheet(
    shareItem: SharedItem,
    onDismiss: () -> Unit,
    onViewShareKeyClick: (SharedItem) -> Unit,
    onCancelShareClick: (Long) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = shareItem.name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            ListItem(
                headlineContent = { Text("查看分享密钥") },
                leadingContent = { Icon(Icons.Default.ContentCopy, "查看分享密钥") },
                modifier = Modifier.clickable { 
                    onDismiss()
                    onViewShareKeyClick(shareItem)
                }
            )
            ListItem(
                headlineContent = { Text("取消分享") },
                leadingContent = { Icon(Icons.Default.Delete, "取消分享") },
                modifier = Modifier.clickable { 
                    onDismiss()
                    onCancelShareClick(shareItem.shareId)
                }
            )
        }
    }
} 