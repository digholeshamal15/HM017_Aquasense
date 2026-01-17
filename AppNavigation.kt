// AppNavigation.kt - UPDATED FOR FIREBASE
// REPLACE the AppNavigation function

package com.example.health


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val isLoggedIn = FirebaseUserManager.isLoggedIn  // Changed from UserManager

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
    val scope = rememberCoroutineScope()

    // Load user data from Firebase when app starts
    LaunchedEffect(Unit) {
        scope.launch {
            // Load mood history
            val moods = FirebaseUserManager.getMoodHistory()
            // Load journal entries
            val journals = FirebaseUserManager.getJournalEntries()
            // Load transactions
            val transactions = FirebaseUserManager.getTransactions()
            // Load budgets
            val budgets = FirebaseUserManager.getBudgets()
            // Load savings goals
            val goals = FirebaseUserManager.getSavingsGoals()
            // Load habits
            val habits = FirebaseUserManager.getHabits()

            // Update local managers with Firebase data
            // (You can create a DataSyncManager for this)
        }
    }

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

                // Load mood data from Firebase
                LaunchedEffect(Unit) {
                    val moods = FirebaseUserManager.getMoodHistory()
                    moodHistory.clear()
                    moodHistory.addAll(moods)
                }

                MoodJourneyScreenWithFirebase(moodHistory, weeklyProgress)
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

                // Load journal data from Firebase
                LaunchedEffect(Unit) {
                    val journals = FirebaseUserManager.getJournalEntries()
                    journalEntries.clear()
                    journalEntries.addAll(journals)
                }

                VoiceJournalScreenWithFirebase(journalEntries)
            }
            composable("habit_tracker") {
                HabitTrackerScreenWithFirebase()
            }
        }
    }
}

// Modified screens that save to Firebase
@Composable
fun MoodJourneyScreenWithFirebase(
    moodHistory: MutableList<MoodEntry>,
    weeklyProgress: MutableState<WeeklyProgress>
) {
    val scope = rememberCoroutineScope()
    var newMoodScore by remember { mutableStateOf(5) }
    var selectedMoodEmoji by remember { mutableStateOf("😐") }

    val moodEmojis = listOf("😢", "😔", "😐", "🙂", "😊", "😄", "🤩", "💪", "⚡", "🌟")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF3E5F5),
                        Color.White
                    )
                )
            )
            .padding(16.dp)
    ) {
        item {
            Text(
                "🎯 Mood Journey",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6A1B9A)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(0.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        "How are you feeling right now?",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(moodEmojis) { emoji ->
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (selectedMoodEmoji == emoji)
                                            Color(0xFF6A1B9A).copy(alpha = 0.2f)
                                        else Color.Transparent
                                    )
                                    .border(
                                        2.dp,
                                        if (selectedMoodEmoji == emoji)
                                            Color(0xFF6A1B9A)
                                        else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable { selectedMoodEmoji = emoji },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, style = MaterialTheme.typography.headlineMedium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        "Intensity: $newMoodScore/10",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = newMoodScore.toFloat(),
                        onValueChange = { newMoodScore = it.toInt() },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF6A1B9A),
                            activeTrackColor = Color(0xFF6A1B9A),
                            inactiveTrackColor = Color(0xFF6A1B9A).copy(alpha = 0.3f)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            val moodName = when (selectedMoodEmoji) {
                                "😢", "😔" -> "Sad"
                                "😐" -> "Neutral"
                                "🙂", "😊" -> "Happy"
                                "😄", "🤩" -> "Joyful"
                                "💪", "⚡", "🌟" -> "Energetic"
                                else -> "Neutral"
                            }
                            val newEntry = MoodEntry(today, moodName, newMoodScore, selectedMoodEmoji)
                            moodHistory.add(0, newEntry)

                            // Save to Firebase
                            scope.launch {
                                FirebaseUserManager.saveMoodEntry(newEntry)
                            }

                            val avgScore = moodHistory.take(7).map { it.score }.average().toInt()
                            weeklyProgress.value = weeklyProgress.value.copy(thisWeek = avgScore)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6A1B9A)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Log Mood", modifier = Modifier.padding(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Text(
                "Recent Mood History",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF6A1B9A)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(moodHistory.take(10)) { mood ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF3E5F5)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(mood.emoji, style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(mood.mood, fontWeight = FontWeight.Bold)
                            Text(mood.date, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Text(
                        "${mood.score}/10",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = when {
                            mood.score >= 8 -> Color(0xFF4CAF50)
                            mood.score >= 6 -> Color(0xFFFF9800)
                            else -> Color(0xFFF44336)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun VoiceJournalScreenWithFirebase(journalEntries: MutableList<JournalEntry>) {
    var currentEntry by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF3E5F5),
                        Color.White
                    )
                )
            )
            .padding(16.dp)
    ) {
        item {
            Text(
                "📓 Daily Journal",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6A1B9A)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            OutlinedTextField(
                value = currentEntry,
                onValueChange = { currentEntry = it },
                label = { Text("Write your thoughts...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 5,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6A1B9A),
                    focusedLabelColor = Color(0xFF6A1B9A),
                    cursorColor = Color(0xFF6A1B9A)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                    val newEntry = JournalEntry(today, currentEntry)
                    journalEntries.add(0, newEntry)

                    // Save to Firebase
                    scope.launch {
                        FirebaseUserManager.saveJournalEntry(newEntry)
                    }

                    currentEntry = ""
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = currentEntry.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6A1B9A)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Entry", modifier = Modifier.padding(8.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Your Journal Entries",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF6A1B9A)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(journalEntries) { entry ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF3E5F5)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        entry.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6A1B9A),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(entry.content)
                }
            }
        }
    }
}

@Composable
fun HabitTrackerScreenWithFirebase() {
    val scope = rememberCoroutineScope()

    // Load habits from Firebase
    LaunchedEffect(Unit) {
        val habits = FirebaseUserManager.getHabits()
        HabitManager.habits.clear()
        HabitManager.habits.addAll(habits)
    }

    // Use existing HabitTrackerScreen but save to Firebase on changes
    HabitTrackerScreen()

    // Save habits whenever they change
    LaunchedEffect(HabitManager.habits.toList()) {
        HabitManager.habits.forEach { habit ->
            FirebaseUserManager.saveHabit(habit)
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
