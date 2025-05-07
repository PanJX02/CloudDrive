package com.panjx.clouddrive.feature.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.panjx.clouddrive.core.network.datasource.MyRetrofitDatasource
import com.panjx.clouddrive.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(application: Application) : AndroidViewModel(application) {
    private val userPreferences = UserPreferences(application)
    private val dataSource = MyRetrofitDatasource(userPreferences)

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Initial)
    val registerState: StateFlow<RegisterState> = _registerState

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password
    
    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword

    private val _inviteCode = MutableStateFlow("")
    val inviteCode: StateFlow<String> = _inviteCode

    fun onUsernameChange(newUsername: String) {
        _username.value = newUsername
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }
    
    fun onConfirmPasswordChange(newConfirmPassword: String) {
        _confirmPassword.value = newConfirmPassword
    }

    fun onInviteCodeChange(newInviteCode: String) {
        _inviteCode.value = newInviteCode
    }

    fun register() {
        viewModelScope.launch {
            try {
                // 验证所有必填字段
                if (_username.value.isBlank() || _password.value.isBlank() || 
                    _confirmPassword.value.isBlank() || _inviteCode.value.isBlank()) {
                    _registerState.value = RegisterState.Error("所有字段都必须填写")
                    return@launch
                }
                
                // 验证用户名长度
                if (_username.value.length < 4) {
                    _registerState.value = RegisterState.Error("用户名长度不能少于4个字符")
                    return@launch
                }
                
                // 验证密码一致性
                if (_password.value != _confirmPassword.value) {
                    _registerState.value = RegisterState.Error("两次输入的密码不一致")
                    return@launch
                }
                
                // 验证密码长度
                if (_password.value.length < 6) {
                    _registerState.value = RegisterState.Error("密码长度不能少于6个字符")
                    return@launch
                }

                // 调用API注册用户
                val response = dataSource.register(_username.value, _password.value, _inviteCode.value)
                if (response.code == 1) {
                    response.data?.let { loginData ->
                        userPreferences.setLoggedIn(
                            isLoggedIn = true, 
                            username = _username.value, 
                            accessToken = loginData.accessToken,
                            refreshToken = loginData.refreshToken
                        )
                        _registerState.value = RegisterState.Success
                    } ?: run {
                        _registerState.value = RegisterState.Error("注册数据为空")
                    }
                } else {
                    _registerState.value = RegisterState.Error(response.message ?: "注册失败")
                }
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error("网络错误：${e.message}")
            }
        }
    }
}

sealed class RegisterState {
    object Initial : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
} 