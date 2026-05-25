package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SpaceBlack

@Composable
fun BlackHoleApp(
    viewModel: SimulatorViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val haptic = LocalHapticFeedback.current

    // Bind ViewModel Haptic Trigger channel to Compose local device triggers
    LaunchedEffect(Unit) {
        viewModel.onImpactHaptic = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        viewModel.onFrictionHaptic = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    MyApplicationTheme {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .testTag("app_main_surface"),
            color = SpaceBlack
        ) {
            // High-impact animated transit wrapper
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(350)) + scaleIn(initialScale = 0.96f, animationSpec = tween(350)))
                        .togetherWith(fadeOut(animationSpec = tween(250)))
                },
                label = "master_screen_transit"
            ) { targetState ->
                when (targetState) {
                    is SimulatorViewModel.Screen.Home -> HomeScreen(viewModel = viewModel)
                    is SimulatorViewModel.Screen.Selection -> PlanetSelectScreen(viewModel = viewModel)
                    is SimulatorViewModel.Screen.Simulation -> SimulationScreen(viewModel = viewModel)
                    is SimulatorViewModel.Screen.CinematicView -> CinematicDestructionOverlay(viewModel = viewModel)
                    is SimulatorViewModel.Screen.Settings -> SettingsMenu(viewModel = viewModel)
                }
            }
        }
    }
}
