package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import com.example.data.*
import com.example.ui.SimulatorViewModel
import com.example.ui.theme.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun SimulationCanvas(
    viewModel: SimulatorViewModel,
    modifier: Modifier = Modifier,
    isCinematicZoomActive: Boolean = false
) {
    val activePlanets by viewModel.activePlanets.collectAsState()
    val particles by viewModel.particles.collectAsState()
    val dragSling by viewModel.dragSling.collectAsState()
    val bhSize by viewModel.blackHoleSize.collectAsState()
    val gravityFactor by viewModel.gravityStrength.collectAsState()

    // Use a locally computed continuous rotation angle to avoid state lag
    var rotationAngle by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            withFrameMillis { time ->
                // Orbit disc speeds
                rotationAngle = (time * 0.04f) % 360f
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("simulation_canvas_block")
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        // Sync center coordinates to ViewModel for sling math
        val center = Offset(widthPx / 2f, heightPx / 2f)
        viewModel.canvasCenterX = center.x
        viewModel.canvasCenterY = center.y

        // Track orbit path prediction points based on slingshot drag
        val predictionPoints = remember(dragSling) {
            viewModel.computeOrbitPredictionPoints()
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            viewModel.onDragStart(offset.x, offset.y)
                        },
                        onDragEnd = {
                            viewModel.onDragEnd()
                        },
                        onDragCancel = {
                            viewModel.onDragEnd()
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            viewModel.onDragMove(change.position.x, change.position.y)
                        }
                    )
                }
        ) {
            // 1. DRAW BACKGROUND SPACE NEBULA & CURVED TIMESPACE GRID
            drawSpaceGrid(center, bhSize)

            // 2. DRAW ROTATING ACCRETION DISC AND EVENT HORIZON HALO
            drawBlackHoleDisk(center, bhSize, rotationAngle)

            // 3. DRAW SYSTEM PARTICLES (stars, cosmic dust, planetary debris)
            drawSpaceParticles(center, particles)

            // 4. DRAW ACTIVE CELESTIAL BODIES
            drawActivePlanets(center, activePlanets, bhSize)

            // 5. DRAW SLINGSHOT VECTOR PREVIEW & TRAJECTORY DOTS
            drawSlingShotHUD(center, dragSling, predictionPoints, viewModel.selectedPlanetTemplate.value)

            // 6. DRAW SINGULARITY INNER VOID (Pure dark eclipse)
            drawSingularityCore(center, bhSize)
        }
    }
}

/**
 * Calculates Einsteinian Gravitational Lensing coordinates distortion
 * Bends standard coordinate lines inwards around the black hole core.
 */
private fun DrawScope.drawSpaceGrid(center: Offset, bhSize: Float) {
    val sizeW = size.width
    val sizeH = size.height

    val gridSpacing = 48f
    val horizonRadius = bhSize

    // Let's draw curved horizon lattices (rows & columns)
    // Draw columns
    var colX = 0f
    while (colX < sizeW) {
        val points = mutableListOf<Offset>()
        var rowY = 0f
        while (rowY < sizeH) {
            // Apply coordinates bending
            val distorted = calculateLensedPoint(colX, rowY, center, horizonRadius)
            points.add(distorted)
            rowY += 24f
        }
        // Draw the curved line
        for (i in 0 until points.size - 1) {
            drawLine(
                color = NeonBlue.copy(alpha = 0.08f),
                start = points[i],
                end = points[i + 1],
                strokeWidth = 1.5f
            )
        }
        colX += gridSpacing
    }

    // Draw rows
    var rY = 0f
    while (rY < sizeH) {
        val points = mutableListOf<Offset>()
        var cX = 0f
        while (cX < sizeW) {
            val distorted = calculateLensedPoint(cX, rY, center, horizonRadius)
            points.add(distorted)
            cX += 24f
        }
        for (i in 0 until points.size - 1) {
            drawLine(
                color = NeonBlue.copy(alpha = 0.08f),
                start = points[i],
                end = points[i + 1],
                strokeWidth = 1.5f
            )
        }
        rY += gridSpacing
    }
}

private fun calculateLensedPoint(
    rawX: Float,
    rawY: Float,
    center: Offset,
    hornRad: Float
): Offset {
    val dx = rawX - center.x
    val dy = rawY - center.y
    val dist = sqrt(dx * dx + dy * dy)

    if (dist < hornRad * 0.95f) {
        // Points inside singularity sink deeply
        val ratio = dist / (hornRad * 0.95f)
        return Offset(
            center.x + dx * (0.01f + ratio * 0.1f),
            center.y + dy * (0.01f + ratio * 0.1f)
        )
    }

    // Einstein-like space distortion formula
    // Pulls points inwards inversely proportional to distance log-linear
    val gravityReach = hornRad * 5.0f
    if (dist < gravityReach) {
        val stretchStrength = (gravityReach - dist) / gravityReach
        // Pull towards singularity
        val pullFactor = 1.0f - (0.42f * stretchStrength * (hornRad / (dist + 50f)))
        return Offset(
            center.x + dx * pullFactor,
            center.y + dy * pullFactor
        )
    }

    return Offset(rawX, rawY)
}

/**
 * Renders multiple layers of glowing, rotating accretion dust discs and flares.
 */
private fun DrawScope.drawBlackHoleDisk(
    center: Offset,
    bhSize: Float,
    rotation: Float
) {
    val hr = bhSize

    // Base Radial Gravity Orange glow beneath everything
    drawCircle(
        brush = Brush.radialGradient(
            0.0f to PlasmaOrange.copy(alpha = 0.8f),
            0.4f to PlasmaOrange.copy(alpha = 0.3f),
            0.8f to LaserPink.copy(alpha = 0.05f),
            1.0f to Color.Transparent,
            center = center,
            radius = hr * 5.5f
        ),
        radius = hr * 5.5f,
        center = center
    )

    // Inner glowing accretion disc rings with different rotation speeds
    // Layer 1: Warm Outer Gas Ring (Saturn-like spiral gas)
    withTransform({
        rotate(rotation * 0.6f, center)
    }) {
        drawCircle(
            color = PlasmaOrange.copy(alpha = 0.25f),
            radius = hr * 2.8f,
            center = center,
            style = Stroke(
                width = hr * 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(120f, 60f), 0f)
            )
        )
    }

    // Layer 2: Fast Hot Inner Accretion Stream
    withTransform({
        rotate(-rotation * 1.8f, center)
    }) {
        drawCircle(
            color = CyberCyan.copy(alpha = 0.4f),
            radius = hr * 1.6f,
            center = center,
            style = Stroke(
                width = hr * 0.6f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(80f, 40f, 120f, 60f), 0f)
            )
        )
    }

    // Layer 3: Blazing Laser Accretion Vortex
    withTransform({
        rotate(rotation * 2.2f, center)
    }) {
        drawCircle(
            color = Color.White.copy(alpha = 0.62f),
            radius = hr * 1.25f,
            center = center,
            style = Stroke(
                width = hr * 0.35f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(200f, 150f), 45f)
            )
        )
    }

    // Glowing Event Horizon Boundary (Neon ring surrounding singularity)
    drawCircle(
        brush = Brush.radialGradient(
            0.7f to EventHorizonWhite,
            0.88f to CyberCyan.copy(alpha = 0.8f),
            0.96f to QuantumPurple.copy(alpha = 0.2f),
            1.0f to Color.Transparent,
            center = center,
            radius = hr * 1.15f
        ),
        radius = hr * 1.15f,
        center = center
    )
}

/**
 * Draws background slow drift stars and active comet debris spirals.
 */
private fun DrawScope.drawSpaceParticles(
    center: Offset,
    particles: List<CosmicParticle>
) {
    for (particle in particles) {
        val px = center.x + particle.x
        val py = center.y + particle.y

        // Guard boundaries
        if (px < 0 || px > size.width || py < 0 || py > size.height) continue

        drawCircle(
            color = particle.color.copy(alpha = particle.alpha),
            radius = particle.size,
            center = Offset(px, py)
        )
    }
}

/**
 * Renders simulated planets, including spaghettification stretching and atmospheric flares.
 */
private fun DrawScope.drawActivePlanets(
    center: Offset,
    activePlanets: List<ActivePlanet>,
    bhSize: Float
) {
    for (planet in activePlanets) {
        val px = center.x + planet.x
        val py = center.y + planet.y

        // A. Draw orbital fade path trail
        val trailCount = planet.trail.size
        for (i in 0 until trailCount - 1) {
            val p1 = planet.trail[i]
            val p2 = planet.trail[i + 1]
            val alpha = (i.toFloat() / trailCount) * 0.8f
            drawLine(
                color = planet.template.atmosphereColor.copy(alpha = alpha),
                start = Offset(center.x + p1.first, center.y + p1.second),
                end = Offset(center.x + p2.first, center.y + p2.second),
                strokeWidth = planet.size * 0.45f * (i.toFloat() / trailCount),
                cap = StrokeCap.Round
            )
        }

        // B. Cinematic Spaghettification Calculations
        val dx = planet.x
        val dy = planet.y
        val distToCenter = sqrt(dx * dx + dy * dy)
        val angleToCore = atan2(dy, dx) // direction towards center

        // Draw planet body. If spaghettified, draw stretched ellipse rotated towards the center!
        withTransform({
            translate(px, py)
            // Rotate so X axis points directly towards the singularity
            // (Atan2 returns angle, rotate expects degrees)
            val deg = Math.toDegrees(angleToCore.toDouble()).toFloat()
            rotate(deg, Offset.Zero)
        }) {
            // Under rotation, X axis points towards black hole, Y axis is tangential.
            // Spaghettification STRETCHES along gravity (X axis) and SQUEEZES along tangent (Y axis)
            val stretchedWidth = planet.size * planet.stretchFactor
            val squeezedHeight = planet.size / sqrt(planet.stretchFactor)

            // 1. Friction Atmosphere Burn Core (Facing forward towards black hole core)
            if (planet.stressLevel > 0.05f) {
                // Glow fire flare shield
                drawArc(
                    brush = Brush.horizontalGradient(
                        0.0f to Color.White,
                        0.5f to planet.template.atmosphereColor.copy(alpha = 0.9f),
                        1.0f to Color.Transparent,
                        startX = -stretchedWidth * 1.5f,
                        endX = 0f
                    ),
                    startAngle = 120f,
                    sweepAngle = 120f,
                    useCenter = false,
                    size = Size(stretchedWidth * 2.5f, squeezedHeight * 2.5f),
                    topLeft = Offset(-stretchedWidth * 1.25f, -squeezedHeight * 1.25f)
                )
            }

            // 2. Main Planet Base Sphere
            // Drawn as elliptical shape reflecting tides
            drawOval(
                color = planet.template.color,
                size = Size(stretchedWidth * 2f, squeezedHeight * 2f),
                topLeft = Offset(-stretchedWidth, -squeezedHeight)
            )

            // 3. Planet Atmosphere Glow Rim Overlay
            drawOval(
                color = planet.template.atmosphereColor.copy(alpha = 0.5f),
                size = Size(stretchedWidth * 2.1f, squeezedHeight * 2.1f),
                topLeft = Offset(-stretchedWidth * 1.05f, -squeezedHeight * 1.05f),
                style = Stroke(width = 2.5f)
            )

            // Special Saturn Ring representation
            if (planet.template.id == "saturn") {
                // Draw Saturn rings tilted
                drawOval(
                    color = Color(0xFFF4E2BB).copy(alpha = 0.7f),
                    size = Size(stretchedWidth * 4.4f, squeezedHeight * 0.8f),
                    topLeft = Offset(-stretchedWidth * 2.2f, -squeezedHeight * 0.4f),
                    style = Stroke(width = squeezedHeight * 0.35f)
                )
            }
        }
    }
}

/**
 * Draws slingshot HUD controls, including vector pull line and predicted orbital path points.
 */
private fun DrawScope.drawSlingShotHUD(
    center: Offset,
    dragSling: SimulatorViewModel.DragSlingState?,
    predictionPoints: List<Pair<Float, Float>>,
    selectedPlanet: CelestialBody
) {
    if (dragSling == null || !dragSling.isDragging) return

    val startAbs = Offset(center.x + dragSling.startX, center.y + dragSling.startY)
    val currAbs = Offset(center.x + dragSling.currentX, center.y + dragSling.currentY)

    // A. Draw predicted orbit trail dots
    for (i in 0 until predictionPoints.size - 1) {
        val pt = predictionPoints[i]
        val opt = Offset(center.x + pt.first, center.y + pt.second)
        // fade dots out along distance
        val dotAlpha = 1.0f - (i.toFloat() / predictionPoints.size)

        drawCircle(
            color = CyberCyan.copy(alpha = dotAlpha),
            radius = 3.5f,
            center = opt
        )
    }

    // B. Draw pull line (Laser sling rope)
    drawLine(
        color = CyberCyan.copy(alpha = 0.8f),
        start = startAbs,
        end = currAbs,
        strokeWidth = 3f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
    )

    // C. Draw launching anchor nodes
    drawCircle(
        color = CyberCyan,
        radius = 8f,
        center = startAbs
    )

    drawCircle(
        color = LaserPink,
        radius = 6f,
        center = currAbs
    )

    // Draw placeholder overlay of throwing planet under finger
    drawCircle(
        color = selectedPlanet.color.copy(alpha = 0.6f),
        radius = selectedPlanet.radius,
        center = startAbs
    )
    drawCircle(
        color = selectedPlanet.atmosphereColor.copy(alpha = 0.4f),
        radius = selectedPlanet.radius * 1.3f,
        center = startAbs,
        style = Stroke(width = 2f)
    )
}

/**
 * Renders the absolute central pitch black circular core (singularity).
 */
private fun DrawScope.drawSingularityCore(center: Offset, bhSize: Float) {
    // Pure absolute void
    drawCircle(
        color = Color.Black,
        radius = bhSize,
        center = center
    )
}
