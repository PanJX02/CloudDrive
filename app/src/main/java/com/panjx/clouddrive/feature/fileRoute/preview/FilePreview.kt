package com.panjx.clouddrive.feature.fileRoute.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.panjx.clouddrive.core.design.theme.MyAppTheme
import com.panjx.clouddrive.feature.fileRoute.component.FileScreen
import com.panjx.clouddrive.feature.fileRoute.viewmodel.FileUiState

/**
 * 文件界面预览
 */
@Preview(showBackground = true)
@Composable
fun FileScreenPreview() {
    MyAppTheme {
        // 提供预览数据
        FileScreen(
            uiState = FileUiState.Success(listOf()), // Example state
            viewModel = viewModel(), // Use a preview ViewModel or mock
            transfersViewModel = hiltViewModel(),
            currentPath = listOf(0L to "根目录"),
            selectedFiles = listOf(),
            onSelectChange = { _, _ -> },
            onNavigateToDirectory = { _, _ -> },
            clearSelection = {},
            extraBottomSpace = 90.dp
        )
    }
} 