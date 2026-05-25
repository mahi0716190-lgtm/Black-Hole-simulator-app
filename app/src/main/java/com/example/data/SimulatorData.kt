package com.example.data

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

/**
 * Static metadata representing a planet/celestial body option.
 */
data class CelestialBody(
    val id: String,
    val name: String,
    val type: String,
    val mass: Float, // Scale factor: e.g., Earth = 1.0f
    val radius: Float, // Visual draw radius
    val color: Color,
    val atmosphereColor: Color,
    val description: String,
    val funFacts: String,
    val isSciFi: Boolean = false
)

/**
 * Represents an active planet simulation instance in flight.
 */
data class ActivePlanet(
    val id: Long,
    val template: CelestialBody,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var mass: Float,
    var size: Float,
    var trail: List<Pair<Float, Float>> = emptyList(),
    var age: Int = 0,
    var state: PlanetState = PlanetState.ORBITING,
    var stretchFactor: Float = 1.0f, // 1.0 means no stretch, higher means pulled radially
    var stressLevel: Float = 0.0f, // 0.0 to 1.0 atmospheric friction, triggers flare colors
    var disintegrationProgress: Float = 0.0f // 1.0 means fully dissolved
)

enum class PlanetState {
    ORBITING,
    SPAGHETTIFYING, // Stretching and bleeding matter
    COLLAPSING,     // Breaking apart into core rings
    ABSORBED        // Merged with singularity
}

/**
 * Represents a particle (dust, spark, fragment) in deep space.
 */
data class CosmicParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    val size: Float,
    var alpha: Float,
    val decay: Float, // rate of alpha reduction per frame
    val isDebris: Boolean = false, // true if planetary fragment, false if background nebula dust
    var angle: Float = 0f,
    val speedFactor: Float = 1f
)

object CelestialCatalogue {
    val bodies = listOf(
        CelestialBody(
            id = "earth",
            name = "Earth",
            type = "Terrestrial",
            mass = 1.0f,
            radius = 20f,
            color = Color(0xFF2B82C9),
            atmosphereColor = Color(0xFF86E3CE),
            description = "Third planet from the Sun, sustaining bio-organic intelligence.",
            funFacts = "Protected by a magnetosphere which glows cyan under space friction."
        ),
        CelestialBody(
            id = "mars",
            name = "Mars",
            type = "Desert",
            mass = 0.6f,
            radius = 16f,
            color = Color(0xFFE25B38),
            atmosphereColor = Color(0xFFFF9E79),
            description = "The red planet, covered in iron-oxide dust and old volcanic calderas.",
            funFacts = "Produces deep orange-red oxide clouds when pulverized by gravity."
        ),
        CelestialBody(
            id = "jupiter",
            name = "Jupiter",
            type = "Gas Giant",
            mass = 12.0f,
            radius = 36f,
            color = Color(0xFFD4A373),
            atmosphereColor = Color(0xFFE9D8A6),
            description = "A massive planetary furnace. Its storms host extreme pressures.",
            funFacts = "Its metallic hydrogen core triggers blinding neon friction bursts."
        ),
        CelestialBody(
            id = "saturn",
            name = "Saturn",
            type = "Gas Giant",
            mass = 9.0f,
            radius = 30f,
            color = Color(0xFFE2C391),
            atmosphereColor = Color(0xFFFEFAE0),
            description = "Famous for its massive crystalline ice and silicated rock rings.",
            funFacts = "Disintegrating Saturn pulls its rings into spectacular spiral flares."
        ),
        CelestialBody(
            id = "neptune",
            name = "Neptune",
            type = "Ice Giant",
            mass = 4.5f,
            radius = 24f,
            color = Color(0xFF274690),
            atmosphereColor = Color(0xFF3A86C8),
            description = "Distantly frozen sphere lashed by supersonic ammonia winds.",
            funFacts = "Methane combustion creates cold neon-sapphire trails in vacuum."
        ),
        CelestialBody(
            id = "venus",
            name = "Venus",
            type = "Acid Furnace",
            mass = 0.9f,
            radius = 19f,
            color = Color(0xFFD8B168),
            atmosphereColor = Color(0xFFFFCC33),
            description = "Runaway greenhouse planet with crushing sulfur dioxide haze.",
            funFacts = "Ignites into a toxic yellow acid firestorm during spaghettification."
        ),
        CelestialBody(
            id = "mercury",
            name = "Mercury",
            type = "Iron Core",
            mass = 0.4f,
            radius = 12f,
            color = Color(0xFF8D99AE),
            atmosphereColor = Color(0xFFDDF0FF),
            description = "A scorched, cratered world heavily dense with minerals.",
            funFacts = "High iron content causes gray-indigo sparks and crackling metallic trails."
        ),
        CelestialBody(
            id = "uranus",
            name = "Uranus",
            type = "Ice Giant",
            mass = 3.8f,
            radius = 23f,
            color = Color(0xFFB5E2FA),
            atmosphereColor = Color(0xFF90E0EF),
            description = "Tilted 98 degrees on its axis, rotating horizontally.",
            funFacts = "Exhibits vertical fluorescent cyan bands under gravitational shears."
        ),
        CelestialBody(
            id = "moon",
            name = "The Moon",
            type = "Satellite",
            mass = 0.2f,
            radius = 10f,
            color = Color(0xFFCCCCCC),
            atmosphereColor = Color(0xFFEAEAEA),
            description = "Earth's tidal companion, battered by eons of impact craters.",
            funFacts = "Breaks apart into dry silicate dust particles without a fiery burn."
        ),
        CelestialBody(
            id = "cybertron",
            name = "Xenon-9",
            type = "Cyber-Grid",
            mass = 6.6f,
            radius = 28f,
            color = Color(0xFF0F2027),
            atmosphereColor = CyberCyan,
            description = "Sci-fi world consisting of synthetic metal and glowing quantum streams.",
            funFacts = "Pulses with bright electronic blue circuit flares when undergoing destruction.",
            isSciFi = true
        ),
        CelestialBody(
            id = "acheron",
            name = "Acheron Prime",
            type = "Magma Forge",
            mass = 3.2f,
            radius = 22f,
            color = Color(0xFF1E0B05),
            atmosphereColor = LaserPink,
            description = "A hellish world of black basalt plates floating on rivers of glowing pink plasma.",
            funFacts = "Explodes in high-intensity thermal pink shockwaves near the event horizon.",
            isSciFi = true
        ),
        CelestialBody(
            id = "pandora",
            name = "Pandora",
            type = "Bioluminescent",
            mass = 1.2f,
            radius = 18f,
            color = Color(0xFF2C103F),
            atmosphereColor = BioGreen,
            description = "Lush exo-moon containing bioluminescent fauna and floating mountains.",
            funFacts = "Green radioactive hyper-clouds trail the planet as it unravels into gravity.",
            isSciFi = true
        )
    )
}
