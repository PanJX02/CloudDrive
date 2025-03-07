package com.panjx.clouddrive.feature.main

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.panjx.clouddrive.R
import com.panjx.clouddrive.feature.fileRoute.FileRoute
import com.panjx.clouddrive.feature.meRoute.MeRoute
import com.panjx.clouddrive.feature.transfersRoute.TransfersRoute
import kotlinx.coroutines.launch

@Composable
fun MainRote(
    finishPage: () -> Unit
) {
    MainScreen(finishPage)
}

@Composable
fun MainScreen(
    finishPage: () -> Unit = {}
) {
    val pagerState = rememberPagerState(pageCount = { 3 }) // 修正为3个页面
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        bottomBar = {
            NavigationBar {
                // 定义导航项
                listOf("文件", "传输", "我的").forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },

                        icon = {
                            Icon(
                                imageVector = when (index) {
                                    0 -> Icons.Filled.Folder    // 文件图标
                                    1 -> Icons.Filled.SwapVert   // 传输图标
                                    else -> Icons.Filled.Person  // 我的图标
                                },
                                contentDescription = title
                            )
                        },
                        label = { Text(title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            userScrollEnabled = false,
            beyondViewportPageCount = 1
        ) { page ->
            when (page) {
                0 -> FileRoute()
                1 -> TransfersRoute() // 注意顺序调整（原代码中1是MeRoute）
                2 -> MeRoute()
            }
        }
    }

}
