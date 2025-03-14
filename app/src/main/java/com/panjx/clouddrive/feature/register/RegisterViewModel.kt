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

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password
    
    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword
    
    private val _nickname = MutableStateFlow("")
    val nickname: StateFlow<String> = _nickname
    
    private val _verifyCode = MutableStateFlow("")
    val verifyCode: StateFlow<String> = _verifyCode
    
    private val _isEmailVerified = MutableStateFlow(false)
    val isEmailVerified: StateFlow<Boolean> = _isEmailVerified

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }
    
    fun onConfirmPasswordChange(newConfirmPassword: String) {
        _confirmPassword.value = newConfirmPassword
    }
    
    fun onNicknameChange(newNickname: String) {
        _nickname.value = newNickname
    }
    
    fun onVerifyCodeChange(newVerifyCode: String) {
        _verifyCode.value = newVerifyCode
    }
    
    // 发送邮箱验证码
    fun sendVerifyCode() {
        viewModelScope.launch {
            try {
                if (_email.value.isBlank()) {
                    _registerState.value = RegisterState.Error("请输入邮箱地址")
                    return@launch
                }
                
                if (!isValidEmail(_email.value)) {
                    _registerState.value = RegisterState.Error("请输入有效的邮箱地址")
                    return@launch
                }
                
                // 调用后端API发送验证码
                val response = dataSource.sendEmailVerifyCode(_email.value)
                if (response.code == 1) {
                    _registerState.value = RegisterState.VerifyCodeSent
                } else {
                    _registerState.value = RegisterState.Error(response.message ?: "发送验证码失败")
                }
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error("发送验证码失败：${e.message}")
            }
        }
    }

    fun register() {
        viewModelScope.launch {
            try {
                // 验证所有必填字段
                if (_email.value.isBlank() || _password.value.isBlank() || 
                    _confirmPassword.value.isBlank() || _nickname.value.isBlank() || _verifyCode.value.isBlank()) {
                    _registerState.value = RegisterState.Error("所有字段都必须填写")
                    return@launch
                }
                
                // 验证邮箱格式
                if (!isValidEmail(_email.value)) {
                    _registerState.value = RegisterState.Error("请输入有效的邮箱地址")
                    return@launch
                }
                
                // 验证码是否已发送
                if (registerState.value !is RegisterState.VerifyCodeSent && registerState.value !is RegisterState.EmailVerified) {
                    _registerState.value = RegisterState.Error("请先获取验证码")
                    return@launch
                }
                
                // 验证验证码格式
                if (_verifyCode.value.length < 4) {
                    _registerState.value = RegisterState.Error("验证码格式不正确")
                    return@launch
                }
                
                // 验证密码一致性
                if (_password.value != _confirmPassword.value) {
                    _registerState.value = RegisterState.Error("两次输入的密码不一致")
                    return@launch
                }
                
                // 验证昵称长度
                if (_nickname.value.length < 2) {
                    _registerState.value = RegisterState.Error("昵称长度不能少于2个字符")
                    return@launch
                }
                
                // 验证密码长度
                if (_password.value.length < 6) {
                    _registerState.value = RegisterState.Error("密码长度不能少于6个字符")
                    return@launch
                }

                // 调用API注册用户，同时传递验证码
                val response = dataSource.register(_email.value, _password.value, _nickname.value, _verifyCode.value)
                if (response.code == 1) {
                    response.data?.let { registerData ->
                        userPreferences.setLoggedIn(true, _email.value, registerData.token)
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
    
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = """^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$""".toRegex()
        return email.matches(emailRegex)
    }
}

sealed class RegisterState {
    object Initial : RegisterState()
    object Success : RegisterState()
    object VerifyCodeSent : RegisterState()
    object EmailVerified : RegisterState()
    data class Error(val message: String) : RegisterState()
} 