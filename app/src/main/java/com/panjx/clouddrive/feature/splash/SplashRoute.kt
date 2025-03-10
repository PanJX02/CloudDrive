package com.panjx.clouddrive.feature.splash

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.panjx.clouddrive.R
import com.panjx.clouddrive.core.design.theme.MyAppTheme
import com.panjx.clouddrive.util.SuperDateUtil

@Composable
fun SplashRoute(
    modifier: Modifier? = Modifier,
    toMain: () -> Unit,
    toLogin: () -> Unit,
    viewModel: SplashViewModel = viewModel()
) {
    // 获取倒计时
    val timeLeft by viewModel.timeLeft.collectAsStateWithLifecycle()
    // 跳转主页面
    val navigateToMain by viewModel.navigateToMain.collectAsState()
    // 跳转登录页面
    val navigateToLogin by viewModel.navigateToLogin.collectAsState()

    SplashScreen(
        SuperDateUtil.currentYear(),
        modifier,
        timeLeft,
        viewModel::onSkipClick
    )

    LaunchedEffect(navigateToMain, navigateToLogin) {
        when {
            navigateToMain -> toMain()
            navigateToLogin -> toLogin()
        }
    }
}

@Composable
fun SplashScreen(
    year: Int,
    modifier: Modifier? = Modifier,
    timeLeft: Long=0,
    onSkipClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        //region Logo
        Image(painter = painterResource(id = R.drawable.splash_logo),
            contentDescription = null,
            modifier = Modifier
                .width(120.dp)
                .height(120.dp)
                .align(Alignment.TopCenter)
                .offset(y = 120.dp)
        )
        //endregion

        //名字
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-80).dp)
                .clickable {
                    Log.d("SplashScreen", "跳转主页面")
                    onSkipClick()
                }
        )

        Text(
            "倒计时：$timeLeft",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(y = 40.dp)
                .offset(x = (-40).dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashRoutePreview() {
    MyAppTheme {
        SplashScreen(2025)
    }
}