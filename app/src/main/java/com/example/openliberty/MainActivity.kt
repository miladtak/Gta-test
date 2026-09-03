package com.example.openliberty

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.openliberty.ui.AppNavigation
import com.example.openliberty.ui.theme.OpenLibertyTheme
import com.example.openliberty.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OpenLibertyTheme {
                val gameViewModel: GameViewModel = viewModel()
                AppNavigation(viewModel = gameViewModel)
            }
        }
    }
}
