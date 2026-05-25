package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.SimulatorViewModel
import com.example.ui.theme.*

@Composable
fun PlanetSelectScreen(
    viewModel: SimulatorViewModel,
    modifier: Modifier = Modifier
) {
    val bodies = CelestialCatalogue.bodies
    val selectedTemplate by viewModel.selectedPlanetTemplate.collectAsState()
    val lazyListState = rememberLazyListState()

    // Map selected template index for keeping it visible on launch
    LaunchedEffect(selectedTemplate) {
        val index = bodies.indexOfFirst { it.id == selectedTemplate.id }
        if (index >= 0) {
            lazyListState.animateScrollToItem(index)
        }
    }

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
                        modifier = Modifier.testTag("back_to_home_button")
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
                            text = "PLANETARY DIRECTORY",
                            color = CyberCyan.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "SELECT ORBITAL TARGET",
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
                    .padding(bottom = 24.dp)
            ) {
                // Horizontal Carousel of Planet Options
                Text(
                    text = "AVAILABLE TARGETS: // ${bodies.size} DETECTED",
                    color = EventHorizonWhite.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                )

                LazyRow(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .testTag("planet_carousel_row"),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(bodies) { body ->
                        val isSelected = body.id == selectedTemplate.id
                        Card(
                            modifier = Modifier
                                .width(135.dp)
                                .fillMaxHeight()
                                .clickable { viewModel.selectPlanetTemplate(body) }
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) body.atmosphereColor else GlassBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .testTag("planet_card_${body.id}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) body.atmosphereColor.copy(alpha = 0.16f) else GlassBg
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        ) {
                            // Mini sphere indicator
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(body.color, RoundedCornerShape(50.dp))
                                    .border(1.5.dp, body.atmosphereColor, RoundedCornerShape(50.dp))
                                    .align(Alignment.TopEnd)
                            )

                            Column(
                                modifier = Modifier.align(Alignment.BottomStart)
                            ) {
                                if (body.isSciFi) {
                                    Text(
                                        text = "SCI-FI //",
                                        color = LaserPink,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Text(
                                    text = body.name,
                                    color = if (isSelected) EventHorizonWhite else EventHorizonWhite.copy(alpha = 0.8f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = body.type,
                                    color = body.atmosphereColor.copy(alpha = 0.8f),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Spec-sheet of Currently Selected Target
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .border(1.dp, CyberCyan.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .testTag("planet_info_card"),
                colors = CardDefaults.cardColors(containerColor = DeepSpace.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header with larger sphere and categorization badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(
                                    Brush.radialGradient(
                                        listOf(selectedTemplate.color, selectedTemplate.atmosphereColor)
                                    ),
                                    RoundedCornerShape(50.dp)
                                )
                                .border(2.dp, EventHorizonWhite, RoundedCornerShape(50.dp))
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = selectedTemplate.name.uppercase(),
                                color = EventHorizonWhite,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(selectedTemplate.atmosphereColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = selectedTemplate.type.uppercase(),
                                        color = selectedTemplate.atmosphereColor,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (selectedTemplate.isSciFi) {
                                    Box(
                                        modifier = Modifier
                                            .background(LaserPink.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "SCI-FI EXOTIC",
                                            color = LaserPink,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Description text block
                    Column {
                        Text(
                            text = "COSMIC FILE EXTENSION //",
                            color = selectedTemplate.atmosphereColor.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedTemplate.description,
                            color = EventHorizonWhite.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }

                    HorizontalDivider(color = CyberCyan.copy(alpha = 0.15f))

                    // STATS SLIDERS (Mass index, Combustion, Destruction potential)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "PLANETARY METRICS AND CONSTANTS //",
                            color = CyberCyan.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        // Stat 1: Mass Index
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Public,
                                        contentDescription = "Mass",
                                        tint = CyberCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Relative Mass", color = EventHorizonWhite, fontSize = 12.sp)
                                }
                                Text(
                                    text = "${selectedTemplate.mass} Earth-masses",
                                    color = CyberCyan,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (selectedTemplate.mass / 12f).coerceIn(0.05f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(4.dp),
                                color = CyberCyan,
                                trackColor = Color.Black.copy(alpha = 0.5f)
                            )
                        }

                        // Stat 2: Atmospheric Stress Risk (Burn combustion potential)
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = "Friction",
                                        tint = LaserPink,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Atmospheric Burn Tendency", color = EventHorizonWhite, fontSize = 12.sp)
                                }
                                val densityFactor = if (selectedTemplate.type.contains("Gas") || selectedTemplate.type.contains("Acid")) "EXTREME" else if (selectedTemplate.id == "moon") "NONE" else "STABLE"
                                Text(
                                    text = "$densityFactor",
                                    color = if (densityFactor == "EXTREME") LaserPink else if (densityFactor == "NONE") Color.Gray else BioGreen,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val progress = when (selectedTemplate.id) {
                                "jupiter", "saturn", "venus", "acheron" -> 0.9f
                                "earth", "neptune", "uranus", "pandora" -> 0.65f
                                "mars" -> 0.35f
                                else -> 0.1f
                            }
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(4.dp),
                                color = LaserPink,
                                trackColor = Color.Black.copy(alpha = 0.5f)
                            )
                        }

                        // Stat 3: Spaghettification Resistance Limit (Roche Limit shear)
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = "Stress",
                                        tint = selectedTemplate.atmosphereColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Tidal Rigidity Rank", color = EventHorizonWhite, fontSize = 12.sp)
                                }
                                val limitStr = if (selectedTemplate.id == "mercury") "STIFF MINERAL" else "FLUIDIC SPHERE"
                                Text(
                                    text = "$limitStr",
                                    color = selectedTemplate.atmosphereColor,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val progress = if (selectedTemplate.id == "mercury") 0.8f else if (selectedTemplate.isSciFi) 0.55f else 0.3f
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(4.dp),
                                color = selectedTemplate.atmosphereColor,
                                trackColor = Color.Black.copy(alpha = 0.5f)
                            )
                        }
                    }

                    HorizontalDivider(color = CyberCyan.copy(alpha = 0.15f))

                    // Scientific Fact files
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Facts",
                                tint = BioGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "GRAVITATIONAL ANOMALY COEFFICIENT //",
                                color = BioGreen,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedTemplate.funFacts,
                            color = EventHorizonWhite.copy(alpha = 0.75f),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Confirm Deployment button
            Button(
                onClick = {
                    viewModel.setScreen(SimulatorViewModel.Screen.Simulation)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 24.dp)
                    .testTag("confirm_deploy_button"),
                colors = ButtonDefaults.buttonColors(containerColor = selectedTemplate.atmosphereColor),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "DEPLOY ${selectedTemplate.name.uppercase()} TO QUANTUM SLINGER",
                    color = SpaceBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
}
