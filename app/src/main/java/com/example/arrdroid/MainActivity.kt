package com.example.arrdroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.arrdroid.ui.screens.SettingsScreen
import com.example.arrdroid.ui.screens.WantedScreen
import com.example.arrdroid.ui.theme.ArrdroidTheme
import com.example.arrdroid.viewmodel.SettingsViewModel
import com.example.arrdroid.viewmodel.WantedViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArrdroidTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    ArrdroidNavHost()
                }
            }
        }
    }
}

private enum class ArrdroidRoute {
    HOME,
    SETTINGS,
    WANTED
}

@Composable
private fun ArrdroidNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: ArrdroidRoute.HOME.name

    val context = LocalContext.current

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(context)
    )
    val wantedViewModel: WantedViewModel = viewModel(
        factory = WantedViewModel.Factory(context)
    )

    ArrdroidScaffold(
        navController = navController,
        currentRoute = currentRoute,
        settingsViewModel = settingsViewModel,
        wantedViewModel = wantedViewModel
    )
}

@Composable
private fun ArrdroidScaffold(
    navController: NavHostController,
    currentRoute: String,
    settingsViewModel: SettingsViewModel,
    wantedViewModel: WantedViewModel
) {
    NavHost(
        navController = navController,
        startDestination = ArrdroidRoute.HOME.name
    ) {
        composable(ArrdroidRoute.HOME.name) {
            com.example.arrdroid.ui.screens.HomeScreen(
                onOpenSettings = { navController.navigate(ArrdroidRoute.SETTINGS.name) },
                onOpenWanted = { navController.navigate(ArrdroidRoute.WANTED.name) }
            )
        }

        composable(ArrdroidRoute.SETTINGS.name) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(ArrdroidRoute.WANTED.name) {
            WantedScreen(
                viewModel = wantedViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

