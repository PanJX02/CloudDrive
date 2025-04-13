package com.panjx.clouddrive.feature.file.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * 面包屑导航组件
 * 
 * @param currentPath 当前路径列表，格式为 List<Pair<Long, String>>，每个Pair包含目录ID和目录名称
 * @param onNavigate 导航回调，当点击某个路径项时触发，参数为目录ID
 */
@Composable
fun BreadcrumbNavigator(
    currentPath: List<Pair<Long, String>>,
    onNavigate: (dirId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        val scrollState = rememberScrollState()
        
        // 当组件加载或路径变化时，自动滚动到最右侧
        LaunchedEffect(currentPath) {
            // 延迟一下再滚动，确保布局已完成
            delay(100)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "位置: ",
                style = MaterialTheme.typography.bodySmall,
            )
            currentPath.forEachIndexed { index, pathItem ->
                Text(
                    text = pathItem.second,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (index == currentPath.size - 1) 
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable(enabled = index != currentPath.size - 1) {
                        onNavigate(pathItem.first)
                    }
                )
                if (index < currentPath.size - 1) {
                    Text(
                        text = " > ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // 添加右侧空白，确保最后一项可以滑动到最左边
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        // 左侧阴影 - 仅当滚动位置不在最左侧时显示
        if (scrollState.value > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(24.dp)
                    .height(16.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
        
        // 右侧阴影 - 仅当滚动位置不在最右侧时显示
        if (scrollState.value < scrollState.maxValue) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(24.dp)
                    .height(16.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                            )
                        )
                    )
            )
        }
    }
} 