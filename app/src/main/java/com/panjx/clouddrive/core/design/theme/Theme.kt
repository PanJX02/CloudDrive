package com.panjx.clouddrive.core.design.theme

import android.os.Build
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun MyAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme: ColorScheme
    val themeType: String

    when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            colorScheme = if (darkTheme) {
                dynamicDarkColorScheme(context).also {
                    themeType = "Dynamic Dark"
                }
            } else {
                dynamicLightColorScheme(context).also {
                    themeType = "Dynamic Light"
                }
            }
        }
        darkTheme -> {
            colorScheme = DarkColorScheme
            themeType = "Static Dark"
        }
        else -> {
            colorScheme = LightColorScheme
            themeType = "Static Light"
        }
    }

    // 输出主题类型到 Log
    Log.d("ThemeLogger", "当前主题类型: $themeType")

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}