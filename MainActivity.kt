// MainActivity.kt
// REPLACE your existing MainActivity.kt with this COMPLETE version
// Location: app/src/main/java/com/example/health/MainActivity.kt

package com.example.health

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val colorScheme = lightColorScheme(
        primary = Color(0xFF6A1B9A),        // Purple from screenshot
        secondary = Color(0xFF8E24AA),      // Light Purple
        tertiary = Color(0xFF4E4AF6),       // Blue accent
        background = Color.White,
        surface = Color.White,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = Color(0xFF1C1B1F),
        onSurface = Color(0xFF1C1B1F),
        primaryContainer = Color(0xFFF3E5F5),
        secondaryContainer = Color(0xFFE1BEE7)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

// Mood Journey Screen
@Composable
fun MoodJourneyScreen(
    moodHistory: MutableList<MoodEntry>,
    weeklyProgress: MutableState<WeeklyProgress>
) {
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
                            moodHistory.add(0, MoodEntry(today, moodName, newMoodScore, selectedMoodEmoji))

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

// Music Screen
@Composable
fun MusicScreen(currentMood: String) {
    val playlists = listOf(
        Triple("😊 Happy Vibes", "Uplifting songs to boost your mood", Color(0xFF4CAF50)),
        Triple("😢 Melancholic", "When you need to feel understood", Color(0xFF2196F3)),
        Triple("😴 Sleep Sounds", "Peaceful music for better sleep", Color(0xFF6A1B9A)),
        Triple("💪 Motivational", "Pump up your energy", Color(0xFFFF6F00)),
        Triple("🧘 Meditation", "Calm and peaceful", Color(0xFF9C27B0)),
        Triple("🎯 Focus", "Concentration music", Color(0xFFE91E63))
    )

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
                "🎵 Music Therapy",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6A1B9A)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(playlists) { (title, description, color) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { },
                elevation = CardDefaults.cardElevation(0.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = color.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            title.split(" ")[0],
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            title.substringAfter(" "),
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                        Text(
                            description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        modifier = Modifier.size(32.dp),
                        tint = color
                    )
                }
            }
        }
    }
}

// Sleep Helper Screen
@Composable
fun SleepHelpScreen() {
    var sleepHours by remember { mutableStateOf(7f) }

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
                "😴 Sleep Helper",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6A1B9A)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE3F2FD)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        "💤 Sleep Quality Check",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("How many hours did you sleep last night?")

                    Spacer(modifier = Modifier.height(12.dp))

                    Slider(
                        value = sleepHours,
                        onValueChange = { sleepHours = it },
                        valueRange = 4f..12f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF2196F3),
                            activeTrackColor = Color(0xFF2196F3)
                        )
                    )
                    Text(
                        "${sleepHours.toInt()} hours",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val sleepQuality = when {
                        sleepHours >= 8 -> "Great! You're getting adequate sleep!"
                        sleepHours >= 6 -> "Good, but try for 7-9 hours"
                        else -> "Consider improving your sleep habits"
                    }

                    Text(
                        sleepQuality,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = when {
                            sleepHours >= 8 -> Color(0xFF4CAF50)
                            sleepHours >= 6 -> Color(0xFFFF9800)
                            else -> Color(0xFFF44336)
                        }
                    )
                }
            }
        }
    }
}

// Add these functions to the SAME MainActivity.kt file (continuation)

// Anxiety Check Screen
@Composable
fun AnxietyCheckScreen() {
    var currentQuestion by remember { mutableStateOf(0) }
    var answers by remember { mutableStateOf(mutableListOf<Int>()) }
    var showResults by remember { mutableStateOf(false) }

    val anxietyQuestions = listOf(
        "How often have you felt nervous, anxious, or on edge?",
        "How often have you not been able to stop or control worrying?",
        "How often have you been worrying too much about different things?",
        "How often have you had trouble relaxing?",
        "How often have you been so restless that it's hard to sit still?",
        "How often have you become easily annoyed or irritable?",
        "How often have you felt afraid as if something awful might happen?"
    )

    val answerOptions = listOf(
        "Not at all" to 0,
        "Several days" to 1,
        "More than half the days" to 2,
        "Nearly every day" to 3
    )

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
                "🧠 Anxiety Check (GAD-7)",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6A1B9A)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Over the last 2 weeks, how often have you been bothered by any of the following problems?",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (!showResults) {
            item {
                LinearProgressIndicator(
                    progress = (currentQuestion + 1) / anxietyQuestions.size.toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF6A1B9A)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Question ${currentQuestion + 1} of ${anxietyQuestions.size}")
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF6A1B9A).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        anxietyQuestions[currentQuestion],
                        modifier = Modifier.padding(24.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            items(answerOptions) { (option, score) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable {
                            answers.add(score)
                            if (currentQuestion < anxietyQuestions.size - 1) {
                                currentQuestion++
                            } else {
                                showResults = true
                            }
                        },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        option,
                        modifier = Modifier.padding(20.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            item {
                val totalScore = answers.sum()
                val anxietyLevel = when {
                    totalScore <= 4 -> Triple("Minimal", Color(0xFF4CAF50), "Low anxiety levels")
                    totalScore <= 9 -> Triple("Mild", Color(0xFFFF9800), "Mild anxiety symptoms")
                    totalScore <= 14 -> Triple("Moderate", Color(0xFFFF5722), "Moderate anxiety")
                    else -> Triple("Severe", Color(0xFFF44336), "Severe anxiety - seek help")
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = anxietyLevel.second.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Your Results",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Score: $totalScore/21",
                            style = MaterialTheme.typography.displaySmall,
                            color = anxietyLevel.second,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${anxietyLevel.first} Anxiety",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = anxietyLevel.second
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            anxietyLevel.third,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        currentQuestion = 0
                        answers.clear()
                        showResults = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6A1B9A)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Retake Assessment", modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}

// Quick Meditation Screen
@Composable
fun QuickMeditationScreen() {
    var selectedMeditation by remember { mutableStateOf("") }
    var isPlaying by remember { mutableStateOf(false) }
    var timeRemaining by remember { mutableStateOf(0) }

    val meditations = listOf(
        Triple("Breathing", "4-7-8 Breathing technique", 60),
        Triple("Body Scan", "Progressive relaxation", 300),
        Triple("Mindfulness", "Present moment awareness", 180),
        Triple("Loving Kindness", "Compassion meditation", 240),
        Triple("Focus", "Concentration practice", 120)
    )

    LaunchedEffect(isPlaying) {
        if (isPlaying && timeRemaining > 0) {
            delay(1000)
            timeRemaining--
            if (timeRemaining <= 0) {
                isPlaying = false
            }
        }
    }

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
                "🧘 Quick Meditation",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6A1B9A)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (isPlaying) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF6A1B9A).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            selectedMeditation,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            "${timeRemaining / 60}:${String.format("%02d", timeRemaining % 60)}",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6A1B9A)
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        val breathingPhase = when (timeRemaining % 19) {
                            in 0..3 -> "Breathe In..."
                            in 4..10 -> "Hold..."
                            in 11..18 -> "Breathe Out..."
                            else -> "Relax..."
                        }

                        Text(
                            breathingPhase,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { isPlaying = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6A1B9A)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Stop", modifier = Modifier.padding(horizontal = 24.dp))
                        }
                    }
                }
            }
        } else {
            items(meditations) { (title, description, duration) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable {
                            selectedMeditation = title
                            timeRemaining = duration
                            isPlaying = true
                        },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Text(
                            "${duration / 60}:${String.format("%02d", duration % 60)}",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6A1B9A)
                        )
                    }
                }
            }
        }
    }
}

// Voice Journal Screen
@Composable
fun VoiceJournalScreen(journalEntries: MutableList<JournalEntry>) {
    var currentEntry by remember { mutableStateOf("") }

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
                    journalEntries.add(0, JournalEntry(today, currentEntry))
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
