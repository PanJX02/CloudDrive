package com.panjx.clouddrive.feature.file.component

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.panjx.clouddrive.core.design.theme.SpaceSmall
import com.panjx.clouddrive.core.modle.File
import com.panjx.clouddrive.core.ui.FilePreviewParameterData.FILE2
import com.panjx.clouddrive.util.DateTimeUtils
import com.panjx.clouddrive.util.FileIconUtils
import com.panjx.clouddrive.util.FileSizeUtils

@Composable
fun ItemFile(
    data: File,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onSelectChange: (Boolean) -> Unit,
    onFolderClick: ((Long, String) -> Unit)? = null // 修改参数类型
) {
    Log.d("Composable", "ItemFile")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // 如果是文件夹且提供了点击回调，则触发回调
                if (data.folderType == 1 && onFolderClick != null) {
                    data.id?.let { data.fileName?.let { it1 -> onFolderClick(it, it1) } }
                }
            }
            .padding(horizontal = 15.dp)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 使用工具类获取文件图标
        data.folderType?.let { FileIconUtils.getFileIcon(it, data.fileCategory) }?.let {
            Icon(
                imageVector = it,
                contentDescription = if (data.folderType == 1) "Folder" else "File",
                modifier = Modifier.size(35.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f)
                .padding(horizontal = 15.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            (if (data.folderType != 1 && !data.fileExtension.isNullOrEmpty()) {
                "${data.fileName}.${data.fileExtension}"
            } else {
                data.fileName
            })?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            SpaceSmall()
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 显示收藏星号
                if (data.favoriteFlag == 1) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "已收藏",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                }
                
                Text(
                    text = data.lastUpdateTime?.let { DateTimeUtils.formatTimestamp(it) } ?: "未知时间",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // 如果不是文件夹，显示文件大小
                if (data.folderType != 1) {
                    Text(
                        text = " · ${data.fileSize?.let { FileSizeUtils.formatFileSize(it) } ?: "未知大小"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        // 动态切换选中图标
        val icon = if (isSelected) { // 使用参数状态
            Icons.Filled.RadioButtonChecked
        } else {
            Icons.Filled.RadioButtonUnchecked
        }

        Icon(
            imageVector = icon,
            contentDescription = "Select",
            tint = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier
                .size(15.dp)
                .clickable {
                    onSelectChange(!isSelected) // 直接传递新状态
                }
        )

    }
}
@Preview(showBackground = true)
@Composable
fun ItemFilePreview() {
    ItemFile(
        data = FILE2,
        isSelected = false, // 预览时默认未选中
        onSelectChange = {}
    )

}