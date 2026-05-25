package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SimulatorViewModel
import com.example.ui.theme.*

@Composable
fun SettingsMenu(
    viewModel: SimulatorViewModel,
    modifier: Modifier = Modifier
) {
    val bhSize by viewModel.blackHoleSize.collectAsState()
    val gravityFactor by viewModel.gravityStrength.collectAsState()
    val slowMo by viewModel.isSlowMotion.collectAsState()
    val cinematicMode by viewModel.isCinematicMode.collectAsState()
    val soundEnabled by viewModel.isSoundEnabled.collectAsState()

    var showConfirmDialog by remember { mutableStateOf(false) }

    SleekBackground {
        Scaffold(
            modifier = modifier
                .fillMaxSize(),
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.setScreen(SimulatorViewModel.Screen.Home) },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = EventHorizonWhite
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "CONTROL CONSOLE",
                            color = LaserPink.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "PHYSICAL PARAMETERS",
                            color = EventHorizonWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Section 1: Singular Core Tuning Sliders
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberCyan.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = DeepSpace.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "SINGULARITY DYNAMICS //",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    // Black Hole Size Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Event Horizon Radius", color = EventHorizonWhite, fontSize = 13.sp)
                            Text(
                                text = "${bhSize.toInt()} Mega-Parsecs",
                                color = CyberCyan,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Slider(
                            value = bhSize,
                            onValueChange = { viewModel.updateBlackHoleSize(it) },
                            valueRange = 30f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = CyberCyan,
                                activeTrackColor = CyberCyan,
                                inactiveTrackColor = CyberCyan.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.testTag("black_hole_size_slider")
                        )
                        Text(
                            text = "Expands the absolute dark gravitational eclipse boundary, eating passing orbits earlier.",
                            color = EventHorizonWhite.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }

                    // Separation bar
                    HorizontalDivider(color = CyberCyan.copy(alpha = 0.12f))

                    // Gravity Pull Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Gravitational Core Constant (G)", color = EventHorizonWhite, fontSize = 13.sp)
                            Text(
                                text = "${String.format("%.1f", gravityFactor)} G-Force",
                                color = LaserPink,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Slider(
                            value = gravityFactor,
                            onValueChange = { viewModel.updateGravityStrength(it) },
                            valueRange = 0.5f..5.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = LaserPink,
                                activeTrackColor = LaserPink,
                                inactiveTrackColor = LaserPink.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.testTag("gravity_strength_slider")
                        )
                        Text(
                            text = "Amplifies orbital accelerations. Excessive gravity strips planetary structures apart into spiraling dust rings rapidly.",
                            color = EventHorizonWhite.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            // Section 2: Real-time Sensors Option Toggles
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeonBlue.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = DeepSpace.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "FEEDBACK COGNITION OPTIONS //",
                        color = NeonBlue,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    // Slow Motion Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Quantum Time Dilation", color = EventHorizonWhite, fontSize = 13.sp)
                            Text(
                                text = "Enables super slow-motion matrix tracking.",
                                color = EventHorizonWhite.copy(alpha = 0.5f),
                                fontSize = 10.sp
                            )
                        }
                        Switch(
                            checked = slowMo,
                            onCheckedChange = { viewModel.toggleSlowMotion() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyberCyan,
                                checkedTrackColor = CyberCyan.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.testTag("slow_motion_toggle")
                        )
                    }

                    HorizontalDivider(color = CyberCyan.copy(alpha = 0.12f))

                    // Cinematic Camera Focus Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Cinematic Horizon Tracking", color = EventHorizonWhite, fontSize = 13.sp)
                            Text(
                                text = "Zooms camera on planets experiencing tidal spaghettification.",
                                color = EventHorizonWhite.copy(alpha = 0.5f),
                                fontSize = 10.sp
                            )
                        }
                        Switch(
                            checked = cinematicMode,
                            onCheckedChange = { viewModel.toggleCinematicMode() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = LaserPink,
                                checkedTrackColor = LaserPink.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.testTag("cinematic_camera_toggle")
                        )
                    }

                    HorizontalDivider(color = CyberCyan.copy(alpha = 0.12f))

                    // Sound / Vibration Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Tactile Vibration Sensation", color = EventHorizonWhite, fontSize = 13.sp)
                            Text(
                                text = "Triggers tactical friction and impact pulses on events.",
                                color = EventHorizonWhite.copy(alpha = 0.5f),
                                fontSize = 10.sp
                            )
                        }
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = { viewModel.toggleSoundEnabled() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = BioGreen,
                                checkedTrackColor = BioGreen.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.testTag("sound_vibration_toggle")
                        )
                    }
                }
            }

            // Universal Reset Button
            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("reset_simulation_trigger"),
                colors = ButtonDefaults.buttonColors(containerColor = LaserPink),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = EventHorizonWhite
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "REBOOT SPACE GRID PROTOCOLS",
                    color = EventHorizonWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    } // Closes SleekBackground
}

    // Confirmation Alert
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    text = "REBOOT COGNITIVE FIELD?",
                    color = LaserPink,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will return active planetary orbits to sleep, reset singularity masses, delete space-dust debris trails, and recalibrate sensors back to default 25,000 exa-tonnes.",
                    color = EventHorizonWhite,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetSimulation()
                        showConfirmDialog = false
                    },
                    modifier = Modifier.testTag("confirm_reset_action")
                ) {
                    Text(text = "CONFIRM DETONATION", color = LaserPink, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmDialog = false }
                ) {
                    Text(text = "CANCEL", color = EventHorizonWhite.copy(alpha = 0.7f))
                }
            },
            containerColor = DeepSpace,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.border(1.dp, LaserPink, RoundedCornerShape(12.dp))
        )
    }
}
