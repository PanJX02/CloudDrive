package com.panjx.clouddrive.feature.splash

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val SPLASH_ROUTE = "splash"

fun NavGraphBuilder.splashScreen(
    toMain: () -> Unit
) {
    composable(SPLASH_ROUTE) {
        SplashRoute(null,toMain)
    }
}