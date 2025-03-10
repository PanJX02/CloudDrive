package com.panjx.clouddrive.feature.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.panjx.clouddrive.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val userPreferences = UserPreferences(application)

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)
    val loginState: StateFlow<LoginState> = _loginState

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    fun onUsernameChange(newUsername: String) {
        _username.value = newUsername
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun login() {
        viewModelScope.launch {
            if (_username.value == "admin" && _password.value == "20020920") {
                userPreferences.setLoggedIn(true, _username.value)
                _loginState.value = LoginState.Success
            } else {
                _loginState.value = LoginState.Error("用户名或密码错误")
            }
        }
    }
}

sealed class LoginState {
    object Initial : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
} 