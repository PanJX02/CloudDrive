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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

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

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun login() {
        viewModelScope.launch {
            try {
                if (_email.value.isBlank() || _password.value.isBlank()) {
                    _loginState.value = LoginState.Error("邮箱和密码不能为空")
                    return@launch
                }
                
                if (!isValidEmail(_email.value)) {
                    _loginState.value = LoginState.Error("请输入有效的邮箱地址")
                    return@launch
                }

                val user = User(_email.value, _password.value)
                val response = networkApiService.login(user)
                if (response.code == 1) {
                    response.data?.let { loginData ->
                        userPreferences.setLoggedIn(true, _email.value, loginData.token)
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
    
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = """^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$""".toRegex()
        return email.matches(emailRegex)
    }
}

sealed class LoginState {
    object Initial : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
} 