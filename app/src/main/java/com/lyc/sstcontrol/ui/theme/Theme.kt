package com.lyc.sstcontrol.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Mint,
    onPrimary = OnMint,
    primaryContainer = MintSoft,
    onPrimaryContainer = OnMint,
    secondary = Indigo,
    onSecondary = Color.White,
    secondaryContainer = Mint,
    onSecondaryContainer = OnMint,
    tertiary = InspectorPurple,
    onTertiary = Color.White,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = Color.White,
    onSurface = TextPrimary,
    surfaceVariant = CardGray,
    onSurfaceVariant = TextSecondary,
    outline = OutlineGray,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun SSTControlTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content
    )
}

@Composable
fun StatusBarIcons(darkIcons: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkIcons
        }
    }
}