package com.panjx.clouddrive.feature.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

data class SharedItem(
    val name: String,
    val type: String,
    val shareTime: String,
    val views: Int,
    val downloads: Int,
    val shareId: Long = 0,
    val shareKey: String = "",
    val code: String = "",
    val shareKeyWithCode: String? = null,
    val validType: Int = 3 // 默认为永久
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedRoute(
    onNavigateBack: () -> Unit,
    viewModel: SharedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // 删除确认对话框状态
    var showDeleteDialog by remember { mutableStateOf(false) }
    var shareToDelete by remember { mutableLongStateOf(0L) }
    
    // 分享链接对话框状态
    var showLinkDialog by remember { mutableStateOf(false) }
    var selectedShare by remember { mutableStateOf<SharedItem?>(null) }
    
    // 底部菜单状态
    var showBottomSheet by remember { mutableStateOf(false) }
    var bottomSheetItem by remember { mutableStateOf<SharedItem?>(null) }
    
    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认取消分享") },
            text = { Text("您确定要取消这个分享吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteShare(shareToDelete)
                        showDeleteDialog = false
                    }
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 分享链接对话框
    selectedShare?.let { shareItem ->
        if (showLinkDialog) {
            ShareLinkDialog(
                shareItem = shareItem,
                onDismiss = { showLinkDialog = false },
                onCopy = { text ->
                    viewModel.copyShareLink(text)
                    showLinkDialog = false
                }
            )
        }
    }
    
    // 底部操作菜单
    bottomSheetItem?.let { item ->
        if (showBottomSheet) {
            ShareBottomSheet(
                shareItem = item,
                onDismiss = { showBottomSheet = false },
                onViewShareKeyClick = { 
                    selectedShare = it
                    showLinkDialog = true
                },
                onCancelShareClick = { 
                    shareToDelete = it
                    showDeleteDialog = true
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的分享") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("加载失败: ${uiState.error}")
                        Button(onClick = { viewModel.loadShareList() }) {
                            Text("重试")
                        }
                    }
                }
            }
            uiState.shareItems.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无分享内容")
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    items(uiState.shareItems) { item ->
                        ShareListItem(
                            shareItem = item,
                            onClick = {
                                // 点击分享项（可以用来处理默认打开详情等操作）
                            },
                            onMoreClick = {
                                // 显示底部菜单
                                bottomSheetItem = it
                                showBottomSheet = true
                            }
                        )
                    }
                }
            }
        }
    }
} 