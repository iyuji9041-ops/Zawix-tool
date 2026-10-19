package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesManager
import com.example.data.local.ThemeMode
import com.example.data.repository.NotesRepository
import com.example.data.repository.ToolsRepository
import com.example.domain.ads.AdMobManager
import com.example.ui.screens.calculator.CalculatorScreen
import com.example.ui.screens.calculator.PercentageScreen
import com.example.ui.screens.calculator.UnitConverterScreen
import com.example.ui.screens.daily.DailyUtilitiesScreen
import com.example.ui.screens.datetime.DateTimeScreen
import com.example.ui.screens.favorites.FavoritesScreen
import com.example.ui.screens.files.FileToolsScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.image.ImageToolsScreen
import com.example.ui.screens.notes.NotesScreen
import com.example.ui.screens.password.PasswordGeneratorScreen
import com.example.ui.screens.qr.QrToolScreen
import com.example.ui.screens.recent.RecentScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.text.TextToolkitScreen
import com.example.ui.theme.ZaswixTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    private lateinit var adMobManager: AdMobManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(this)
        val toolsRepository = ToolsRepository(database.toolUsageDao())
        val notesRepository = NotesRepository(database.noteDao())
        val preferencesManager = PreferencesManager(this)

        // Read saved theme before first composition to eliminate theme flash on startup
        val initialSavedTheme = runBlocking {
            try {
                preferencesManager.themeModeFlow.first()
            } catch (e: Exception) {
                ThemeMode.SYSTEM
            }
        }

        // Initialize Google UMP Consent flow before loading ads
        adMobManager = AdMobManager(this)
        adMobManager.requestConsentAndInitialize(this)

        setContent {
            val themeMode by preferencesManager.themeModeFlow.collectAsState(initial = initialSavedTheme)
            val isFirstLaunchCompleted by preferencesManager.isFirstLaunchCompleted.collectAsState(initial = true)

            val isDark = when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            ZaswixTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ZaswixNavApp(
                        activity = this@MainActivity,
                        adMobManager = adMobManager,
                        toolsRepository = toolsRepository,
                        notesRepository = notesRepository,
                        preferencesManager = preferencesManager,
                        isFirstLaunch = !isFirstLaunchCompleted,
                        currentTheme = themeMode
                    )
                }
            }
        }
    }
}

@Composable
fun ZaswixNavApp(
    activity: Activity,
    adMobManager: AdMobManager,
    toolsRepository: ToolsRepository,
    notesRepository: NotesRepository,
    preferencesManager: PreferencesManager,
    isFirstLaunch: Boolean,
    currentTheme: ThemeMode
) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    // Tracks total tool launches across session to trigger interstitial after every 4th tool open
    var toolOpenCount by rememberSaveable { mutableIntStateOf(0) }

    val allTools by toolsRepository.allToolsFlow.collectAsState(initial = emptyList())
    val favoriteTools by toolsRepository.favoriteToolsFlow.collectAsState(initial = emptyList())
    val recentTools by toolsRepository.recentToolsFlow.collectAsState(initial = emptyList())

    fun handleToolLaunch(toolRoute: String, toolId: String) {
        coroutineScope.launch {
            toolsRepository.recordToolUsage(toolId)
        }
        val currentCount = toolOpenCount + 1
        toolOpenCount = currentCount

        // Show interstitial ad after every 4th tool open (not on first launch)
        if (currentCount % 4 == 0) {
            adMobManager.showInterstitialIfReady(activity) {
                navController.navigate(toolRoute) { launchSingleTop = true }
            }
        } else {
            navController.navigate(toolRoute) { launchSingleTop = true }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                allTools = allTools,
                favoriteTools = favoriteTools,
                recentTools = recentTools,
                adsEnabled = true,
                isFirstLaunch = isFirstLaunch,
                onDismissOnboarding = {
                    coroutineScope.launch {
                        preferencesManager.setFirstLaunchCompleted(true)
                    }
                },
                onToolClick = { tool ->
                    handleToolLaunch(tool.route, tool.id)
                },
                onFavoriteToggle = { toolId ->
                    coroutineScope.launch {
                        toolsRepository.toggleFavorite(toolId)
                    }
                },
                onNavigateFavorites = {
                    navController.navigate("favorites") { launchSingleTop = true }
                },
                onNavigateRecent = {
                    navController.navigate("recent") { launchSingleTop = true }
                },
                onNavigateSettings = {
                    navController.navigate("settings") { launchSingleTop = true }
                }
            )
        }

        composable("favorites") {
            FavoritesScreen(
                favoriteTools = favoriteTools,
                onToolClick = { tool ->
                    handleToolLaunch(tool.route, tool.id)
                },
                onFavoriteToggle = { toolId ->
                    coroutineScope.launch {
                        toolsRepository.toggleFavorite(toolId)
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("recent") {
            RecentScreen(
                recentTools = recentTools,
                onToolClick = { tool ->
                    handleToolLaunch(tool.route, tool.id)
                },
                onFavoriteToggle = { toolId ->
                    coroutineScope.launch {
                        toolsRepository.toggleFavorite(toolId)
                    }
                },
                onClearHistory = {
                    coroutineScope.launch {
                        toolsRepository.clearRecents()
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreen(
                currentTheme = currentTheme,
                onThemeChange = { mode ->
                    coroutineScope.launch { preferencesManager.setThemeMode(mode) }
                },
                onClearRecents = {
                    coroutineScope.launch { toolsRepository.clearRecents() }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // TOOL SCREENS with launchSingleTop navigation
        composable("tool_calc") {
            CalculatorScreen(onBackClick = { navController.popBackStack() })
        }

        composable("tool_percentage") {
            PercentageScreen(onBackClick = { navController.popBackStack() })
        }

        composable("tool_converter") {
            UnitConverterScreen(onBackClick = { navController.popBackStack() })
        }

        composable("tool_text") {
            TextToolkitScreen(onBackClick = { navController.popBackStack() })
        }

        composable("tool_notes") {
            NotesScreen(
                notesRepository = notesRepository,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("tool_qr") {
            QrToolScreen(onBackClick = { navController.popBackStack() })
        }

        composable("tool_image") {
            ImageToolsScreen(onBackClick = { navController.popBackStack() })
        }

        composable("tool_files") {
            FileToolsScreen(onBackClick = { navController.popBackStack() })
        }

        composable("tool_password") {
            PasswordGeneratorScreen(onBackClick = { navController.popBackStack() })
        }

        composable("tool_datetime") {
            DateTimeScreen(onBackClick = { navController.popBackStack() })
        }

        composable("tool_daily") {
            DailyUtilitiesScreen(onBackClick = { navController.popBackStack() })
        }
    }
}
