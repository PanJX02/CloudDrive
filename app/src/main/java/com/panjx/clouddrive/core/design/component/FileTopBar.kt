package com.panjx.clouddrive.core.design.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileTopBar(
    toSearch: () -> Unit,
    showBackIcon: Boolean,
    onNavigateUp: () -> Unit = {}
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
                    IconButton(onClick = { onNavigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回上一级"
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
                    contentDescription = "搜索",
                    modifier = Modifier
                        .padding(start = 8.dp, end = 16.dp)
                        .clickable {
                            toSearch()
                        }
                )
            }
        }
    )
}