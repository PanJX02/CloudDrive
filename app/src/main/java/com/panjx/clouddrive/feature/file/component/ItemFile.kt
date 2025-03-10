package com.panjx.clouddrive.feature.file.component

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
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
import com.panjx.clouddrive.core.ui.FilePreviewParameterData.FILE
import com.panjx.clouddrive.util.DateTimeUtils

@Composable
fun ItemFile(
    data: File,
    isSelected: Boolean, // 新增参数
    modifier: Modifier = Modifier,
    onSelectChange: (Boolean) -> Unit // 新增回调
) {
    Log.d("Composable", "ItemFile")
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 15.dp)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (data.type == "Folder"){
            Icon(
                imageVector = Icons.Filled.Folder,
                contentDescription = "Folder",
                modifier = Modifier
                    .size(35.dp)
            )
        }else{
            Icon(
                imageVector = Icons.Filled.FileCopy,
                contentDescription = "OtherFile",
                modifier = Modifier
                    .size(35.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f)
                .padding(horizontal = 15.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = data.name,
                style = MaterialTheme.typography.bodyLarge,
            )
            SpaceSmall()
            Text(
                text = DateTimeUtils.formatTimestamp(data.updateTime), // 调用工具类
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
        data = FILE,
        isSelected = false, // 预览时默认未选中
        onSelectChange = {}
    )

}