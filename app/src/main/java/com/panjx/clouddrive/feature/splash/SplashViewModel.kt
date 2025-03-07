package com.panjx.clouddrive.feature.splash

import android.os.CountDownTimer
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/*
* 启动页面ViewModel
*
*/
class SplashViewModel : ViewModel() {
    /**
     * 倒计时秒数
     */
    private val _timeLeft = MutableStateFlow(0L)
    val timeLeft:StateFlow<Long> = _timeLeft
    private var timer:CountDownTimer?=null
    /*
    * 倒计时结束跳转到主界面
    * */
    val navigateToMain = MutableStateFlow(false)
    init {
        delayToNext(1000)
    }

    private fun delayToNext(time:Long=3000) {
        object : CountDownTimer(time, 1000) {
            override fun onTick(p0: Long) {
                _timeLeft.value=p0/1000+1
            }

            override fun onFinish() {
                toNext()
            }

        }.start()
    }

    private fun toNext() {
        navigateToMain.value = true
    }

    fun onSkipClick() {
        timer?.cancel()
        toNext()
    }
}