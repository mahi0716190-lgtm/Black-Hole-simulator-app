package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.SimulatorViewModel
import com.example.ui.theme.*

@Composable
fun SimulationScreen(
    viewModel: SimulatorViewModel,
    modifier: Modifier = Modifier
) {
    val activePlanets by viewModel.activePlanets.collectAsState()
    val massGained by viewModel.blackHoleMassExaTonnes.collectAsState()
    val consumedCount by viewModel.planetsConsumedCount.collectAsState()
    val logs by viewModel.hudLogs.collectAsState()
    val templateSelected by viewModel.selectedPlanetTemplate.collectAsState()
    val slowMo by viewModel.isSlowMotion.collectAsState()
    val bhSize by viewModel.blackHoleSize.collectAsState()
    val gStrength by viewModel.gravityStrength.collectAsState()

    var showQuickTray by remember { mutableStateOf(false) }

    SleekBackground(
        modifier = modifier.testTag("simulation_screen_root")
    ) {
        // BACKDROP CANVAS
        SimulationCanvas(
            viewModel = viewModel,
            isCinematicZoomActive = false,
            modifier = Modifier.fillMaxSize()
        )

        // TOP ROW: TELEMETRY AND SHORTCUTS COMBO
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            // General Command buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Exit button
                IconButton(
                    onClick = { viewModel.setScreen(SimulatorViewModel.Screen.Home) },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.72f), RoundedCornerShape(8.dp))
                        .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .testTag("top_nav_home")
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = CyberCyan
                    )
                }

                // Title Telemetry gauge description
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.72f), RoundedCornerShape(8.dp))
                        .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(if (activePlanets.isNotEmpty()) LaserPink else BioGreen, RoundedCornerShape(100.dp))
                        )
                        Text(
                            text = if (activePlanets.isNotEmpty()) "ACTIVE ORBITS: ${activePlanets.size}" else "SINGULARITY DORMANT",
                            color = EventHorizonWhite,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Settings shortcut
                IconButton(
                    onClick = { viewModel.setScreen(SimulatorViewModel.Screen.Settings) },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.72f), RoundedCornerShape(8.dp))
                        .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .testTag("top_nav_settings")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configure",
                        tint = EventHorizonWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // READOUT QUANTUM GAUGES (Sleek glass layout alignment)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Gauge A: Singularity mass
                Card(
                    modifier = Modifier
                        .weight(1.3f)
                        .border(1.dp, GlassBorder, RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = GlassBg)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "SINGULARITY CORE MASS",
                            fontSize = 9.sp,
                            color = CyberCyan.copy(alpha = 0.8f),
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${"%,.1f".format(massGained)} EXA-T",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = EventHorizonWhite,
                            modifier = Modifier.testTag("core_mass_counter")
                        )
                    }
                }

                // Gauge B: Consumed counters
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, GlassBorder, RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = GlassBg)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "PLANETS DIGESTED",
                            fontSize = 9.sp,
                            color = LaserPink.copy(alpha = 0.8f),
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "$consumedCount WORLDS",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = LaserPink,
                            modifier = Modifier.testTag("consumed_planets_counter")
                        )
                    }
                }
            }
        }

        // TOUCH GESTURE INSTRUCTION BOX (Only shown if no planets are flying)
        if (activePlanets.isEmpty() && viewModel.dragSling.collectAsState().value == null) {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-60).dp)
                    .width(280.dp)
                    .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .alpha(0.85f),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.9f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Gesture,
                        contentDescription = "Slingshot Gesture",
                        tint = CyberCyan,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "TOUCH & PULL TO SLING",
                        color = EventHorizonWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Press your finger, drag backwards to set velocity trajectory, and release to sling ${templateSelected.name} into space orbit!",
                        color = EventHorizonWhite.copy(alpha = 0.60f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp
                    )

                    HorizontalDivider(color = CyberCyan.copy(alpha = 0.15f))

                    Text(
                        text = "OR TAP 'LAUNCH BALANCED ORBIT' BELOW",
                        color = LaserPink,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // BOTTOM SYSTEM CONTROL CONSOLE
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Ticker Scroll terminal
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .background(GlassBg, RoundedCornerShape(8.dp))
                    .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "DIAGNOSTIC STATUS LOGS //",
                        fontSize = 9.sp,
                        color = BioGreen,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (logs.isNotEmpty()) logs[0] else "SENSORS SYNCED // NO ERRORS REVEALED",
                        color = EventHorizonWhite.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scrolling_log_msg")
                    )
                }
            }

            // Quick Active Planet Selector Slinger tray
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Label of Active launcher object
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .background(GlassBg, RoundedCornerShape(8.dp))
                        .border(1.dp, templateSelected.atmosphereColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .clickable { showQuickTray = !showQuickTray }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("quick_planet_tray_selector")
                ) {
                    Text(
                        text = "LAUNCH APPARATUS",
                        fontSize = 8.sp,
                        color = templateSelected.atmosphereColor,
                        fontFamily = FontFamily.Monospace
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = templateSelected.name.uppercase(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = EventHorizonWhite
                        )
                        Icon(
                            imageVector = if (showQuickTray) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = "Expand",
                            tint = CyberCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Action A: Balanced orbit launch direct
                Button(
                    onClick = { viewModel.spawnOrbitingPlanet() },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(44.dp)
                        .testTag("launch_balanced_orbit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = templateSelected.atmosphereColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "Orbit",
                        tint = SpaceBlack,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LAUNCH ORBIT",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = SpaceBlack,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Action B: SlowMo time dilation switch
                IconButton(
                    onClick = { viewModel.toggleSlowMotion() },
                    modifier = Modifier
                        .size(44.dp)
                        .background(if (slowMo) LaserPink else Color.Black.copy(alpha = 0.82f), RoundedCornerShape(8.dp))
                        .border(1.dp, if (slowMo) LaserPink else CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .testTag("slowmo_sh_toggle")
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Slow Motion Toggle",
                        tint = if (slowMo) SpaceBlack else EventHorizonWhite
                    )
                }
            }

            // Expanded Quick planetary selection panel
            AnimatedVisibility(
                visible = showQuickTray,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .border(1.dp, CyberCyan.copy(alpha = 0.25f), RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.95f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(CelestialCatalogue.bodies) { body ->
                            val isSelected = body.id == templateSelected.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) body.atmosphereColor.copy(alpha = 0.25f) else Color.Transparent)
                                    .border(
                                        1.dp,
                                        if (isSelected) body.atmosphereColor else EventHorizonWhite.copy(alpha = 0.15f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable {
                                        viewModel.selectPlanetTemplate(body)
                                        showQuickTray = false
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("tray_opt_${body.id}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(body.color, RoundedCornerShape(100.dp))
                                    )
                                    Text(
                                        text = body.name,
                                        color = EventHorizonWhite,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
