// WellnessScreen.kt
// Create this file in: app/src/main/java/com/example/health/

package com.example.health

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun WellnessScreen(navController: NavHostController) {
    val wellnessFeatures = listOf(
        WellnessFeature("🎯", "Mood Journey", "Track your daily emotions", "mood_journey", Color(0xFF6A1B9A)),
        WellnessFeature("🎵", "Music Therapy", "Mood-based playlists", "music", Color(0xFFE91E63)),
        WellnessFeature("😴", "Sleep Helper", "Better sleep tips", "sleep_help", Color(0xFF2196F3)),
        WellnessFeature("🧠", "Anxiety Check", "Mental health screening", "anxiety_check", Color(0xFFFF9800)),
        WellnessFeature("🧘", "Meditation", "Quick calm techniques", "meditation", Color(0xFF4CAF50)),
        WellnessFeature("📓", "Daily Journal", "Write your thoughts", "journal", Color(0xFF9C27B0))
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF3E5F5),
                        Color(0xFFFCE4EC),
                        Color.White
                    )
                )
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Wellness Center",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6A1B9A)
            )
            Text(
                "Take care of your mental health",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(wellnessFeatures.chunked(2)) { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { feature ->
                    WellnessFeatureCard(
                        feature = feature,
                        onClick = { navController.navigate(feature.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Wellness Tips Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF6A1B9A).copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        "💡 Wellness Tips",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6A1B9A)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val tips = listOf(
                        "Track your mood daily to understand patterns",
                        "Practice gratitude - write 3 things you're grateful for",
                        "Get 7-9 hours of quality sleep each night",
                        "Stay connected with friends and family",
                        "Take breaks during the day to breathe deeply"
                    )

                    tips.forEach { tip ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("• ", color = Color(0xFF6A1B9A), fontWeight = FontWeight.Bold)
                            Text(
                                tip,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WellnessFeatureCard(
    feature: WellnessFeature,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = feature.color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                feature.emoji,
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                feature.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = feature.color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                feature.description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
        }
    }
}

data class WellnessFeature(
    val emoji: String,
    val title: String,
    val description: String,
    val route: String,
    val color: Color
)