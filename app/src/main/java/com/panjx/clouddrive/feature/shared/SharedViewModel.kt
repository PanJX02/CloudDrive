package com.panjx.clouddrive.feature.shared

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panjx.clouddrive.core.modle.response.ShareListResponse
import com.panjx.clouddrive.core.network.datasource.MyRetrofitDatasource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class SharedUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val shareItems: List<SharedItem> = emptyList()
)

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val datasource: MyRetrofitDatasource,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SharedUiState())
    val uiState: StateFlow<SharedUiState> = _uiState.asStateFlow()

    init {
        loadShareList()
    }

    fun loadShareList() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val response = datasource.getShareList()
                if (response.code == 1) {
                    val items = mapResponseToSharedItems(response.data ?: emptyList())
                    _uiState.update { it.copy(shareItems = items, isLoading = false) }
                } else {
                    _uiState.update { it.copy(error = response.message, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
    
    private fun mapResponseToSharedItems(responses: List<ShareListResponse>): List<SharedItem> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        return responses.map { response ->
            SharedItem(
                name = response.shareName ?: "未命名分享",
                type = if (response.shareName?.contains("文件夹") == true) "文件夹" else "文件",
                shareTime = dateFormat.format(Date(response.shareTime)),
                views = response.showCount,
                downloads = 0, // 示例数据中没有下载数，暂时设为0
                shareId = response.shareId,
                shareKey = response.shareKey,
                code = response.code,
                shareKeyWithCode = response.shareKeyWithCode,
                validType = response.validType
            )
        }
    }
    
    fun deleteShare(shareId: Long) {
        // 根据shareId找到对应的分享项
        val shareItem = _uiState.value.shareItems.find { it.shareId == shareId }
        if (shareItem == null) {
            Log.e("SharedViewModel", "未找到ID为 $shareId 的分享项")
            return
        }
        
        // 获取shareKeyWithCode
        val shareKeyWithCode = shareItem.shareKeyWithCode
        if (shareKeyWithCode == null) {
            Log.e("SharedViewModel", "分享项 ${shareItem.name}(ID: $shareId) 的shareKeyWithCode为空")
            return
        }
        
        Log.d("SharedViewModel", "准备取消分享: ${shareItem.name}(ID: $shareId)")
        Log.d("SharedViewModel", "使用shareKeyWithCode: $shareKeyWithCode")
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 调用取消分享的API，使用shareKeyWithCode
                val response = datasource.cancelShare(shareKeyWithCode)
                
                if (response.code == 1) {
                    Log.d("SharedViewModel", "取消分享成功: ${shareItem.name}(ID: $shareId)")
                    // 取消成功后，更新列表
                    val updatedItems = _uiState.value.shareItems.filter { it.shareId != shareId }
                    _uiState.update { it.copy(shareItems = updatedItems, isLoading = false) }
                    showToast("分享已取消")
                } else {
                    Log.e("SharedViewModel", "取消分享失败: ${response.message}")
                    _uiState.update { it.copy(error = response.message, isLoading = false) }
                    showToast("取消分享失败: ${response.message}")
                }
            } catch (e: Exception) {
                Log.e("SharedViewModel", "取消分享异常:", e)
                _uiState.update { it.copy(error = e.message, isLoading = false) }
                showToast("取消分享失败: ${e.message}")
            }
        }
    }
    
    fun copyShareLink(text: String) {
        try {
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("分享链接", text)
            clipboardManager.setPrimaryClip(clipData)
            showToast("链接已复制到剪贴板")
        } catch (e: Exception) {
            showToast("复制链接失败: ${e.message}")
        }
    }
    
    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
} 