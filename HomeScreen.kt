// HomeScreen.kt - FIXED VERSION
// REPLACE your existing HomeScreen.kt with this COMPLETE version
// Location: app/src/main/java/com/example/health/HomeScreen.kt

package com.example.health

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    // Get current username
    val currentUsername = FirebaseUserManager.currentUser?.name ?: "User"

    // FIXED: Safe date parsing for moods
    val moodEntriesThisWeek = remember {
        derivedStateOf {
            val today = LocalDate.now()
            val weekAgo = today.minusDays(7)
            MoodManager.moods.count {
                try {
                    val moodDate = LocalDate.parse(it.date)
                    moodDate.isAfter(weekAgo) && moodDate.isBefore(today.plusDays(1))
                } catch (e: Exception) {
                    false // Skip invalid dates
                }
            }
        }
    }

    // FIXED: Safe date parsing for journals (handles both date and datetime formats)
    val journalEntriesThisWeek = remember {
        derivedStateOf {
            val today = LocalDate.now()
            val weekAgo = today.minusDays(7)
            JournalManager.journals.count {
                try {
                    // Try to extract just the date part (first 10 characters: YYYY-MM-DD)
                    val dateStr = it.date.take(10)
                    val journalDate = LocalDate.parse(dateStr)
                    journalDate.isAfter(weekAgo) && journalDate.isBefore(today.plusDays(1))
                } catch (e: Exception) {
                    false // Skip invalid dates
                }
            }
        }
    }

    // FIXED: Safe today check for habits
    val habitsCompletedToday = remember {
        derivedStateOf {
            val today = LocalDate.now().toString()
            HabitManager.habits.count {
                it.completedDates.contains(today)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD),
                        Color(0xFFF3E5F5),
                        Color.White
                    )
                )
            )
    ) {
        // Header with Username
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1976D2).copy(alpha = 0.1f),
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Welcome Back,",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                        Text(
                            currentUsername,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1976D2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            currentUsername.firstOrNull()?.uppercase() ?: "U",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Quick Stats - FIXED: Live Updates
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Your Week at a Glance",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickStatCard(
                        icon = "😊",
                        value = "${moodEntriesThisWeek.value}",
                        label = "Mood Logs",
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                    QuickStatCard(
                        icon = "📔",
                        value = "${journalEntriesThisWeek.value}",
                        label = "Journals",
                        color = Color(0xFF9C27B0),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickStatCard(
                        icon = "🔥",
                        value = "${habitsCompletedToday.value}/${HabitManager.habits.size}",
                        label = "Habits Today",
                        color = Color(0xFFFF6B6B),
                        modifier = Modifier.weight(1f)
                    )
                    QuickStatCard(
                        icon = "💰",
                        value = "₹${String.format("%,.0f", FinanceManager.currentBalance.value)}",
                        label = "Balance",
                        color = Color(0xFF2196F3),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Today's Progress
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Today's Progress",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ProgressItem(
                            icon = "🔥",
                            title = "Habits Completed",
                            current = habitsCompletedToday.value,
                            total = HabitManager.habits.size,
                            color = Color(0xFFFF6B6B)
                        )
                        ProgressItem(
                            icon = "😊",
                            title = "Mood Logged",
                            current = if (MoodManager.moods.firstOrNull()?.date == LocalDate.now().toString()) 1 else 0,
                            total = 1,
                            color = Color(0xFF4CAF50)
                        )
                        ProgressItem(
                            icon = "📔",
                            title = "Journal Written",
                            current = if (JournalManager.journals.firstOrNull()?.date?.take(10) == LocalDate.now().toString()) 1 else 0,
                            total = 1,
                            color = Color(0xFF9C27B0)
                        )
                    }
                }
            }
        }

        // Financial Summary
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Financial Summary",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "Income",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                                Text(
                                    "₹${String.format("%,.0f", FinanceManager.totalIncome.value)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "Expenses",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                                Text(
                                    "₹${String.format("%,.0f", FinanceManager.totalExpenses.value)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF5252)
                                )
                            }
                        }

                        Divider(color = Color.Gray.copy(alpha = 0.3f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Balance",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "₹${String.format("%,.0f", FinanceManager.currentBalance.value)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2196F3)
                            )
                        }
                    }
                }
            }
        }

        // Recent Activity
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Recent Activity",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )

                // Recent Mood
                if (MoodManager.moods.isNotEmpty()) {
                    val recentMood = MoodManager.moods.first()
                    ActivityCard(
                        icon = recentMood.emoji,
                        title = "Latest Mood",
                        description = "${recentMood.mood} - ${recentMood.date}",
                        color = Color(0xFF4CAF50)
                    )
                }

                // Recent Transaction
                if (FinanceManager.transactions.isNotEmpty()) {
                    val recentTransaction = FinanceManager.transactions.first()
                    ActivityCard(
                        icon = recentTransaction.emoji,
                        title = "Latest Transaction",
                        description = "${recentTransaction.category} - ₹${recentTransaction.amount}",
                        color = Color(0xFF2196F3)
                    )
                }

                // Active Habits
                if (HabitManager.habits.isNotEmpty()) {
                    ActivityCard(
                        icon = "🔥",
                        title = "Active Habits",
                        description = "${HabitManager.habits.size} habits being tracked",
                        color = Color(0xFFFF6B6B)
                    )
                }
            }
        }

        // Bottom Spacing
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun QuickStatCard(
    icon: String,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                icon,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
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
}

@Composable
fun ProgressItem(
    icon: String,
    title: String,
    current: Int,
    total: Int,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(icon, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "$current/$total",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (current >= total) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun ActivityCard(
    icon: String,
    title: String,
    description: String,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    icon,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}
