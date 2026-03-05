package com.example.arrdroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.arrdroid.ui.screens.ArtistDetailScreen
import com.example.arrdroid.ui.screens.ArtistListScreen
import com.example.arrdroid.ui.screens.HomeScreen
import com.example.arrdroid.ui.screens.SettingsScreen
import com.example.arrdroid.ui.screens.WantedScreen
import com.example.arrdroid.ui.theme.ArrdroidTheme
import com.example.arrdroid.viewmodel.ArtistViewModel
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

// ── Navigation routes ────────────────────────────────────────────────

private object Routes {
    const val HOME = "home"
    const val ARTISTS = "artists"
    const val ARTIST_DETAIL = "artist/{artistId}"
    const val WANTED = "wanted"
    const val SETTINGS = "settings"

    fun artistDetail(id: Int) = "artist/$id"
}

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Routes.HOME, "Home", Icons.Default.Home),
    BottomNavItem(Routes.ARTISTS, "Künstler", Icons.Default.MusicNote),
    BottomNavItem(Routes.WANTED, "Wanted", Icons.Default.PlaylistAdd),
    BottomNavItem(Routes.SETTINGS, "Settings", Icons.Default.Settings),
)

// ── Root composable ──────────────────────────────────────────────────

@Composable
private fun ArrdroidNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(context)
    )
    val wantedViewModel: WantedViewModel = viewModel(
        factory = WantedViewModel.Factory(context)
    )
    val artistViewModel: ArtistViewModel = viewModel(
        factory = ArtistViewModel.Factory(context)
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Zeige BottomBar nur auf den vier Haupt-Tabs
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        // Pop up to home so we don't stack tabs
                                        popUpTo(Routes.HOME) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenSettings = {
                        navController.navigate(Routes.SETTINGS) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onOpenWanted = {
                        navController.navigate(Routes.WANTED) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(Routes.ARTISTS) {
                ArtistListScreen(
                    viewModel = artistViewModel,
                    onBack = { navController.popBackStack() },
                    onArtistClick = { artistId ->
                        navController.navigate(Routes.artistDetail(artistId))
                    }
                )
            }

            composable(
                route = Routes.ARTIST_DETAIL,
                arguments = listOf(navArgument("artistId") { type = NavType.IntType })
            ) { entry ->
                val artistId = entry.arguments?.getInt("artistId") ?: return@composable
                ArtistDetailScreen(
                    artistId = artistId,
                    viewModel = artistViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.WANTED) {
                WantedScreen(
                    viewModel = wantedViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

