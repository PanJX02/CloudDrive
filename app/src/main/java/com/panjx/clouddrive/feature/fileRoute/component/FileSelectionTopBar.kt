package com.panjx.clouddrive.feature.fileRoute.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 文件选择状态下的顶部栏
 * 
 * @param selectedCount 已选择的文件数量
 * @param onClearSelection 清除选择的回调
 * @param onSelectAllClick 全选或取消全选按钮的回调
 * @param isAllSelected 是否已全选
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileSelectionTopBar(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onSelectAllClick: () -> Unit,
    isAllSelected: Boolean = false
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = fileOperationBackgroundColor // 使用与FileActionBar相同的背景色
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 中间显示已选择文件数量
                Text(
                    text = "已选择 $selectedCount 项",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                // 全选/取消全选按钮
                IconButton(onClick = onSelectAllClick) {
                    Icon(
                        imageVector = if (isAllSelected) Icons.Filled.ClearAll else Icons.Filled.SelectAll,
                        contentDescription = if (isAllSelected) "取消全选" else "全选"
                    )
                }
            }
        },
        navigationIcon = {
            // 左侧X按钮，用于退出选择模式
            IconButton(onClick = onClearSelection) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "退出选择"
                )
            }
        }
    )
} 