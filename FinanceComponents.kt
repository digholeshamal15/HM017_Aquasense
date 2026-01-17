// FinanceComponents.kt - CLEANED VERSION (No Conflicts)
package com.example.health

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// Category Spending Item
@Composable
fun CategorySpendingItem(
    category: String,
    amount: Double,
    totalExpenses: Double,
    color: Color
) {
    val percentage = if (totalExpenses > 0) (amount / totalExpenses * 100).toInt() else 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
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
                    category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "₹${String.format("%,.0f", amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = (percentage / 100f).coerceAtMost(1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "$percentage% of total expenses",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

// Budget Category Item
@Composable
fun BudgetCategoryItem(
    category: String,
    budgetAmount: Double,
    spentAmount: Double
) {
    val percentage = if (budgetAmount > 0) (spentAmount / budgetAmount * 100).toInt() else 0
    val remainingAmount = budgetAmount - spentAmount

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                percentage >= 100 -> Color(0xFFFFEBEE)
                percentage >= 80 -> Color(0xFFFFF3E0)
                else -> Color(0xFFE8F5E9)
            }
        ),
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
                    category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "$percentage%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        percentage >= 100 -> Color(0xFFD32F2F)
                        percentage >= 80 -> Color(0xFFFF6F00)
                        else -> Color(0xFF388E3C)
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = (percentage / 100f).coerceAtMost(1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when {
                    percentage >= 100 -> Color(0xFFD32F2F)
                    percentage >= 80 -> Color(0xFFFF6F00)
                    else -> Color(0xFF388E3C)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Spent: ₹${String.format("%,.0f", spentAmount)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Budget: ₹${String.format("%,.0f", budgetAmount)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                if (remainingAmount >= 0)
                    "Remaining: ₹${String.format("%,.0f", remainingAmount)}"
                else
                    "Over budget by: ₹${String.format("%,.0f", -remainingAmount)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = when {
                    percentage >= 100 -> Color(0xFFD32F2F)
                    percentage >= 80 -> Color(0xFFFF6F00)
                    else -> Color(0xFF388E3C)
                }
            )
        }
    }
}

// Savings Goal Item
@Composable
fun SavingsGoalItem(goal: SavingsGoal) {
    val progress = (goal.currentAmount / goal.targetAmount * 100).toInt()
    val remaining = goal.targetAmount - goal.currentAmount

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF6A1B9A).copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        goal.emoji,
                        style = MaterialTheme.typography.displaySmall
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            goal.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Target: ${goal.deadline}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                Text(
                    "$progress%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6A1B9A)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = (progress / 100f).coerceAtMost(1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = Color(0xFF6A1B9A)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Current",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        "₹${String.format("%,.0f", goal.currentAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6A1B9A)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Target",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        "₹${String.format("%,.0f", goal.targetAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "₹${String.format("%,.0f", remaining)} more to reach your goal",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6A1B9A)
            )
        }
    }
}

// Insight Card
@Composable
fun InsightCard(insight: FinancialInsight) {
    val (icon, color) = when (insight.severity) {
        InsightSeverity.CRITICAL -> "⚠️" to Color(0xFFD32F2F)
        InsightSeverity.WARNING -> "⚡" to Color(0xFFFF6F00)
        InsightSeverity.INFO -> "💡" to Color(0xFF1976D2)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                icon,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(end = 12.dp)
            )
            Text(
                insight.message,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// Spending Pattern Row
@Composable
fun SpendingPattern(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
