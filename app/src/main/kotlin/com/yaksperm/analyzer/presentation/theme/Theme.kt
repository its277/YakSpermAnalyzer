package com.yaksperm.analyzer.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val YakDarkColorScheme = darkColorScheme(
    primary          = CyanPrimary,
    onPrimary        = NavyBg,
    primaryContainer = CyanDim,
    onPrimaryContainer = CyanLight,
    secondary        = GreenMotile,
    onSecondary      = NavyBg,
    secondaryContainer = GreenDim,
    onSecondaryContainer = GreenMotile,
    tertiary         = AmberCount,
    onTertiary       = NavyBg,
    tertiaryContainer = AmberDim,
    onTertiaryContainer = AmberCount,
    background       = NavyBg,
    onBackground     = TextPrimary,
    surface          = CardBg,
    onSurface        = TextPrimary,
    surfaceVariant   = CardAlt,
    onSurfaceVariant = TextSecond,
    outline          = Border,
    outlineVariant   = BorderLight,
    error            = RedAlert,
    onError          = NavyBg,
    errorContainer   = RedDim,
    onErrorContainer = RedAlert,
    inverseSurface   = TextPrimary,
    inverseOnSurface = NavyBg,
    inversePrimary   = CyanDim,
    scrim            = NavyBg
)

@Composable
fun YakSpermTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = YakDarkColorScheme,
        typography = YakSpermTypography,
        content = content
    )
}
