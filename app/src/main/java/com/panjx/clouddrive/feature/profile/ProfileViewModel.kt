package com.panjx.clouddrive.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panjx.clouddrive.core.modle.response.UserInfoResponse
import com.panjx.clouddrive.core.network.datasource.MyRetrofitDatasource
import com.panjx.clouddrive.data.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserState(
    val isLoading: Boolean = false,
    val isInitializing: Boolean = true,
    val userInfo: UserInfoResponse? = null,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val datasource: MyRetrofitDatasource,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _userState = MutableStateFlow(UserState())
    val userState: StateFlow<UserState> = _userState.asStateFlow()

    init {
        // 优先加载缓存数据
        loadCachedUserInfo()
        // 然后从服务器加载最新数据
        loadUserInfo()
    }
    
    // 加载本地缓存的用户信息
    private fun loadCachedUserInfo() {
        viewModelScope.launch {
            try {
                val cachedInfo = userPreferences.cachedUserInfo.first()
                if (cachedInfo != null) {
                    _userState.value = _userState.value.copy(
                        userInfo = cachedInfo,
                        isInitializing = false
                    )
                }
            } catch (e: Exception) {
                // 加载缓存失败时继续执行，不更新状态
            }
        }
    }

    fun loadUserInfo() {
        viewModelScope.launch {
            try {
                // 只有在没有缓存数据时才显示加载指示器
                if (_userState.value.userInfo == null) {
                    _userState.value = _userState.value.copy(isLoading = true)
                }
                
                val response = datasource.getUserInfo()
                if (response.code == 1 && response.data != null) {
                    // 更新内存中的状态
                    _userState.value = _userState.value.copy(
                        isLoading = false,
                        isInitializing = false,
                        userInfo = response.data,
                        error = null
                    )
                    
                    // 保存到本地存储
                    userPreferences.saveUserInfo(response.data)
                } else {
                    _userState.value = _userState.value.copy(
                        isLoading = false,
                        isInitializing = false,
                        error = response.message
                    )
                }
            } catch (e: Exception) {
                _userState.value = _userState.value.copy(
                    isLoading = false, 
                    isInitializing = false,
                    error = e.message
                )
            }
        }
    }
} 