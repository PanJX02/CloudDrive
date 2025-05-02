package com.panjx.clouddrive.feature.file

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

// 路由常量
const val FILE_SELECTION_ROUTE = "file_selection"
const val FOLDER_SELECTION_ROUTE = "folder_selection"

// 参数键
private const val INITIAL_DIR_ID_ARG = "initialDirId"
private const val EXCLUDE_FOLDERS_ARG = "excludeFolders"
private const val ALLOW_MULTI_SELECT_ARG = "allowMultiSelect"
private const val OPERATION_TYPE_ARG = "operationType"
private const val SHARE_KEY_ARG = "shareKey"
private const val SHARE_CODE_ARG = "shareCode"

// 操作类型
enum class FileOperationType {
    COPY, MOVE, SAVE_SHARE
}

// 导航到文件选择页面
fun NavController.navigateToFileSelection(
    initialDirId: Long = 0L,
    allowMultiSelect: Boolean = true
) {
    navigate("$FILE_SELECTION_ROUTE/$initialDirId?allowMultiSelect=$allowMultiSelect")
}

// 导航到文件夹选择页面
fun NavController.navigateToFolderSelection(
    initialDirId: Long = 0L,
    excludeFolderIds: String = "",
    operationType: FileOperationType
) {
    navigate("$FOLDER_SELECTION_ROUTE/$initialDirId?excludeFolders=$excludeFolderIds&operationType=${operationType.name}")
}

// 导航到分享文件转存文件夹选择页面
fun NavController.navigateToShareSaveFolderSelection(
    shareKey: String,
    shareCode: String = ""
) {
    navigate("$FOLDER_SELECTION_ROUTE/0?shareKey=$shareKey&shareCode=$shareCode&operationType=${FileOperationType.SAVE_SHARE.name}")
}

// 注册文件选择页面路由
fun NavGraphBuilder.fileSelectionScreen(
    onBackClick: () -> Unit,
    onFilesSelected: (List<Long>) -> Unit
) {
    composable(
        route = "$FILE_SELECTION_ROUTE/{$INITIAL_DIR_ID_ARG}?$ALLOW_MULTI_SELECT_ARG={$ALLOW_MULTI_SELECT_ARG}",
        arguments = listOf(
            navArgument(INITIAL_DIR_ID_ARG) {
                type = NavType.LongType
                defaultValue = 0L
            },
            navArgument(ALLOW_MULTI_SELECT_ARG) {
                type = NavType.BoolType
                defaultValue = true
            }
        )
    ) { backStackEntry ->
        val initialDirId = backStackEntry.arguments?.getLong(INITIAL_DIR_ID_ARG) ?: 0L
        val allowMultiSelect = backStackEntry.arguments?.getBoolean(ALLOW_MULTI_SELECT_ARG) ?: true
        
        FileSelectionScreen(
            title = "选择文件",
            onBackClick = onBackClick,
            onFilesSelected = onFilesSelected,
            allowMultiSelect = allowMultiSelect,
            initialDirectoryId = initialDirId
        )
    }
}

// 注册文件夹选择页面路由
fun NavGraphBuilder.folderSelectionScreen(
    onBackClick: () -> Unit,
    onFolderSelected: (Long, FileOperationType) -> Unit,
    onShareSaveSelected: (Long, String, String) -> Unit
) {
    composable(
        route = "$FOLDER_SELECTION_ROUTE/{$INITIAL_DIR_ID_ARG}?$EXCLUDE_FOLDERS_ARG={$EXCLUDE_FOLDERS_ARG}&$OPERATION_TYPE_ARG={$OPERATION_TYPE_ARG}&$SHARE_KEY_ARG={$SHARE_KEY_ARG}&$SHARE_CODE_ARG={$SHARE_CODE_ARG}",
        arguments = listOf(
            navArgument(INITIAL_DIR_ID_ARG) {
                type = NavType.LongType
                defaultValue = 0L
            },
            navArgument(EXCLUDE_FOLDERS_ARG) {
                type = NavType.StringType
                defaultValue = ""
            },
            navArgument(OPERATION_TYPE_ARG) {
                type = NavType.StringType
                defaultValue = FileOperationType.COPY.name
            },
            navArgument(SHARE_KEY_ARG) {
                type = NavType.StringType
                defaultValue = ""
            },
            navArgument(SHARE_CODE_ARG) {
                type = NavType.StringType
                defaultValue = ""
            }
        )
    ) { backStackEntry ->
        val initialDirId = backStackEntry.arguments?.getLong(INITIAL_DIR_ID_ARG) ?: 0L
        val excludeFoldersString = backStackEntry.arguments?.getString(EXCLUDE_FOLDERS_ARG) ?: ""
        val operationTypeString = backStackEntry.arguments?.getString(OPERATION_TYPE_ARG) 
            ?: FileOperationType.COPY.name
        val shareKey = backStackEntry.arguments?.getString(SHARE_KEY_ARG) ?: ""
        val shareCode = backStackEntry.arguments?.getString(SHARE_CODE_ARG) ?: ""
        
        // 转换排除文件夹列表
        val excludeFolderIds = if (excludeFoldersString.isNotEmpty()) {
            excludeFoldersString.split(",").mapNotNull { it.toLongOrNull() }
        } else {
            emptyList()
        }
        
        // 转换操作类型
        val operationType = try {
            FileOperationType.valueOf(operationTypeString)
        } catch (e: IllegalArgumentException) {
            FileOperationType.COPY
        }
        
        // 设置标题
        val title = when (operationType) {
            FileOperationType.COPY -> "选择复制目标位置"
            FileOperationType.MOVE -> "选择移动目标位置"
            FileOperationType.SAVE_SHARE -> "选择分享文件转存文件夹"
        }
        
        FolderSelectionScreen(
            title = title,
            onBackClick = onBackClick,
            onFolderSelected = { folderId -> 
                // 根据操作类型调用不同的回调
                when (operationType) {
                    FileOperationType.SAVE_SHARE -> {
                        // 如果是转存分享文件，调用专用回调
                        if (shareKey.isNotEmpty()) {
                            onShareSaveSelected(folderId, shareKey, shareCode)
                        }
                    }
                    else -> {
                        // 其他操作类型
                        onFolderSelected(folderId, operationType)
                    }
                }
            },
            excludeFolderIds = excludeFolderIds,
            initialDirectoryId = initialDirId
        )
    }
} 