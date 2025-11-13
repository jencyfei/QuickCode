package com.sms.tagger.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Soft Glassmorphism 柔和玻璃拟态风格 - 深色模式
private val DarkColorScheme = darkColorScheme(
    primary = AccentActive,
    secondary = GradientPurple,
    tertiary = GradientPink,
    background = Color(0xFF1A1A1A),
    surface = GlassFill,
    onPrimary = Color.White,
    onSecondary = TextPrimary,
    onTertiary = TextPrimary,
    onBackground = Color(0xFFE6E1E5),
    onSurface = TextPrimary,
    surfaceVariant = GlassBorder,
)

// Soft Glassmorphism 柔和玻璃拟态风格 - 浅色模式
private val LightColorScheme = lightColorScheme(
    primary = AccentActive,
    secondary = GradientPurple,
    tertiary = GradientPink,
    background = BackgroundMain,
    surface = GlassFill,
    onPrimary = Color.White,
    onSecondary = TextPrimary,
    onTertiary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = GlassBorder,
)

@Composable
fun SmsAgentTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // 使用背景色作为状态栏颜色,营造沉浸式体验
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
