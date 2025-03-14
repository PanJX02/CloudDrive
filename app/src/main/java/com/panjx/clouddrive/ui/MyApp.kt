package com.panjx.clouddrive.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.panjx.clouddrive.data.UserPreferences
import com.panjx.clouddrive.feature.login.LOGIN_ROUTE
import com.panjx.clouddrive.feature.login.loginScreen
import com.panjx.clouddrive.feature.main.MAIN_ROUTE
import com.panjx.clouddrive.feature.main.mainScreen
import com.panjx.clouddrive.feature.main.navigateToMain
import com.panjx.clouddrive.feature.register.REGISTER_ROUTE
import com.panjx.clouddrive.feature.register.registerScreen
import com.panjx.clouddrive.feature.splash.SPLASH_ROUTE
import com.panjx.clouddrive.feature.splash.splashScreen

@Composable
fun MyApp(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }

    NavHost(navController, startDestination = SPLASH_ROUTE) {
        splashScreen(
            toMain = navController::navigateToMain,
            toLogin = { 
                navController.navigate(LOGIN_ROUTE) {
                    popUpTo(SPLASH_ROUTE) { inclusive = true }
                }
            }
        )
        loginScreen(
            toMain = navController::navigateToMain,
            toRegister = {
                navController.navigate(REGISTER_ROUTE) {
                    popUpTo(LOGIN_ROUTE) { inclusive = true }
                }
            }
        )
        registerScreen(
            toMain = navController::navigateToMain,
            toLogin = {
                navController.navigate(LOGIN_ROUTE) {
                    popUpTo(REGISTER_ROUTE) { inclusive = true }
                }
            }
        )
        mainScreen(
            finishPage = navController::popBackStack,
            userPreferences = userPreferences,
            onNavigateToLogin = { 
                navController.navigate(LOGIN_ROUTE) {
                    popUpTo(MAIN_ROUTE) { inclusive = true }
                }
            }
        )
    }
}