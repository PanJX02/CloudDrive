package com.panjx.clouddrive.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.panjx.clouddrive.feature.login.LOGIN_ROUTE
import com.panjx.clouddrive.feature.login.loginScreen
import com.panjx.clouddrive.feature.main.mainScreen
import com.panjx.clouddrive.feature.main.navigateToMain
import com.panjx.clouddrive.feature.splash.SPLASH_ROUTE
import com.panjx.clouddrive.feature.splash.splashScreen

@Composable
fun MyApp(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(navController, startDestination = SPLASH_ROUTE) {
        splashScreen(
            toMain = navController::navigateToMain,
            toLogin = { navController.navigate(LOGIN_ROUTE) }
        )
        loginScreen(
            toMain = navController::navigateToMain
        )
        mainScreen(
            finishPage = navController::popBackStack
        )
    }
}