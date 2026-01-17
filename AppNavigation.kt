// AppNavigation.kt
// Create this file in: app/src/main/java/com/example/health/

package com.example.health

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp  // ADD THIS IMPORT - THIS WAS MISSING
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val isLoggedIn = UserManager.isLoggedIn

    if (isLoggedIn) {
        MainApp(navController)
    } else {
        AuthNavigation(navController)
    }
}

@Composable
fun AuthNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    // Login successful, trigger recomposition
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    // Registration successful, trigger recomposition
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(navController: NavHostController) {
    val mainNavController = rememberNavController()
    val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem("home", "Home", Icons.Default.Home),
        BottomNavItem("wellness", "Wellness", Icons.Default.Favorite),
        BottomNavItem("finance", "Finance", Icons.Default.AccountBalance),
        BottomNavItem("profile", "Profile", Icons.Default.Person)
    )

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar(currentRoute)) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                mainNavController.navigate(item.route) {
                                    popUpTo(mainNavController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(item.icon, contentDescription = item.label)
                            },
                            label = {
                                Text(
                                    item.label,
                                    fontWeight = if (currentRoute == item.route) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF6A1B9A),
                                selectedTextColor = Color(0xFF6A1B9A),
                                indicatorColor = Color(0xFF6A1B9A).copy(alpha = 0.1f),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = mainNavController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                HomeScreen(
                    onNavigateToFinance = {
                        mainNavController.navigate("finance")
                    }
                )
            }
            composable("wellness") {
                WellnessScreen(mainNavController)
            }
            composable("finance") {
                PersonalFinanceScreen()
            }
            composable("profile") {
                ProfileScreen(
                    onNavigateBack = {
                        mainNavController.popBackStack()
                    },
                    onLogout = {
                        // Logout will trigger recomposition and show login screen
                    }
                )
            }
            composable("mood_journey") {
                val moodHistory = remember { mutableStateListOf<MoodEntry>() }
                val weeklyProgress = remember { mutableStateOf(WeeklyProgress(45, 65, 20)) }
                MoodJourneyScreen(moodHistory, weeklyProgress)
            }
            composable("music") {
                MusicScreen("")
            }
            composable("sleep_help") {
                SleepHelpScreen()
            }
            composable("anxiety_check") {
                AnxietyCheckScreen()
            }
            composable("meditation") {
                QuickMeditationScreen()
            }
            composable("journal") {
                val journalEntries = remember { mutableStateListOf<JournalEntry>() }
                VoiceJournalScreen(journalEntries)
            }
        }
    }
}

fun shouldShowBottomBar(route: String?): Boolean {
    return route in listOf("home", "wellness", "finance", "profile")
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

// Data Classes for Wellness Features
data class MoodEntry(val date: String, val mood: String, val score: Int, val emoji: String)
data class JournalEntry(val date: String, val content: String, val audioNote: String? = null)
data class WeeklyProgress(val lastWeek: Int, val thisWeek: Int, val improvement: Int)