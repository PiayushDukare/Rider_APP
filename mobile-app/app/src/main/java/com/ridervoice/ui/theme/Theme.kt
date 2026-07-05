package com.ridervoice.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val TacticalDarkColorScheme = darkColorScheme(
    primary = NeonOrange,
    secondary = ElectricCyan,
    tertiary = NeonViolet, // Upgraded to NeonViolet for gradients
    background = GraphiteBase,
    surface = DarkSlate,
    onPrimary = GraphiteBase,
    onSecondary = GraphiteBase,
    onTertiary = GraphiteBase,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = AlertRed,
    onError = TextPrimary
)

@Composable
fun RiderVoiceTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = GraphiteBase.toArgb()
            window.navigationBarColor = GraphiteBase.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = TacticalDarkColorScheme,
        typography = Typography,
        content = content
    )
}
