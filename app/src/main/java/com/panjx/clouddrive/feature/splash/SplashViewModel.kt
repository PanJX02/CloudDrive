package com.panjx.clouddrive.feature.splash

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.panjx.clouddrive.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/*
* 启动页面ViewModel
*
*/
class SplashViewModel(application: Application) : AndroidViewModel(application) {
    private val userPreferences = UserPreferences(application)

    /**
     * 倒计时秒数
     */
    private val _timeLeft = MutableStateFlow(0L)
    val timeLeft: StateFlow<Long> = _timeLeft

    private val _navigateToMain = MutableStateFlow(false)
    val navigateToMain: StateFlow<Boolean> = _navigateToMain

    private val _navigateToLogin = MutableStateFlow(false)
    val navigateToLogin: StateFlow<Boolean> = _navigateToLogin

    private var timer: CountDownTimer? = null

    init {
        delayToNext(1000)
        checkLoginState()
    }

    private fun checkLoginState() {
        viewModelScope.launch {
            userPreferences.isLoggedIn.collect { isLoggedIn ->
                if (timer == null) {
                    if (isLoggedIn) {
                        _navigateToMain.value = true
                    } else {
                        _navigateToLogin.value = true
                    }
                }
            }
        }
    }

    private fun delayToNext(time: Long = 3000) {
        timer = object : CountDownTimer(time, 1000) {
            override fun onTick(p0: Long) {
                _timeLeft.value = p0 / 1000 + 1
            }

            override fun onFinish() {
                timer = null
                checkLoginState()
            }
        }.start()
    }

    fun onSkipClick() {
        timer?.cancel()
        timer = null
        checkLoginState()
    }
}