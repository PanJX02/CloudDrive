package com.panjx.clouddrive.feature.fileRoute.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.panjx.clouddrive.core.design.theme.SpaceSmall

/**
 * 文件信息对话框
 * 显示选择的文件信息，包括文件名、大小、类型等
 */
@Composable
fun FileInfoDialog(
    fileName: String,
    fileSize: String,
    fileSizeBytes: String = "",
    fileType: String,
    fileExtension: String = "",
    uploadFolderId: String = "",
    uploadFolderName: String = "",
    md5Hash: String = "",
    md5Time: Long = 0,
    sha1Hash: String = "",
    sha1Time: Long = 0,
    sha256Hash: String = "",
    sha256Time: Long = 0,
    sha512Hash: String = "",
    sha512Time: Long = 0,
    keccak256Hash: String = "",
    keccak256Time: Long = 0,
    isCalculatingHashes: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "文件信息")
        },
        text = {
            val scrollState = rememberScrollState()
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = "文件名：$fileName",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(SpaceSmall))
                
                Text(
                    text = "文件大小：$fileSize",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                if (fileSizeBytes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(SpaceSmall))
                    
                    Text(
                        text = "字节大小：$fileSizeBytes 字节",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                Spacer(modifier = Modifier.height(SpaceSmall))
                
                Text(
                    text = "文件类型：$fileType",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                if (fileExtension.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(SpaceSmall))

                    Text(
                        text = "文件后缀：$fileExtension",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }else {
                    Spacer(modifier = Modifier.height(SpaceSmall))

                    Text(
                        text = "文件后缀：null",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                Spacer(modifier = Modifier.height(SpaceSmall))
                
                Text(
                    text = "上传位置：${if (uploadFolderName.isNotEmpty()) uploadFolderName else "根目录"}",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(SpaceSmall))
                
                Text(
                    text = "文件夹ID：$uploadFolderId",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                // 哈希值部分
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "文件哈希值",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                if (isCalculatingHashes) {
                    // 显示计算中状态
                    Spacer(modifier = Modifier.height(SpaceSmall))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        
                        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                        
                        Text(
                            text = "正在计算哈希值，请稍候...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    // 显示计算结果
                    if (md5Hash.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(SpaceSmall))
                        
                        Text(
                            text = "MD5 ${if(md5Time > 0) "(${md5Time}ms)" else ""}：",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = md5Hash,
                            style = MaterialTheme.typography.bodySmall,
                            overflow = TextOverflow.Visible
                        )
                    }
                    
                    if (sha1Hash.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(SpaceSmall))
                        
                        Text(
                            text = "SHA1 ${if(sha1Time > 0) "(${sha1Time}ms)" else ""}：",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = sha1Hash,
                            style = MaterialTheme.typography.bodySmall,
                            overflow = TextOverflow.Visible
                        )
                    }
                    
                    if (sha256Hash.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(SpaceSmall))
                        
                        Text(
                            text = "SHA256 ${if(sha256Time > 0) "(${sha256Time}ms)" else ""}：",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = sha256Hash,
                            style = MaterialTheme.typography.bodySmall,
                            overflow = TextOverflow.Visible
                        )
                    }
                    
                    // 添加SHA-512哈希值显示
                    if (sha512Hash.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(SpaceSmall))
                        
                        Text(
                            text = "SHA512 ${if(sha512Time > 0) "(${sha512Time}ms)" else ""}：",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = sha512Hash,
                            style = MaterialTheme.typography.bodySmall,
                            overflow = TextOverflow.Visible
                        )
                    }
                    
                    // 添加Keccak-256哈希值显示
                    if (keccak256Hash.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(SpaceSmall))
                        
                        Text(
                            text = "Keccak256 ${if(keccak256Time > 0) "(${keccak256Time}ms)" else ""}：",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = keccak256Hash,
                            style = MaterialTheme.typography.bodySmall,
                            overflow = TextOverflow.Visible
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm
            ) {
                Text("上传")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("取消")
            }
        }
    )
} 