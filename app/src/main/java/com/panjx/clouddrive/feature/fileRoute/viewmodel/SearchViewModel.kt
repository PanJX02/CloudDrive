package com.panjx.clouddrive.feature.fileRoute.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panjx.clouddrive.core.modle.File
import com.panjx.clouddrive.core.network.datasource.MyRetrofitDatasource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val navigationPath: List<Pair<Long, String>> = listOf(Pair(0L, "搜索结果")),
    val currentFiles: List<File> = emptyList(),
    val initialSearchResults: List<File> = emptyList(), // 保存初始搜索结果
    val selectedFiles: List<Long> = emptyList(), // 添加选中文件列表
    val isSelectionMode: Boolean = false // 添加选择模式状态
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val dataSource: MyRetrofitDatasource // 注入数据源
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    
    // 添加下拉刷新状态
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val currentDirId: Long
        get() = uiState.value.navigationPath.lastOrNull()?.first ?: 0L
    
    // 添加刷新当前文件夹的方法
    fun refreshCurrentFolder() {
        val dirId = currentDirId
        
        if (dirId == 0L && uiState.value.searchQuery.isNotBlank()) {
            // 如果在搜索结果页，则重新执行搜索
            _isRefreshing.value = true
            refreshSearch()
        } else if (dirId != 0L) {
            // 如果在文件夹内，则刷新文件夹内容
            _isRefreshing.value = true
            refreshFolderContent(dirId)
        } else {
            // 如果在初始页且没有搜索关键词，则不做任何操作
            _isRefreshing.value = false
        }
    }
    
    // 刷新搜索结果
    private fun refreshSearch() {
        val query = uiState.value.searchQuery
        if (query.isBlank()) {
            _isRefreshing.value = false
            return
        }

        viewModelScope.launch {
            try {
                val response = dataSource.searchFiles(query)
                _isRefreshing.value = false
                
                if (response.code == 1 && response.data != null) {
                    val files = response.data.list ?: emptyList()
                    _uiState.update {
                        it.copy(
                            currentFiles = files,
                            initialSearchResults = files
                        )
                    }
                } else {
                    Log.e("SearchViewModel", "Search refresh failed: ${response.message}")
                }
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error during search refresh", e)
                _isRefreshing.value = false
            }
        }
    }
    
    // 刷新文件夹内容
    private fun refreshFolderContent(dirId: Long) {
        viewModelScope.launch {
            try {
                val response = dataSource.getFilesByFolderId(dirId.toString())
                _isRefreshing.value = false
                
                if (response.code == 1 && response.data != null) {
                    _uiState.update { it.copy(currentFiles = response.data.list ?: emptyList()) }
                } else {
                    Log.e("SearchViewModel", "Failed to refresh folder $dirId: ${response.message}")
                }
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error refreshing folder $dirId", e)
                _isRefreshing.value = false
            }
        }
    }
        
    // 选择文件
    fun selectFile(fileId: Long, isSelected: Boolean) {
        val currentSelectedFiles = _uiState.value.selectedFiles.toMutableList()
        
        if (isSelected && !currentSelectedFiles.contains(fileId)) {
            currentSelectedFiles.add(fileId)
        } else if (!isSelected) {
            currentSelectedFiles.remove(fileId)
        }
        
        _uiState.update { 
            it.copy(
                selectedFiles = currentSelectedFiles,
                isSelectionMode = currentSelectedFiles.isNotEmpty()
            ) 
        }
    }
    
    // 清空选择
    fun clearSelection() {
        _uiState.update { it.copy(selectedFiles = emptyList(), isSelectionMode = false) }
    }
    
    // 退出选择模式
    fun exitSelectionMode() {
        clearSelection()
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun clearSearch() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                currentFiles = emptyList(),
                initialSearchResults = emptyList(),
                navigationPath = listOf(Pair(0L, "搜索结果")), // 重置路径
                isSearching = false,
                selectedFiles = emptyList(), // 清空选中文件
                isSelectionMode = false // 退出选择模式
            )
        }
    }

    fun performSearch() {
        val query = uiState.value.searchQuery
        if (query.isBlank()) return

        // 执行搜索时清空选择
        _uiState.update { 
            it.copy(
                isSearching = true, 
                navigationPath = listOf(Pair(0L, "搜索结果")),
                selectedFiles = emptyList(),
                isSelectionMode = false
            ) 
        }

        viewModelScope.launch {
            try {
                val response = dataSource.searchFiles(query) // 直接调用 dataSource
                if (response.code == 1 && response.data != null) {
                    val files = response.data.list ?: emptyList()
                    _uiState.update {
                        it.copy(
                            currentFiles = files,
                            initialSearchResults = files, // 保存初始结果
                            isSearching = false
                        )
                    }
                } else {
                    Log.e("SearchViewModel", "Search failed: ${response.message}")
                    _uiState.update { it.copy(currentFiles = emptyList(), initialSearchResults = emptyList(), isSearching = false) } // Handle error
                }
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error during search", e)
                _uiState.update { it.copy(currentFiles = emptyList(), initialSearchResults = emptyList(), isSearching = false) } // Handle error
            }
        }
    }

    fun navigateToFolder(file: File) {
        if (file.folderType != 1) return
        val id = file.id ?: 0L
        val name = file.fileName ?: "Unknown Folder"
        if (id == 0L) return

        // 导航时清空选择
        val currentPath = uiState.value.navigationPath
        _uiState.update {
            it.copy(
                isSearching = true,
                navigationPath = currentPath + Pair(id, name),
                currentFiles = emptyList(), // 清空当前文件，等待加载
                selectedFiles = emptyList(),
                isSelectionMode = false
            )
        }

        loadFolderContent(id)
    }

    fun navigateUp() {
        val currentPath = uiState.value.navigationPath
        if (currentPath.size <= 1) return

        // 导航时清空选择
        val newPath = currentPath.dropLast(1)
        val prevDirId = newPath.last().first

        _uiState.update { 
            it.copy(
                isSearching = true, 
                navigationPath = newPath,
                selectedFiles = emptyList(),
                isSelectionMode = false
            ) 
        }

        if (prevDirId == 0L) {
            // 返回到初始搜索结果
            _uiState.update { it.copy(currentFiles = it.initialSearchResults, isSearching = false) }
        } else {
            loadFolderContent(prevDirId)
        }
    }

    fun navigateToPathIndex(index: Int) {
        val currentPath = uiState.value.navigationPath
        if (index < 0 || index >= currentPath.size || index == currentPath.size - 1) return

        // 导航时清空选择
        val newPath = currentPath.subList(0, index + 1)
        val targetDirId = newPath.last().first

        _uiState.update { 
            it.copy(
                isSearching = true, 
                navigationPath = newPath,
                selectedFiles = emptyList(),
                isSelectionMode = false
            ) 
        }

        if (targetDirId == 0L) {
            // 返回到初始搜索结果
            _uiState.update { it.copy(currentFiles = it.initialSearchResults, isSearching = false) }
        } else {
            loadFolderContent(targetDirId)
        }
    }

    private fun loadFolderContent(dirId: Long) {
        _uiState.update { it.copy(isSearching = true) } // Set searching true before launch
        viewModelScope.launch {
            try {
                // 直接调用 dataSource 获取文件夹内容
                val response = dataSource.getFilesByFolderId(dirId.toString())
                if (response.code == 1 && response.data != null) {
                    _uiState.update { it.copy(currentFiles = response.data.list ?: emptyList(), isSearching = false) }
                } else {
                    Log.e("SearchViewModel", "Failed to load folder $dirId: ${response.message}")
                    _uiState.update { it.copy(currentFiles = emptyList(), isSearching = false) } // Handle error state appropriately
                }
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error loading folder $dirId", e)
                _uiState.update { it.copy(currentFiles = emptyList(), isSearching = false) } // Handle error state appropriately
            }
        }
    }
} 