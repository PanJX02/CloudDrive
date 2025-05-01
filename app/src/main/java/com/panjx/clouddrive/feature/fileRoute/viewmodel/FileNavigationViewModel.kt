package com.panjx.clouddrive.feature.fileRoute.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.panjx.clouddrive.core.network.datasource.MyRetrofitDatasource
import com.panjx.clouddrive.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 专门处理文件导航相关功能的ViewModel
 */
class FileNavigationViewModel(application: Application) : AndroidViewModel(application) {
    private val userPreferences = UserPreferences(application)
    private val networkDataSource = MyRetrofitDatasource(userPreferences)
    
    // 当前目录ID，0表示根目录
    private val _currentDirId = MutableStateFlow(0L)
    val currentDirId: StateFlow<Long> = _currentDirId
    
    // 当前路径，用于展示面包屑导航
    private val _currentPath = MutableStateFlow<List<Pair<Long, String>>>(listOf(Pair(0L, "根目录")))
    val currentPath: StateFlow<List<Pair<Long, String>>> = _currentPath

    // 加载特定目录的内容，并更新导航路径
    fun navigateToDirectory(dirId: Long, dirName: String? = null) {
        Log.d("FileNavigationViewModel", "navigateToDirectory: 导航到目录 $dirId")
        
        // 立即更新导航路径
        if (dirId == 0L) {
            // 重置到根目录
            _currentPath.value = listOf(Pair(0L, "根目录"))
        } else if (dirName != null) {
            // 如果提供了目录名，添加到路径中
            val newPath = _currentPath.value.toMutableList()
            // 检查是否已经存在该目录，防止重复添加
            if (newPath.none { it.first == dirId }) {
                newPath.add(Pair(dirId, dirName))
                _currentPath.value = newPath
            }
        } else {
            // 处理点击面包屑导航已有路径的情况
            // 查找点击的路径项在当前路径中的位置
            val existingPathIndex = _currentPath.value.indexOfFirst { it.first == dirId }
            if (existingPathIndex != -1) {
                // 如果找到了，截断路径至该位置（保留该项）
                _currentPath.value = _currentPath.value.subList(0, existingPathIndex + 1)
            }
        }
        
        // 更新当前目录ID
        _currentDirId.value = dirId
    }
    
    // 返回上一级目录
    fun navigateUp(): Boolean {
        val currentPath = _currentPath.value
        if (currentPath.size > 1) {
            // 移除当前路径的最后一个元素，返回上一级
            val newPath = currentPath.dropLast(1)
            _currentPath.value = newPath
            // 更新当前目录ID为上一级目录的ID
            val parentDir = newPath.last()
            _currentDirId.value = parentDir.first
            return true
        }
        return false // 没有上一级目录了
    }
    
    // 获取当前目录的父目录ID
    fun getCurrentParentDirId(): Long {
        val currentPath = _currentPath.value
        return if (currentPath.size > 1) {
            currentPath[currentPath.size - 2].first
        } else {
            0L // 已经在根目录
        }
    }
} 