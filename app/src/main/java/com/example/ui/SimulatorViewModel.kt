package com.example.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class SimulatorViewModel : ViewModel() {

    // --- Screen State Navigation ---
    sealed class Screen {
        object Home : Screen()
        object Selection : Screen()
        object Simulation : Screen()
        object CinematicView : Screen()
        object Settings : Screen()
    }

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // --- Active Selected Planet Template ---
    private val _selectedPlanetTemplate = MutableStateFlow<CelestialBody>(CelestialCatalogue.bodies[0])
    val selectedPlanetTemplate: StateFlow<CelestialBody> = _selectedPlanetTemplate.asStateFlow()

    // --- Sliders and Simulation Configuration ---
    private val _blackHoleSize = MutableStateFlow(60f) // visual radius in dp
    val blackHoleSize: StateFlow<Float> = _blackHoleSize.asStateFlow()

    private val _gravityStrength = MutableStateFlow(2.5f) // force scalar
    val gravityStrength: StateFlow<Float> = _gravityStrength.asStateFlow()

    private val _isSlowMotion = MutableStateFlow(false)
    val isSlowMotion: StateFlow<Boolean> = _isSlowMotion.asStateFlow()

    private val _isCinematicMode = MutableStateFlow(true)
    val isCinematicMode: StateFlow<Boolean> = _isCinematicMode.asStateFlow()

    private val _isSoundEnabled = MutableStateFlow(true)
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    // --- Black Hole Accumulated Stats ---
    private val _blackHoleMassExaTonnes = MutableStateFlow(25000.0) // accumulates as planets are eaten
    val blackHoleMassExaTonnes: StateFlow<Double> = _blackHoleMassExaTonnes.asStateFlow()

    private val _planetsConsumedCount = MutableStateFlow(0)
    val planetsConsumedCount: StateFlow<Int> = _planetsConsumedCount.asStateFlow()

    // --- Physics Elements ---
    private val _activePlanets = MutableStateFlow<List<ActivePlanet>>(emptyList())
    val activePlanets: StateFlow<List<ActivePlanet>> = _activePlanets.asStateFlow()

    private val _particles = MutableStateFlow<List<CosmicParticle>>(emptyList())
    val particles: StateFlow<List<CosmicParticle>> = _particles.asStateFlow()

    // --- Holographic Info / Warning HUD logs ---
    private val _hudLogs = MutableStateFlow<List<String>>(listOf(
        "SYSTEM INITIALIZED // NESTED SINGULARITY DETECTED",
        "GRAVITATIONAL SHEARS NOMINAL. READY FOR OBJECT HORIZON ENTRY."
    ))
    val hudLogs: StateFlow<List<String>> = _hudLogs.asStateFlow()

    // --- Interaction / Drag Sling Launch Preview State ---
    // Start, Current coordinates relative to canvas center
    data class DragSlingState(
        val startX: Float,
        val startY: Float,
        val currentX: Float,
        val currentY: Float,
        val isDragging: Boolean = false
    )
    private val _dragSling = MutableStateFlow<DragSlingState?>(null)
    val dragSling: StateFlow<DragSlingState?> = _dragSling.asStateFlow()

    // Screen Center placeholder (dynamic inside Canvas, but updated from view overlay)
    var canvasCenterX: Float = 500f
    var canvasCenterY: Float = 800f

    // Haptics channel callback
    var onImpactHaptic: (() -> Unit)? = null
    var onFrictionHaptic: (() -> Unit)? = null

    // Real-time animation loop job
    private var simulationJob: Job? = null

    init {
        startSimulationLoop()
        initializeBackgroundStars()
    }

    fun setScreen(screen: Screen) {
        _currentScreen.value = screen
        addHudLog("NAVNAV // INTERFACE TRANSIT TO ${screen.javaClass.simpleName.uppercase()}")
    }

    fun selectPlanetTemplate(body: CelestialBody) {
        _selectedPlanetTemplate.value = body
        addHudLog("LOADLOAD // CELESTIAL LOADED: ${body.name.uppercase()} (MASS: ${body.mass}x)")
    }

    fun updateBlackHoleSize(size: Float) {
        _blackHoleSize.value = size
    }

    fun updateGravityStrength(strength: Float) {
        _gravityStrength.value = strength
    }

    fun toggleSlowMotion() {
        _isSlowMotion.value = !_isSlowMotion.value
        addHudLog(if (_isSlowMotion.value) "MODEMODE // TIME DILATION MATRIX ACTIVATED [SLOW-MO]" else "MODEMODE // STANDARD TIME CONTINUUM COMPLETED")
    }

    fun toggleCinematicMode() {
        _isCinematicMode.value = !_isCinematicMode.value
        addHudLog(if (_isCinematicMode.value) "HUDHUD // DYNAMIC EVENT FOCUSING CAM ON" else "HUDHUD // MANUAL MULTI-ANGLE FOCUS ON")
    }

    fun toggleSoundEnabled() {
        _isSoundEnabled.value = !_isSoundEnabled.value
        addHudLog(if (_isSoundEnabled.value) "AUDIOSYNTH // SYSTEM SENSORS TRANSMITTING SOUND" else "AUDIOSYNTH // VACUUM SILENCE PROTOCOL ACTIVE")
    }

    fun resetSimulation() {
        _activePlanets.value = emptyList()
        _blackHoleMassExaTonnes.value = 25000.0
        _planetsConsumedCount.value = 0
        initializeBackgroundStars()
        _hudLogs.value = listOf(
            "CORE REBOOT // SINGULARITY STABILIZED",
            "ACCUMULATED FIELD RESTORED // READY FOR PLANETARY TARGETING"
        )
    }

    fun addHudLog(message: String) {
        val current = _hudLogs.value.toMutableList()
        current.add(0, "SYSTEM: $message")
        if (current.size > 8) {
            current.removeAt(current.size - 1)
        }
        _hudLogs.value = current
    }

    // --- Slingshot Sling Gestures Handler ---
    fun onDragStart(x: Float, y: Float) {
        _dragSling.value = DragSlingState(
            startX = x - canvasCenterX,
            startY = y - canvasCenterY,
            currentX = x - canvasCenterX,
            currentY = y - canvasCenterY,
            isDragging = true
        )
    }

    fun onDragMove(x: Float, y: Float) {
        _dragSling.value?.let { current ->
            _dragSling.value = current.copy(
                currentX = x - canvasCenterX,
                currentY = y - canvasCenterY
            )
        }
    }

    fun onDragEnd() {
        val sling = _dragSling.value ?: return
        _dragSling.value = null

        // Velocity is proportional to the slingshot extension vector
        // Drag vector: current -> start
        // Throw planet in direction of vector pointing from drag finger position back to release origin
        val vx = (sling.startX - sling.currentX) * 0.12f
        val vy = (sling.startY - sling.currentY) * 0.12f

        launchPlanet(
            x = sling.startX,
            y = sling.startY,
            vx = vx,
            vy = vy
        )
    }

    private fun launchPlanet(x: Float, y: Float, vx: Float, vy: Float) {
        val template = _selectedPlanetTemplate.value
        val uniqueId = System.currentTimeMillis()
        val newPlanet = ActivePlanet(
            id = uniqueId,
            template = template,
            x = x,
            y = y,
            vx = vx,
            vy = vy,
            mass = template.mass,
            size = template.radius
        )

        val current = _activePlanets.value.toMutableList()
        current.add(newPlanet)
        _activePlanets.value = current

        addHudLog("LAUNCH // ${template.name.uppercase()} THROWN AT VELOCITY VECTOR (${"%.1f".format(vx)}, ${"%.1f".format(vy)})")

        // Switch screen to Main Simulation if they were in Selector
        if (_currentScreen.value == Screen.Selection) {
            _currentScreen.value = Screen.Simulation
        }
    }

    // Spawns immediate planet directly with simple orbiting velocity for testing/convenience
    fun spawnOrbitingPlanet() {
        val template = _selectedPlanetTemplate.value
        // Determine a safe radius outside event horizon
        val orbitRadius = _blackHoleSize.value * 2.8f + 120f
        // Let's place it at top-left
        val angle = Math.random() * 2.0 * Math.PI
        val px = (orbitRadius * cos(angle)).toFloat()
        val py = (orbitRadius * sin(angle)).toFloat()

        // Orbital speed: v = sqrt(G*M / r)
        val gCoeff = _gravityStrength.value * 7000f
        val orbitalSpeed = sqrt(gCoeff / orbitRadius)

        // Velocity vector perpendicular to position vector
        val vx = (-orbitalSpeed * sin(angle)).toFloat()
        val vy = (orbitalSpeed * cos(angle)).toFloat()

        launchPlanet(px, py, vx, vy)
    }

    // --- Background Stars initialization ---
    private fun initializeBackgroundStars() {
        val starPool = mutableListOf<CosmicParticle>()
        for (i in 0..120) {
            val theta = Math.random() * 2 * Math.PI
            // Star fields clustered beautifully around core
            val r = (40f + Math.random() * 800f).toFloat()
            val sx = (r * cos(theta)).toFloat()
            val sy = (r * sin(theta)).toFloat()

            // slow orbital drift
            val starDrift = 0.2f + (Math.random() * 0.5f).toFloat()
            val vx = (-starDrift * sin(theta)).toFloat()
            val vy = (starDrift * cos(theta)).toFloat()

            starPool.add(
                CosmicParticle(
                    x = sx,
                    y = sy,
                    vx = vx,
                    vy = vy,
                    color = when (Math.random() * 4) {
                        in 0.0..1.0 -> CyberCyan.copy(alpha = (0.2f + Math.random() * 0.6f).toFloat())
                        in 1.0..2.0 -> NeonBlue.copy(alpha = (0.2f + Math.random() * 0.6f).toFloat())
                        in 2.0..3.0 -> LaserPink.copy(alpha = (0.1f + Math.random() * 0.5f).toFloat())
                        else -> Color.White.copy(alpha = (0.4f + Math.random() * 0.6f).toFloat())
                    },
                    size = (1f + Math.random() * 2.5f).toFloat(),
                    alpha = (0.2f + Math.random() * 0.8f).toFloat(),
                    decay = 0.0f
                )
            )
        }
        _particles.value = starPool
    }

    /**
     * Compute Orbit Preview Path Points
     * Simulates forward 60 frames for the current drag coordinates to paint dots.
     */
    fun computeOrbitPredictionPoints(): List<Pair<Float, Float>> {
        val sling = _dragSling.value ?: return emptyList()
        val template = _selectedPlanetTemplate.value

        var px = sling.startX
        var py = sling.startY
        var pvx = (sling.startX - sling.currentX) * 0.12f
        var pvy = (sling.startY - sling.currentY) * 0.12f

        val points = mutableListOf<Pair<Float, Float>>()
        val simSteps = 45
        val bhRadius = _blackHoleSize.value
        val gravityMult = _gravityStrength.value * 12000f

        for (i in 0 until simSteps) {
            val dist = sqrt(px * px + py * py)
            if (dist < bhRadius) break // Sucked inside

            points.add(Pair(px, py))

            // Force calculation
            val force = gravityMult / (dist * dist + 1500f) // Softened gravity to avoid numerical explosion
            val ax = -force * (px / dist)
            val ay = -force * (py / dist)

            pvx += ax
            pvy += ay
            px += pvx
            py += pvy
        }
        return points
    }

    // --- Main Physics/Animation Ticker ---
    private fun startSimulationLoop() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            while (true) {
                // ~60 FPS target delay
                delay(16)
                updateTick()
            }
        }
    }

    private fun updateTick() {
        // Slow motion scale factor
        val dt = if (_isSlowMotion.value) 0.18f else 1.0f

        val bhRadius = _blackHoleSize.value
        val gScalar = _gravityStrength.value * 15000f // Scaling factor for gravity intensity
        val shearRadius = bhRadius * 2.8f // Radius where spaghettification begins

        // 1. UPDATE PLANET ORBITS
        val currPlanets = _activePlanets.value
        val nextPlanets = mutableListOf<ActivePlanet>()

        for (planet in currPlanets) {
            val px = planet.x
            val py = planet.y
            val dist = sqrt(px * px + py * py)

            // A. Gravity Well Collision Check
            if (dist <= bhRadius + 5f) {
                // PLANET CONSUMED!
                _planetsConsumedCount.value += 1
                val massGained = planet.template.mass * 840.4
                _blackHoleMassExaTonnes.value += massGained

                addHudLog("CRITICAL // ${planet.template.name.uppercase()} HAS CROSSED EVENT HORIZON // ADDED ${"%,.1f".format(massGained)} EXA-TONNES TO CORRESPONDING SINGULARITY.")

                // Trigger large flash explosion particles at consumption coordinate
                spawnConsumptionExplosion(px, py, planet.template.atmosphereColor)
                if (_isSoundEnabled.value) {
                    onImpactHaptic?.invoke()
                }

                // If in standard viewport, automatically trigger a brief visual flare or transition
                if (_isCinematicMode.value && _currentScreen.value == Screen.Simulation) {
                    _currentScreen.value = Screen.CinematicView
                    // Automatically drop back to simulation after a cinematic climax
                    viewModelScope.launch {
                        delay(2800)
                        if (_currentScreen.value == Screen.CinematicView) {
                            _currentScreen.value = Screen.Simulation
                        }
                    }
                }
                continue // Do not keep active
            }

            // B. Orbital Gravity Physics (Spaghettification & Velocity modification)
            val pullDirectionX = -px / dist
            val pullDirectionY = -py / dist

            val forceMagnitude = gScalar / (dist * dist + 800f) // softened gravity denominator
            val ax = forceMagnitude * pullDirectionX
            val ay = forceMagnitude * pullDirectionY

            // Integrate velocity & position
            planet.vx += ax * dt
            planet.vy += ay * dt
            planet.x += planet.vx * dt
            planet.y += planet.vy * dt

            // Update fading trace trail
            val newTrail = planet.trail.toMutableList()
            newTrail.add(Pair(px, py))
            if (newTrail.size > 22) {
                newTrail.removeAt(0)
            }
            planet.trail = newTrail
            planet.age++

            // C. Spaghettification & Atmospheric Friction Calculations
            if (dist < shearRadius) {
                planet.state = PlanetState.SPAGHETTIFYING

                // Linear scale of spaghettification stretch stretching
                val stretchAlpha = (shearRadius - dist) / (shearRadius - bhRadius)
                planet.stretchFactor = 1.0f + stretchAlpha * 4.4f // stretch up to 5x ratio
                planet.stressLevel = stretchAlpha // atmospheric thermal burns

                // Trigger vibration/feedback during extreme shearing
                if (_isSoundEnabled.value && Math.random() < 0.1) {
                    onFrictionHaptic?.invoke()
                }

                // Generate debris shedding trails
                if (Math.random() < 0.35 + (stretchAlpha * 0.4)) {
                    spawnDebrisShedding(planet.x, planet.y, planet.vx, planet.vy, planet.template.atmosphereColor)
                }

                if (dist < bhRadius * 1.5f) {
                    planet.state = PlanetState.COLLAPSING
                }
            } else {
                planet.state = PlanetState.ORBITING
                planet.stretchFactor = 1.0f
                planet.stressLevel = 0.0f
            }

            nextPlanets.add(planet)
        }
        _activePlanets.value = nextPlanets

        // 2. UPDATE PARTICLES (BACKGROUND STARS DISK FLOW AND DEBRIS COLLAPSE)
        val currParticles = _particles.value
        val nextParticles = mutableListOf<CosmicParticle>()

        // Maintain general background density by spawning replacement star flows
        var backgroundSpawnNeeded = 120 - currParticles.count { !it.isDebris }
        if (backgroundSpawnNeeded < 0) backgroundSpawnNeeded = 0

        for (particle in currParticles) {
            val px = particle.x
            val py = particle.y
            val r = sqrt(px * px + py * py)

            if (r < bhRadius) {
                // Swallowed by black hole
                continue
            }

            // Background particles slowly spin inward or float orbitally, debris spirals rapidly
            val angle = atan2(py, px)

            if (particle.isDebris) {
                // Heavy gravity pull + spiral spin
                val circularForceVal = (gScalar * 0.08f) / (r * 0.5f + 10f)
                val radialPull = (gScalar * 0.35f) / (r * r + 200f)

                // update particle velocities based on spiral direction
                val pullX = -px / r
                val pullY = -py / r
                val tangentX = -sin(angle)
                val tangentY = cos(angle)

                particle.vx += (pullX * radialPull + tangentX * circularForceVal) * dt
                particle.vy += (pullY * radialPull + tangentY * circularForceVal) * dt

                particle.x += particle.vx * dt
                particle.y += particle.vy * dt
                particle.alpha -= (particle.decay * dt)
            } else {
                // Background celestial stars orbit slowly
                // Drift is slow circular rotation + slight center lensing distortion
                val rotationalDrift = 1.6f / (sqrt(r) * 0.05f + 1f)
                val tx = -sin(angle) * rotationalDrift
                val ty = cos(angle) * rotationalDrift

                // subtle gravitational warping towards singularity
                val warpScalar = 0.05f * (shearRadius / (r + 10f))
                val wx = (-px / r) * warpScalar
                val wy = (-py / r) * warpScalar

                particle.x += (tx + wx) * dt
                particle.y += (ty + wy) * dt
            }

            if (particle.alpha > 0.01f) {
                nextParticles.add(particle)
            }
        }

        // Refill slowly background star fields
        for (i in 0 until backgroundSpawnNeeded) {
            val theta = Math.random() * 2 * Math.PI
            val r = (bhRadius * 1.5f + Math.random() * 650f).toFloat()
            val sx = (r * cos(theta)).toFloat()
            val sy = (r * sin(theta)).toFloat()

            nextParticles.add(
                CosmicParticle(
                    x = sx,
                    y = sy,
                    vx = 0f,
                    vy = 0f,
                    color = when (Math.random() * 3) {
                        in 0.0..1.0 -> CyberCyan.copy(alpha = 0.4f)
                        in 1.0..2.0 -> QuantumPurple.copy(alpha = 0.4f)
                        else -> Color.White.copy(alpha = 0.5f)
                    },
                    size = (1f + Math.random() * 2f).toFloat(),
                    alpha = 0.3f + (Math.random() * 0.5f).toFloat(),
                    decay = 0.0f
                )
            )
        }

        _particles.value = nextParticles
    }

    // Spawns debris trails shedding off the spaghettifying celestial body
    private fun spawnDebrisShedding(px: Float, py: Float, vx: Float, vy: Float, color: Color) {
        val current = _particles.value.toMutableList()
        val numDebris = (2 + (Math.random() * 3)).toInt()

        for (i in 0 until numDebris) {
            // Ejected backwards and slightly outward
            val dispersionAngle = Math.random() * 2 * Math.PI
            val dispSpeed = (1f + Math.random() * 4f).toFloat()
            val pvx = (vx * 0.7f + cos(dispersionAngle) * dispSpeed).toFloat()
            val pvy = (vy * 0.7f + sin(dispersionAngle) * dispSpeed).toFloat()

            current.add(
                CosmicParticle(
                    x = px + ((-5..5).random()).toFloat(),
                    y = py + ((-5..5).random()).toFloat(),
                    vx = pvx,
                    vy = pvy,
                    color = color,
                    size = (2f + Math.random() * 3f).toFloat(),
                    alpha = 1.0f,
                    decay = 0.02f + (Math.random() * 0.05f).toFloat(),
                    isDebris = true
                )
            )
        }
        _particles.value = current
    }

    // Massive planet destruction explosion rings
    private fun spawnConsumptionExplosion(px: Float, py: Float, color: Color) {
        val current = _particles.value.toMutableList()
        val numExplosionParticles = 65

        // Spawn concentric rings sending debris flying in all radial angles!
        for (i in 0 until numExplosionParticles) {
            val angle = (i * (2f * Math.PI / numExplosionParticles)) + (Math.random() * 0.2)
            val speed = (4f + Math.random() * 15f).toFloat()
            val pvx = (speed * cos(angle)).toFloat()
            val pvy = (speed * sin(angle)).toFloat()

            // Flash colors
            val pColor = when (Math.random() * 3) {
                in 0.0..1.0 -> color
                in 1.0..2.0 -> Color.White
                else -> PlasmaOrange
            }

            current.add(
                CosmicParticle(
                    x = px,
                    y = py,
                    vx = pvx,
                    vy = pvy,
                    color = pColor,
                    size = (2f + Math.random() * 5f).toFloat(),
                    alpha = 1.0f,
                    decay = 0.015f + (Math.random() * 0.03f).toFloat(),
                    isDebris = true
                )
            )
        }

        // Add secondary central pressure wave (smaller slower particles)
        for (i in 0..25) {
            val angle = Math.random() * 2 * Math.PI
            val speed = (1f + Math.random() * 4f).toFloat()
            current.add(
                CosmicParticle(
                    x = px,
                    y = py,
                    vx = (speed * cos(angle)).toFloat(),
                    vy = (speed * sin(angle)).toFloat(),
                    color = QuantumPurple,
                    size = (1.5f + Math.random() * 3f).toFloat(),
                    alpha = 1.0f,
                    decay = 0.01f + (Math.random() * 0.02f).toFloat(),
                    isDebris = true
                )
            )
        }

        _particles.value = current
    }

    override fun onCleared() {
        super.onCleared()
        simulationJob?.cancel()
    }
}
