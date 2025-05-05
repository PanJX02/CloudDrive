package com.panjx.clouddrive.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panjx.clouddrive.core.config.Config
import com.panjx.clouddrive.data.UserPreferences
import com.panjx.clouddrive.data.repository.TransferRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val transferRepository: TransferRepository
) : ViewModel() {

    private val _currentEndpoint = MutableStateFlow(UserPreferences.DEFAULT_ENDPOINT)
    val currentEndpoint: StateFlow<String> = _currentEndpoint.asStateFlow()

    private val _showEndpointDialog = MutableStateFlow(false)
    val showEndpointDialog: StateFlow<Boolean> = _showEndpointDialog.asStateFlow()

    private val _showRestartDialog = MutableStateFlow(false)
    val showRestartDialog: StateFlow<Boolean> = _showRestartDialog.asStateFlow()

    private val _selectedEndpoint = MutableStateFlow("")
    val selectedEndpoint: StateFlow<String> = _selectedEndpoint.asStateFlow()

    init {
        viewModelScope.launch {
            _currentEndpoint.value = userPreferences.endpoint.first()
        }
    }

    fun setShowEndpointDialog(show: Boolean) {
        _showEndpointDialog.value = show
    }

    fun setShowRestartDialog(show: Boolean) {
        _showRestartDialog.value = show
    }

    fun setSelectedEndpoint(endpoint: String) {
        _selectedEndpoint.value = endpoint
    }

    fun changeEndpoint(endpoint: String) {
        viewModelScope.launch {
            if (endpoint != _currentEndpoint.value) {
                userPreferences.setEndpoint(endpoint)
                Config.updateEndpoint(endpoint)
                _currentEndpoint.value = endpoint
                _selectedEndpoint.value = endpoint
                _showEndpointDialog.value = false
                _showRestartDialog.value = true
            } else {
                _showEndpointDialog.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            // 1. 清空所有传输记录
            transferRepository.deleteAllTransfers()
            
            // 2. 清除登录状态
            userPreferences.clearLoginState()
        }
    }

    fun clearCache() {
        // 实现清理缓存逻辑
        viewModelScope.launch {
            // TODO: 添加清理缓存的实现
        }
    }
} 