package com.panjx.clouddrive.feature.recycleBin.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 回收站操作栏组件
 * 
 * @param selectedCount 选中文件数量
 * @param onRestore 恢复文件回调
 * @param onDelete 永久删除回调
 */
@Composable
fun RecycleBinActions(
    selectedCount: Int,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    BottomAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 显示已选择的文件数量
            Text(text = "已选择 $selectedCount 项")
            
            // 操作按钮
            Row {
                // 恢复按钮
                IconButton(onClick = onRestore) {
                    Icon(
                        imageVector = Icons.Default.Restore, 
                        contentDescription = "恢复"
                    )
                }
                
                // 永久删除按钮
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever, 
                        contentDescription = "永久删除"
                    )
                }
            }
        }
    }
} 