package com.panjx.clouddrive.feature.announcements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panjx.clouddrive.core.modle.response.Announcement
import com.panjx.clouddrive.core.network.datasource.MyRetrofitDatasource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnnouncementsViewModel @Inject constructor(
    private val datasource: MyRetrofitDatasource
) : ViewModel() {
    
    // UI状态
    data class AnnouncementsUiState(
        val announcements: List<Announcement> = emptyList(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )
    
    private val _uiState = MutableStateFlow(AnnouncementsUiState(isLoading = true))
    val uiState: StateFlow<AnnouncementsUiState> = _uiState.asStateFlow()
    
    init {
        loadAnnouncements()
    }
    
    fun loadAnnouncements() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val response = datasource.getAnnouncement()
                when (response.code) {
                    1 -> {
                        _uiState.value = _uiState.value.copy(
                            announcements = response.data ?: emptyList(),
                            isLoading = false
                        )
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = response.message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "获取公告失败"
                )
            }
        }
    }
} 