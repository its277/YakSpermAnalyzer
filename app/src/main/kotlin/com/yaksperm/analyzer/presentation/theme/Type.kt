package com.yaksperm.analyzer.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Using system fonts that are always available on Android
// For production: bundle Inter or Outfit via downloadable fonts
val InterFamily = FontFamily.Default

val YakSpermTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp,
        color = TextPrimary
    ),
    displayMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        color = TextPrimary
    ),
    headlineLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        color = TextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = TextPrimary
    ),
    headlineSmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        color = TextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp,
        color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = TextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = TextSecond
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = TextSecond
    ),
    bodySmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = TextDim
    ),
    labelLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp,
        color = TextPrimary
    ),
    labelMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = TextDim
    ),
    labelSmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 9.sp,
        lineHeight = 12.sp,
        letterSpacing = 0.5.sp,
        color = TextDim
    )
)
