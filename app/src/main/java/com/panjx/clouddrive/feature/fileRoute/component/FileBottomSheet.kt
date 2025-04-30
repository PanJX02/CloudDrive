package com.panjx.clouddrive.feature.fileRoute.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.panjx.clouddrive.feature.transfersRoute.TransfersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBottomSheet(
    transfersViewModel: TransfersViewModel,
    currentDirId: Long,
    onDismiss: () -> Unit,
    onNewFolderClick: () -> Unit,
    onShareContentClick: () -> Unit
) {
    val context = LocalContext.current
    
    // 创建文件选择器
    val filePicker = FilePicker(
        transfersViewModel = transfersViewModel,
        currentDirId = currentDirId,
        context = context
    )
    
    // 创建启动器
    val launchFilePicker = filePicker.createLauncher(onFilePicked = { onDismiss() })

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
                text = "文件操作",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            ListItem(
                headlineContent = { Text("上传文件") },
                leadingContent = { Icon(Icons.Default.Upload, "上传文件") },
                modifier = Modifier.clickable { 
                    launchFilePicker()
                }
            )
            ListItem(
                headlineContent = { Text("新建文件夹") },
                leadingContent = { Icon(Icons.Default.CreateNewFolder, "新建文件夹") },
                modifier = Modifier.clickable { 
                    onDismiss()
                    onNewFolderClick()
                }
            )
            ListItem(
                headlineContent = { Text("获取分享内容") },
                leadingContent = { Icon(Icons.Default.Link, "获取分享内容") },
                modifier = Modifier.clickable {
                    onDismiss()
                    onShareContentClick()
                }
            )
        }
    }
} 