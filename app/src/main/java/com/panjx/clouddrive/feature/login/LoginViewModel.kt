package com.panjx.clouddrive.feature.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.panjx.clouddrive.core.config.Config
import com.panjx.clouddrive.core.modle.request.User
import com.panjx.clouddrive.core.network.retrofit.MyNetworkApiService
import com.panjx.clouddrive.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import okhttp3.MediaType.Companion.toMediaType

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val userPreferences = UserPreferences(application)
    private val networkApiService: MyNetworkApiService

    init {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val contentType = "application/json".toMediaType()
        val json = Json { 
            ignoreUnknownKeys = true
            prettyPrint = true
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(Config.ENDPOINT)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
        networkApiService = retrofit.create(MyNetworkApiService::class.java)
    }

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
            try {
                if (_username.value.isBlank() || _password.value.isBlank()) {
                    _loginState.value = LoginState.Error("用户名和密码不能为空")
                    return@launch
                }

                val user = User(_username.value, _password.value)
                val response = networkApiService.login(user)
                if (response.code == 200) {
                    response.data?.let { loginData ->
                        userPreferences.setLoggedIn(true, _username.value, loginData.token)
                        _loginState.value = LoginState.Success
                    } ?: run {
                        _loginState.value = LoginState.Error("登录数据为空")
                    }
                } else {
                    _loginState.value = LoginState.Error(response.message ?: "登录失败")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("网络错误：${e.message}")
            }
        }
    }
}

sealed class LoginState {
    object Initial : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
} 