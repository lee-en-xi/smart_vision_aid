package com.example.smart_vision_aid.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color



@Composable
fun SmartVisionAidTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = GreenPrimary,
            secondary = GreenSecondary,
            background = GreenBackground,
            onPrimary = TextOnPrimary,
            onSecondary = TextOnPrimary,
            onBackground = TextPrimary,
            error = ErrorRed,
            onError = TextOnPrimary
        ),
        typography = Typography,
        content = content
    )
}