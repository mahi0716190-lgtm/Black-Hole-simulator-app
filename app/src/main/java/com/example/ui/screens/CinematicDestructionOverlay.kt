package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SimulatorViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun CinematicDestructionOverlay(
    viewModel: SimulatorViewModel,
    modifier: Modifier = Modifier
) {
    val consumedCount by viewModel.planetsConsumedCount.collectAsState()
    val massGained by viewModel.blackHoleMassExaTonnes.collectAsState()
    val lastAlert = viewModel.hudLogs.value.firstOrNull { it.contains("CRITICAL") || it.contains("LAUNCH") }
        ?: "COSMIC SINGULARITY COLLAPSE IN PROGRESS"

    // Siren alarm pulsing logic
    val infiniteTransition = rememberInfiniteTransition(label = "siren")
    val sirenAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sirenAlpha"
    )

    val scaleOffset by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInBack),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Automatically transition back to simulation after 3.2 seconds of explosion peak
    LaunchedEffect(Unit) {
        delay(3200)
        viewModel.setScreen(SimulatorViewModel.Screen.Simulation)
    }

    SleekBackground(
        modifier = modifier.testTag("cinematic_destruction_block")
    ) {
        // Red Pulsing Siren Alarm Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LaserPink.copy(alpha = sirenAlpha))
        )

        // Close-up zoomed canvas background simulation showing high particles
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.40f)
        ) {
            SimulationCanvas(viewModel = viewModel, isCinematicZoomActive = true)
        }

        // Top Warning Bar HUD
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(24.dp)
                .background(Color.Black.copy(alpha = 0.82f), RoundedCornerShape(8.dp))
                .border(2.dp, LaserPink, RoundedCornerShape(8.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = LaserPink,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "SINGULARITY CONSUMPTION EVENT !",
                    color = EventHorizonWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                color = LaserPink,
                trackColor = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
            )
        }

        // Center spaghettification clinical alerts
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // HUD Warning box
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                    .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(24.dp)
                    .widthIn(max = 420.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "TITANIC SHEAR COMPLETED",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = "EVENT DETECTED",
                        color = EventHorizonWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = lastAlert.replace("SYSTEM: ", ""),
                        color = EventHorizonWhite.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.testTag("cinematic_alert_content")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "ACCUMULATED RECORD", color = EventHorizonWhite.copy(alpha = 0.5f), fontSize = 9.sp)
                            Text(text = "$consumedCount CONSUMED", color = LaserPink, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "CURRENT MASS", color = EventHorizonWhite.copy(alpha = 0.5f), fontSize = 9.sp)
                            Text(text = "${"%,.0f".format(massGained)} exa-t", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            Text(
                text = "SYSTEM AUTO-STABILIZING... STANDBY...",
                color = BioGreen,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        // Bottom action button to bypass countdown
        Button(
            onClick = { viewModel.setScreen(SimulatorViewModel.Screen.Simulation) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(24.dp)
                .fillMaxWidth()
                .height(48.dp)
                .testTag("dismiss_cinematic_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, EventHorizonWhite.copy(alpha = 0.3f))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Bypass",
                tint = EventHorizonWhite
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "RETURN REGULAR WORKSPACE",
                color = EventHorizonWhite,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
