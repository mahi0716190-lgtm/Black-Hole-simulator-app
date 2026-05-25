package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SimulatorViewModel
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: SimulatorViewModel,
    modifier: Modifier = Modifier
) {
    // Elegant entry transitions
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val neonAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutQuint),
            repeatMode = RepeatMode.Reverse
        ),
        label = "neonAlpha"
    )

    // Sleek space design container
    SleekBackground(
        modifier = modifier.testTag("home_screen_block")
    ) {
        // Holographic Space grid backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.18f)
        ) {
            SimulationCanvas(viewModel = viewModel, isCinematicZoomActive = false)
        }

        // HUD Top Bar (Sleek sci-fi header arrangement)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "EVENT HORIZON",
                    color = CyberCyan.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.testTag("sensors_status_ticker")
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Sgr A*",
                        color = EventHorizonWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "4.3M ☉",
                        color = CyberCyan.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Blinking Video recording feed tag
            Row(
                modifier = Modifier
                    .border(BorderStroke(1.dp, GlassBorder), RoundedCornerShape(50.dp))
                    .background(GlassBg)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .alpha(neonAlpha)
                        .background(LaserPink, RoundedCornerShape(100.dp))
                )
                Text(
                    text = "REC 00:24:12",
                    color = EventHorizonWhite,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Main Center Content Block
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Futuristic Icon (Glowing Event Horizon ring)
            Card(
                modifier = Modifier
                    .size(96.dp)
                    .scale(pulseScale)
                    .border(2.dp, CyberCyan, RoundedCornerShape(50.dp)),
                colors = CardDefaults.cardColors(containerColor = SpaceBlack),
                shape = RoundedCornerShape(50.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        progress = { 0.82f },
                        modifier = Modifier
                            .size(72.dp)
                            .align(Alignment.Center),
                        color = LaserPink,
                        strokeWidth = 3.dp
                    )
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color.Black, RoundedCornerShape(100.dp))
                            .border(1.dp, EventHorizonWhite, RoundedCornerShape(100.dp))
                            .align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Application Headings
            Text(
                text = "EVENT HORIZON",
                color = neonAlphaColor(CyberCyan, neonAlpha),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )

            Text(
                text = "BLACK HOLE",
                color = EventHorizonWhite,
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("app_title_text")
            )

            Text(
                text = "SIMULATOR",
                color = LaserPink,
                fontSize = 32.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 4.sp,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Sling celestial bodies into the singularity. Monitor thermal friction, tidal spaghettification, and accretion mass expansion under real-time simulated gravity.",
                color = EventHorizonWhite.copy(alpha = 0.65f),
                fontSize = 13.sp,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Bottom Action buttons Block
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Action 1: Launcher Core (Simulator Screen)
            Button(
                onClick = { viewModel.setScreen(SimulatorViewModel.Screen.Simulation) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("enter_simulator_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Simulate",
                    tint = SpaceBlack
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "INITIALIZE COGNITIVE SIMULATOR",
                    color = SpaceBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Action 2 & 3: Selection Screen & Settings row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Planetary specifications
                OutlinedButton(
                    onClick = { viewModel.setScreen(SimulatorViewModel.Screen.Selection) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("browse_planets_button"),
                    border = BorderStroke(1.dp, GlassBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan, containerColor = GlassBg),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "Planets"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PLANET DATA",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Global configs
                OutlinedButton(
                    onClick = { viewModel.setScreen(SimulatorViewModel.Screen.Settings) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("settings_button"),
                    border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.35f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EventHorizonWhite, containerColor = GlassBg),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SETTINGS",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Small watermark footer
            Text(
                text = "RESEARCH SECTOR 12 // SECURE HOLOGRAPHIC CONNECTION",
                color = EventHorizonWhite.copy(alpha = 0.3f),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun neonAlphaColor(color: Color, alpha: Float): Color {
    return color.copy(alpha = alpha)
}
