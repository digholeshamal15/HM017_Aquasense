// HabitTrackerScreen.kt
// CREATE THIS NEW FILE in: app/src/main/java/com/example/health/

package com.example.health

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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Habit(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val emoji: String,
    val color: Color,
    val targetDays: Int = 7,
    val completedDates: MutableList<String> = mutableListOf(),
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalCompletions: Int = 0
)

object HabitManager {
    val habits = mutableStateListOf(
        Habit(
            name = "Morning Exercise",
            emoji = "💪",
            color = Color(0xFFFF6B6B),
            completedDates = mutableListOf(),
            currentStreak = 3,
            longestStreak = 7,
            totalCompletions = 15
        ),
        Habit(
            name = "Meditation",
            emoji = "🧘",
            color = Color(0xFF9C27B0),
            completedDates = mutableListOf(),
            currentStreak = 5,
            longestStreak = 12,
            totalCompletions = 28
        ),
        Habit(
            name = "Read Books",
            emoji = "📚",
            color = Color(0xFF2196F3),
            completedDates = mutableListOf(),
            currentStreak = 2,
            longestStreak = 5,
            totalCompletions = 10
        ),
        Habit(
            name = "Drink Water",
            emoji = "💧",
            color = Color(0xFF03A9F4),
            completedDates = mutableListOf(),
            currentStreak = 7,
            longestStreak = 14,
            totalCompletions = 45
        ),
        Habit(
            name = "Healthy Eating",
            emoji = "🥗",
            color = Color(0xFF4CAF50),
            completedDates = mutableListOf(),
            currentStreak = 4,
            longestStreak = 8,
            totalCompletions = 20
        )
    )

    fun toggleHabitCompletion(habit: Habit, date: String) {
        val habitIndex = habits.indexOfFirst { it.id == habit.id }
        if (habitIndex != -1) {
            val updatedHabit = habits[habitIndex]
            if (updatedHabit.completedDates.contains(date)) {
                updatedHabit.completedDates.remove(date)
            } else {
                updatedHabit.completedDates.add(date)
            }

            // Update stats
            val newTotalCompletions = updatedHabit.completedDates.size
            val newCurrentStreak = calculateCurrentStreak(updatedHabit.completedDates)
            val newLongestStreak = maxOf(updatedHabit.longestStreak, newCurrentStreak)

            habits[habitIndex] = updatedHabit.copy(
                totalCompletions = newTotalCompletions,
                currentStreak = newCurrentStreak,
                longestStreak = newLongestStreak
            )
        }
    }

    private fun calculateCurrentStreak(dates: List<String>): Int {
        if (dates.isEmpty()) return 0

        val sortedDates = dates.sortedDescending()
        val today = LocalDate.now()
        var streak = 0
        var checkDate = today

        for (dateStr in sortedDates) {
            val date = LocalDate.parse(dateStr)
            if (date.isEqual(checkDate)) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else {
                break
            }
        }

        return streak
    }

    fun addHabit(habit: Habit) {
        habits.add(habit)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerScreen() {
    var showAddHabitDialog by remember { mutableStateOf(false) }
    val today = LocalDate.now()
    val last7Days = remember {
        (0..6).map { today.minusDays(it.toLong()) }.reversed()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFCE4EC),
                        Color(0xFFF3E5F5),
                        Color.White
                    )
                )
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "🔥 Habit Tracker",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6A1B9A)
                    )
                    Text(
                        "Build consistent daily habits",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                IconButton(
                    onClick = { showAddHabitDialog = true },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6A1B9A))
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Habit",
                        tint = Color.White
                    )
                }
            }
        }

        // Stats Overview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFF6B6B).copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatColumn(
                        "🔥",
                        "${HabitManager.habits.maxOfOrNull { it.currentStreak } ?: 0}",
                        "Best Streak",
                        Color(0xFFFF6B6B)
                    )
                    StatColumn(
                        "✅",
                        "${HabitManager.habits.sumOf { it.totalCompletions }}",
                        "Total Done",
                        Color(0xFF4CAF50)
                    )
                    StatColumn(
                        "📊",
                        "${HabitManager.habits.size}",
                        "Active Habits",
                        Color(0xFF2196F3)
                    )
                }
            }
        }

        // Week Calendar Header
        item {
            Text(
                "This Week",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6A1B9A)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    last7Days.forEach { date ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                date.dayOfWeek.toString().take(3),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (date == today) Color(0xFF6A1B9A) else Color.Gray
                            )
                            Text(
                                date.dayOfMonth.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (date == today) FontWeight.Bold else FontWeight.Normal,
                                color = if (date == today) Color(0xFF6A1B9A) else Color.Black
                            )
                        }
                    }
                }
            }
        }

        // Habits List
        item {
            Text(
                "Your Habits",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6A1B9A)
            )
        }

        items(HabitManager.habits) { habit ->
            HabitCard(habit, last7Days)
        }

        if (HabitManager.habits.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF6A1B9A).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔥", style = MaterialTheme.typography.displayLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No habits yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Start building your daily habits",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Tips
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF6A1B9A).copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "💡 Habit Building Tips",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF6A1B9A)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf(
                        "Start with small, achievable habits",
                        "Be consistent - aim for daily completion",
                        "Track your progress to stay motivated",
                        "Celebrate your streaks!",
                        "Don't break the chain - keep going!"
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

    // Add Habit Dialog
    if (showAddHabitDialog) {
        AddHabitDialog(
            onDismiss = { showAddHabitDialog = false },
            onAdd = { habit ->
                HabitManager.addHabit(habit)
                showAddHabitDialog = false
            }
        )
    }
}

@Composable
fun HabitCard(habit: Habit, last7Days: List<LocalDate>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = habit.color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Habit Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        habit.emoji,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            habit.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = habit.color
                        )
                        Text(
                            "🔥 ${habit.currentStreak} day streak",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFF6B6B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Week Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                last7Days.forEach { date ->
                    val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val isCompleted = habit.completedDates.contains(dateStr)

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCompleted) habit.color
                                else habit.color.copy(alpha = 0.2f)
                            )
                            .clickable {
                                HabitManager.toggleHabitCompletion(habit, dateStr)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HabitStat("Current", "${habit.currentStreak} days")
                HabitStat("Best", "${habit.longestStreak} days")
                HabitStat("Total", "${habit.totalCompletions} times")
            }
        }
    }
}

@Composable
fun HabitStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun StatColumn(emoji: String, value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onAdd: (Habit) -> Unit
) {
    var habitName by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("💪") }
    var selectedColor by remember { mutableStateOf(Color(0xFFFF6B6B)) }

    val emojis = listOf("💪", "🧘", "📚", "💧", "🥗", "🏃", "🎯", "✍️", "🎨", "🎵")
    val colors = listOf(
        Color(0xFFFF6B6B),
        Color(0xFF4ECDC4),
        Color(0xFF95E1D3),
        Color(0xFFFFA07A),
        Color(0xFF9C27B0),
        Color(0xFF2196F3),
        Color(0xFF4CAF50),
        Color(0xFFFF9800)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Create New Habit",
                color = Color(0xFF6A1B9A),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = habitName,
                    onValueChange = { habitName = it },
                    label = { Text("Habit Name") },
                    placeholder = { Text("e.g., Morning Exercise") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6A1B9A),
                        focusedLabelColor = Color(0xFF6A1B9A),
                        cursorColor = Color(0xFF6A1B9A)
                    )
                )

                Column {
                    Text(
                        "Choose Emoji",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(emojis) { emoji ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (selectedEmoji == emoji)
                                            Color(0xFF6A1B9A).copy(alpha = 0.2f)
                                        else Color.Transparent
                                    )
                                    .border(
                                        2.dp,
                                        if (selectedEmoji == emoji)
                                            Color(0xFF6A1B9A)
                                        else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable { selectedEmoji = emoji },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, style = MaterialTheme.typography.headlineMedium)
                            }
                        }
                    }
                }

                Column {
                    Text(
                        "Choose Color",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(colors) { color ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        3.dp,
                                        if (selectedColor == color) Color.White else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable { selectedColor = color }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (habitName.isNotBlank()) {
                        onAdd(
                            Habit(
                                name = habitName,
                                emoji = selectedEmoji,
                                color = selectedColor
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6A1B9A)
                ),
                enabled = habitName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF6A1B9A))
            }
        }
    )
}
