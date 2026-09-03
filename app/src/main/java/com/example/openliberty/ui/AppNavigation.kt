package com.example.openliberty.ui

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.openliberty.ui.game.GameScreen
import com.example.openliberty.ui.game.VehicleTestScreen
import com.example.openliberty.ui.inspector.RenderWareInspectorScreen
import com.example.openliberty.ui.menu.CreditsScreen
import com.example.openliberty.ui.menu.MainMenuScreen
import com.example.openliberty.ui.menu.OptionsMenuScreen
import com.example.openliberty.viewmodel.AppScreen
import com.example.openliberty.viewmodel.GameViewModel

@Composable
fun AppNavigation(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Crossfade(targetState = state.currentScreen, label = "ScreenTransition") { screen ->
        when (screen) {
            AppScreen.MAIN_MENU -> MainMenuScreen(viewModel = viewModel, modifier = modifier)
            AppScreen.OPEN_WORLD -> GameScreen(viewModel = viewModel, modifier = modifier)
            AppScreen.VEHICLE_TEST -> VehicleTestScreen(viewModel = viewModel, modifier = modifier)
            AppScreen.RENDERWARE_INSPECTOR -> RenderWareInspectorScreen(viewModel = viewModel, modifier = modifier)
            AppScreen.OPTIONS -> OptionsMenuScreen(viewModel = viewModel, modifier = modifier)
            AppScreen.CREDITS -> CreditsScreen(viewModel = viewModel, modifier = modifier)
        }
    }
}
