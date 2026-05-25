package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme =
  darkColorScheme(
    primary = CyberCyan,
    secondary = NeonBlue,
    tertiary = LaserPink,
    background = SpaceBlack,
    surface = DeepSpace,
    onPrimary = SpaceBlack,
    onSecondary = EventHorizonWhite,
    onBackground = EventHorizonWhite,
    onSurface = CyberCyan
  )

private val LightColorScheme = DarkColorScheme // Default to DarkTheme for sci-fi immersion

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force cinematic dark theme
  dynamicColor: Boolean = false, // Force our custom high-impact neon palette
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

/**
 * Premium glassmorphic background with cinematic radial space dust gradients
 * and futuristic science corner boundaries overlayed over the viewport.
 */
@Composable
fun SleekBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceBlack)
    ) {
        // High fidelity radial dark space lights simulating deep-field nebulae
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Sleek Indigo atmospheric center-left nebula
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(QuantumPurple.copy(alpha = 0.16f), Color.Transparent),
                    center = Offset(width * 0.28f, height * 0.32f),
                    radius = width * 0.65f
                ),
                radius = width * 0.65f,
                center = Offset(width * 0.28f, height * 0.32f)
            )

            // 2. Cyber-cyan bottom-right thermal glow highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(CyberCyan.copy(alpha = 0.11f), Color.Transparent),
                    center = Offset(width * 0.72f, height * 0.68f),
                    radius = width * 0.72f
                ),
                radius = width * 0.70f,
                center = Offset(width * 0.72f, height * 0.68f)
            )
        }

        // Inner content layout block
        content()

        // Science telemetry outer boundary corner indicators
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            val dLength = 20.dp.toPx()
            val dWidth = 1.5.dp.toPx()
            val bracketColor = CyberCyan.copy(alpha = 0.25f)

            // Top Left Bracket
            drawLine(bracketColor, start = Offset(0f, 0f), end = Offset(dLength, 0f), strokeWidth = dWidth)
            drawLine(bracketColor, start = Offset(0f, 0f), end = Offset(0f, dLength), strokeWidth = dWidth)

            // Top Right Bracket
            drawLine(bracketColor, start = Offset(size.width, 0f), end = Offset(size.width - dLength, 0f), strokeWidth = dWidth)
            drawLine(bracketColor, start = Offset(size.width, 0f), end = Offset(size.width, dLength), strokeWidth = dWidth)

            // Bottom Left Bracket
            drawLine(bracketColor, start = Offset(0f, size.height), end = Offset(dLength, size.height), strokeWidth = dWidth)
            drawLine(bracketColor, start = Offset(0f, size.height), end = Offset(0f, size.height - dLength), strokeWidth = dWidth)

            // Bottom Right Bracket
            drawLine(bracketColor, start = Offset(size.width, size.height), end = Offset(size.width - dLength, size.height), strokeWidth = dWidth)
            drawLine(bracketColor, start = Offset(size.width, size.height), end = Offset(size.width, size.height - dLength), strokeWidth = dWidth)
        }
    }
}

