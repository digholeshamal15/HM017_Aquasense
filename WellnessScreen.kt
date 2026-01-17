// WellnessScreen.kt - FIXED VERSION (No duplicate data classes)
package com.example.health

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

// NOTE: Data classes (MoodEntry, JournalEntry, WeeklyProgress) are in DataModels.kt

// FIXED: Mood Manager with correct scoring
object MoodManager {
    val moods = mutableStateListOf<MoodEntry>()

    // FIXED: Correct mood to score mapping
    private val moodScores = mapOf(
        "😭" to 1,  // Very Sad
        "😟" to 2,  // Sad
        "😐" to 3,  // Neutral
        "🙂" to 4,  // Good
        "😊" to 5   // Great
    )

    fun getMoodScore(emoji: String): Int {
        return moodScores[emoji] ?: 3
    }

    fun addMood(mood: MoodEntry) {
        moods.add(0, mood)
    }

    fun getAverageMoodThisWeek(): Double {
        val today = LocalDate.now()
        val weekAgo = today.minusDays(7)

        val thisWeekMoods = moods.filter {
            val moodDate = LocalDate.parse(it.date)
            moodDate.isAfter(weekAgo) && moodDate.isBefore(today.plusDays(1))
        }

        return if (thisWeekMoods.isNotEmpty()) {
            thisWeekMoods.map { it.score }.average()
        } else 0.0
    }
}

object JournalManager {
    val journals = mutableStateListOf<JournalEntry>()

    fun addJournal(journal: JournalEntry) {
        journals.add(0, journal)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WellnessScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Mood Tracker", "Daily Journal", "Meditation")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE1F5FE),
                        Color(0xFFF3E5F5),
                        Color.White
                    )
                )
            )
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1976D2).copy(alpha = 0.1f),
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    "🧘 Wellness",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
                Text(
                    "Track your mental wellbeing",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color(0xFF1976D2)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // Content
        when (selectedTab) {
            0 -> MoodTrackerTab()
            1 -> DailyJournalTab()
            2 -> MeditationTab()
        }
    }
}

@Composable
fun MoodTrackerTab() {
    var showMoodDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Load moods from Firebase
    LaunchedEffect(Unit) {
        val moods = FirebaseUserManager.getMoodHistory()
        MoodManager.moods.clear()
        MoodManager.moods.addAll(moods)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Add Mood Button
        item {
            Button(
                onClick = { showMoodDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1976D2)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Your Mood", style = MaterialTheme.typography.titleMedium)
            }
        }

        // Weekly Stats
        item {
            val avgMood = MoodManager.getAverageMoodThisWeek()
            val moodText = when {
                avgMood >= 4.5 -> "Excellent"
                avgMood >= 3.5 -> "Good"
                avgMood >= 2.5 -> "Fair"
                avgMood >= 1.5 -> "Struggling"
                else -> "No data"
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "This Week's Average",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        String.format("%.1f/5", avgMood),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        moodText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            }
        }

        // Mood History
        item {
            Text(
                "Mood History",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
        }

        if (MoodManager.moods.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1976D2).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("😊", style = MaterialTheme.typography.displayLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No mood entries yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Start tracking your mood daily",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(MoodManager.moods) { mood ->
                MoodCard(mood)
            }
        }
    }

    // Mood Dialog
    if (showMoodDialog) {
        AddMoodDialog(
            onDismiss = { showMoodDialog = false },
            onSave = { mood ->
                MoodManager.addMood(mood)

                // Save to Firebase
                scope.launch {
                    FirebaseUserManager.saveMoodEntry(mood)
                }

                showMoodDialog = false
            }
        )
    }
}

@Composable
fun MoodCard(mood: MoodEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (mood.score) {
                5 -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                4 -> Color(0xFF8BC34A).copy(alpha = 0.1f)
                3 -> Color(0xFFFFC107).copy(alpha = 0.1f)
                2 -> Color(0xFFFF9800).copy(alpha = 0.1f)
                else -> Color(0xFFFF5252).copy(alpha = 0.1f)
            }
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    mood.emoji,
                    style = MaterialTheme.typography.displaySmall
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        mood.mood,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        mood.date,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
            Text(
                "${mood.score}/5",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = when (mood.score) {
                    5 -> Color(0xFF4CAF50)
                    4 -> Color(0xFF8BC34A)
                    3 -> Color(0xFFFFC107)
                    2 -> Color(0xFFFF9800)
                    else -> Color(0xFFFF5252)
                }
            )
        }
    }
}

@Composable
fun AddMoodDialog(
    onDismiss: () -> Unit,
    onSave: (MoodEntry) -> Unit
) {
    var selectedMood by remember { mutableStateOf<Pair<String, String>?>(null) }

    // FIXED: Correct mood options with proper scoring
    val moods = listOf(
        "😭" to "Very Sad",
        "😟" to "Sad",
        "😐" to "Neutral",
        "🙂" to "Good",
        "😊" to "Great"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "How are you feeling?",
                color = Color(0xFF1976D2),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                moods.forEach { (emoji, mood) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedMood = emoji to mood },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedMood?.first == emoji)
                                Color(0xFF1976D2).copy(alpha = 0.2f)
                            else Color.Transparent
                        ),
                        border = BorderStroke(
                            2.dp,
                            if (selectedMood?.first == emoji)
                                Color(0xFF1976D2)
                            else Color.Gray.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(emoji, style = MaterialTheme.typography.headlineMedium)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                mood,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (selectedMood?.first == emoji)
                                    FontWeight.Bold
                                else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedMood?.let { (emoji, mood) ->
                        val score = MoodManager.getMoodScore(emoji)
                        onSave(
                            MoodEntry(
                                date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                                mood = mood,
                                score = score,
                                emoji = emoji
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1976D2)
                ),
                enabled = selectedMood != null
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF1976D2))
            }
        }
    )
}

@Composable
fun DailyJournalTab() {
    var showJournalDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Load journals from Firebase
    LaunchedEffect(Unit) {
        val journals = FirebaseUserManager.getJournalEntries()
        JournalManager.journals.clear()
        JournalManager.journals.addAll(journals)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Add Journal Button
        item {
            Button(
                onClick = { showJournalDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9C27B0)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Journal Entry", style = MaterialTheme.typography.titleMedium)
            }
        }

        // Journal Entries
        item {
            Text(
                "Your Journals",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9C27B0)
            )
        }

        if (JournalManager.journals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF9C27B0).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📔", style = MaterialTheme.typography.displayLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No journal entries yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Start writing your thoughts",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(JournalManager.journals) { journal ->
                JournalCard(journal)
            }
        }
    }

    // Journal Dialog
    if (showJournalDialog) {
        AddJournalDialog(
            onDismiss = { showJournalDialog = false },
            onSave = { journal ->
                JournalManager.addJournal(journal)

                // Save to Firebase
                scope.launch {
                    FirebaseUserManager.saveJournalEntry(journal)
                }

                showJournalDialog = false
            }
        )
    }
}

@Composable
fun JournalCard(journal: JournalEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF9C27B0).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    journal.date,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9C27B0)
                )
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    tint = Color(0xFF9C27B0)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                journal.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3
            )
        }
    }
}

@Composable
fun AddJournalDialog(
    onDismiss: () -> Unit,
    onSave: (JournalEntry) -> Unit
) {
    var journalText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "New Journal Entry",
                color = Color(0xFF9C27B0),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            OutlinedTextField(
                value = journalText,
                onValueChange = { journalText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                placeholder = { Text("Write your thoughts...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF9C27B0),
                    focusedLabelColor = Color(0xFF9C27B0),
                    cursorColor = Color(0xFF9C27B0)
                ),
                maxLines = 10
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (journalText.isNotBlank()) {
                        onSave(
                            JournalEntry(
                                date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                                content = journalText
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9C27B0)
                ),
                enabled = journalText.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF9C27B0))
            }
        }
    )
}

@Composable
fun MeditationTab() {
    var selectedDuration by remember { mutableStateOf(5) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var timeRemaining by remember { mutableStateOf(selectedDuration * 60) }

    // FIXED: Meditation timer logic
    LaunchedEffect(isTimerRunning, timeRemaining) {
        if (isTimerRunning && timeRemaining > 0) {
            kotlinx.coroutines.delay(1000)
            timeRemaining--
        } else if (timeRemaining == 0) {
            isTimerRunning = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Timer Display
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF00BCD4).copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "🧘",
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Meditation Timer",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00BCD4)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Time Display
                val minutes = timeRemaining / 60
                val seconds = timeRemaining % 60
                Text(
                    String.format("%02d:%02d", minutes, seconds),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00BCD4)
                )
            }
        }

        // Duration Selection
        if (!isTimerRunning) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        "Select Duration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf(5, 10, 15, 20).forEach { duration ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (selectedDuration == duration)
                                            Color(0xFF00BCD4)
                                        else Color(0xFF00BCD4).copy(alpha = 0.1f)
                                    )
                                    .clickable {
                                        selectedDuration = duration
                                        timeRemaining = duration * 60
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${duration}m",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedDuration == duration)
                                        Color.White
                                    else Color(0xFF00BCD4)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Control Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!isTimerRunning) {
                Button(
                    onClick = {
                        isTimerRunning = true
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start", style = MaterialTheme.typography.titleMedium)
                }
            } else {
                Button(
                    onClick = {
                        isTimerRunning = false
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFC107)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pause", style = MaterialTheme.typography.titleMedium)
                }
            }

            Button(
                onClick = {
                    isTimerRunning = false
                    timeRemaining = selectedDuration * 60
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF5252)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset", style = MaterialTheme.typography.titleMedium)
            }
        }

        // Tips
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF00BCD4).copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "💡 Meditation Tips",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF00BCD4)
                )
                Spacer(modifier = Modifier.height(8.dp))
                listOf(
                    "Find a quiet, comfortable place",
                    "Sit with your back straight",
                    "Focus on your breathing",
                    "Let thoughts pass without judgment",
                    "Start with shorter sessions"
                ).forEach { tip ->
                    Text(
                        "• $tip",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}
