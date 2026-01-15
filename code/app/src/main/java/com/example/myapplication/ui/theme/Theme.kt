package com.example.myapplication.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = BrownDark,
    secondary = GreenDark,
    tertiary = YellowStrong,

    background = Greenish,
    surface = Cream,

    onPrimary = Cream,
    onSecondary = Cream,
    onBackground = BrownDark,
    onSurface = BrownDark
)

private val LightColorScheme = lightColorScheme(
    primary = BrownDark,
    secondary = GreenDark,
    tertiary = YellowStrong,

    background = Greenish,
    surface = Cream,

    onPrimary = Cream,
    onSecondary = Cream,
    onBackground = BrownDark,
    onSurface = BrownDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}