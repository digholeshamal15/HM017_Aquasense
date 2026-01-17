// HomeScreen.kt
// Create this file in: app/src/main/java/com/example/health/

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onNavigateToFinance: () -> Unit = {}
) {
    val user = UserManager.currentUser

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF3E5F5),
                        Color(0xFFF1F8E9),
                        Color.White
                    )
                )
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF6A1B9A)
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            user?.name?.first()?.uppercase() ?: "U",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "Welcome back,",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            user?.name ?: "User",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Weekly Progress Section
        item {
            Text(
                "Weekly Progress",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color(0xFF6A1B9A)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE3F2FD)
                ),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        "🎯 Your Week at a Glance",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ProgressItem(
                        icon = Icons.Default.Face,
                        label = "Mood Tracking",
                        progress = 5,
                        total = 7,
                        color = Color(0xFF2196F3)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ProgressItem(
                        icon = Icons.Default.Book,
                        label = "Journal Entries",
                        progress = 4,
                        total = 7,
                        color = Color(0xFF4CAF50)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ProgressItem(
                        icon = Icons.Default.SelfImprovement,
                        label = "Meditation Sessions",
                        progress = 3,
                        total = 5,
                        color = Color(0xFF9C27B0)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = 0.65f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF4CAF50)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Overall: 65% weekly goal completed",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }

        // Quick Stats
        item {
            Text(
                "Today's Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color(0xFF6A1B9A)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    icon = "😊",
                    value = "Good",
                    label = "Mood",
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = "📝",
                    value = "1",
                    label = "Entries",
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Features Section
        item {
            Text(
                "Your Wellness Tools",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color(0xFF6A1B9A)
            )
        }

        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureCard(
                    icon = Icons.Default.Face,
                    title = "Mood Tracker",
                    description = "Track and understand your emotional wellbeing",
                    color = Color(0xFF6A1B9A),
                    onClick = {}
                )

                FeatureCard(
                    icon = Icons.Default.Book,
                    title = "Mindfulness Journal",
                    description = "Reflect on your thoughts and experiences",
                    color = Color(0xFF4CAF50),
                    onClick = {}
                )

                FeatureCard(
                    icon = Icons.Default.SelfImprovement,
                    title = "Meditation",
                    description = "Guided meditation for peace of mind",
                    color = Color(0xFF9C27B0),
                    onClick = {}
                )

                FeatureCard(
                    icon = Icons.Default.MusicNote,
                    title = "Music Therapy",
                    description = "Soothing music for relaxation",
                    color = Color(0xFFFF6F00),
                    onClick = {}
                )
            }
        }

        // Personal Finance Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Financial Wellness",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color(0xFF6A1B9A)
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToFinance() },
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                elevation = CardDefaults.cardElevation(8.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "💰",
                                style = MaterialTheme.typography.displaySmall
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Personal Finance",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Manage your money for better mental wellness",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Mini stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            MiniStat(
                                "Balance",
                                "₹${String.format("%,.0f", FinanceManager.currentBalance.value)}"
                            )
                            MiniStat(
                                "Budget",
                                "${if(FinanceManager.budgetTotal.value > 0) (FinanceManager.monthlyExpenses.value/FinanceManager.budgetTotal.value*100).toInt() else 0}%"
                            )
                        }
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Go to Finance",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // Tips Section
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF6A1B9A).copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "💡 Daily Wellness Tip",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6A1B9A)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Financial stress can impact mental health. Take 5 minutes today to review your spending and set a realistic budget.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProgressItem(
    icon: ImageVector,
    label: String,
    progress: Int,
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
            Icon(
                icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            "$progress/$total",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun StatCard(
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
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                icon,
                style = MaterialTheme.typography.displaySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun FeatureCard(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Go",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun MiniStat(label: String, value: String) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f)
        )
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}