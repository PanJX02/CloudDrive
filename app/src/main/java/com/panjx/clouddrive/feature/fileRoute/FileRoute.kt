package com.panjx.clouddrive.feature.fileRoute

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.panjx.clouddrive.core.design.theme.MyAppTheme

@Composable
fun FileRoute() {
    FileScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileScreen() {
    var searchQuery by remember { mutableStateOf("") }  // 搜索关键词
    var showSearchBar by remember { mutableStateOf(false) } // 控制搜索栏显示
    var showMenu by remember { mutableStateOf(false) } // 控制更多菜单显示
    Scaffold(
        topBar = {
            FileTopBar(
                toSearch = {  },
                showBackIcon = showSearchBar
            )
        },

    ) { innerPadding ->
        // 文件列表内容（示例）
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(/* 模拟文件数据 */ listOf("文档1.txt", "图片.jpg", "视频.mp4")) { fileName ->
                Text(
                    text = fileName,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileTopBar(
    toSearch: () -> Unit,
    showBackIcon: Boolean
) {
    var showMenu by remember { mutableStateOf(false) } // 控制更多菜单显示
    CenterAlignedTopAppBar(
        modifier = Modifier
            .height(90.dp), // 设置高度（默认 Material3 大标题栏为 152dp，此处调整为更紧凑的高度）

        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            // 使用 surface 颜色，与 BottomBar 默认颜色相同但通过高度区分
            // 或者直接指定一个高对比色，例如 MaterialTheme.colorScheme.primaryContainer
            containerColor = MaterialTheme.colorScheme.surfaceContainer, // 关键修改
        ),

        // 返回按钮
        navigationIcon = {
            Box(
                modifier = Modifier
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ){
                if (showBackIcon) {
                    IconButton(onClick = { /* 返回逻辑 */ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            }
        },
        // 搜索框
        title = {
            Row(
                modifier = Modifier
                    .fillMaxSize(),
                        // 关键参数设置
                horizontalArrangement = Arrangement.End,    // 水平靠右
                verticalAlignment = Alignment.CenterVertically // 垂直居中
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clickable {
                            toSearch()
                        }
                )
            }
        },
        // 更多功能菜单
        actions = {
            // 使用 Box 容器，填充整个 actions 区域，并设置内容居中
            Box(
                modifier = Modifier
                    .fillMaxHeight(), // 占满父容器全部空间
                contentAlignment = Alignment.Center // 内容居中
            ) {
                IconButton(
                    onClick = { showMenu = true },
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "更多"
                    )
                }
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("新建文件夹") },
                    onClick = { /* 处理新建文件夹 */ }
                )
                DropdownMenuItem(
                    text = { Text("排序") },
                    onClick = { /* 处理排序 */ }
                )
                DropdownMenuItem(
                    text = { Text("设置") },
                    onClick = { /* 跳转到设置页 */ }
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun FileRoutePreview() {
    MyAppTheme {
        FileScreen()
    }
}
