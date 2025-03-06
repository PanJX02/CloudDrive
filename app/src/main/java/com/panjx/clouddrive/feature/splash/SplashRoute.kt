package com.panjx.clouddrive.feature.splash

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.panjx.clouddrive.R
import com.panjx.clouddrive.ui.theme.CloudDriveTheme
import com.panjx.clouddrive.util.SuperDateUtil

@Composable
fun SplashRoute(
    modifier: Modifier? = Modifier,
    toMain: () -> Unit
) {
    SplashScreen(SuperDateUtil.currentYear(),
        modifier,
        toMain
    )
}

@Composable
fun SplashScreen(
    year: Int,
    modifier: Modifier? = Modifier,
    toMain: () -> Unit = {}
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
                    toMain()
                }
        )

    }
}

@Preview(showBackground = true)
@Composable
fun SplashRoutePreview() {
    CloudDriveTheme {
        SplashScreen(2025)
    }
}