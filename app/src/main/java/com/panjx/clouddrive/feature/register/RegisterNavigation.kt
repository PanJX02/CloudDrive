package com.panjx.clouddrive.feature.register

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val REGISTER_ROUTE = "register"

fun NavGraphBuilder.registerScreen(
    toMain: () -> Unit,
    toLogin: () -> Unit
) {
    composable(REGISTER_ROUTE) {
        RegisterRoute(
            toMain = toMain,
            toLogin = toLogin
        )
    }
} 