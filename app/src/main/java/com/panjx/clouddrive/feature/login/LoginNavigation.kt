package com.panjx.clouddrive.feature.login

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val LOGIN_ROUTE = "login"

fun NavGraphBuilder.loginScreen(
    toMain: () -> Unit,
    toRegister: () -> Unit
) {
    composable(LOGIN_ROUTE) {
        LoginRoute(
            toMain = toMain,
            toRegister = toRegister
        )
    }
} 