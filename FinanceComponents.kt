// FinanceComponents.kt
// Create this file in: app/src/main/java/com/example/health/

package com.example.health

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Transaction Item Component
@Composable
fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (transaction.type == TransactionType.INCOME)
                Color(0xFFE8F5E9)
            else
                Color(0xFFFFEBEE)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (transaction.type == TransactionType.INCOME)
                                Color(0xFF4CAF50).copy(alpha = 0.2f)
                            else
                                Color(0xFFFF5252).copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        transaction.emoji,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        transaction.category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        transaction.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    if (transaction.note.isNotEmpty()) {
                        Text(
                            transaction.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${if (transaction.type == TransactionType.INCOME) "+" else "-"} ₹${String.format("%,.2f", transaction.amount)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.type == TransactionType.INCOME)
                        Color(0xFF4CAF50)
                    else
                        Color(0xFFFF5252)
                )
                Text(
                    transaction.mode.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

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
        )
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
        )
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
        elevation = CardDefaults.cardElevation(4.dp)
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

// Quick Action Card
@Composable
fun QuickActionCard(
    emoji: String,
    title: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1.5f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                emoji,
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center
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
        shape = RoundedCornerShape(16.dp)
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

// Add Transaction Dialog
@OptIn(ExperimentalMaterial3Api::class)  // ADD THIS ANNOTATION
@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onAdd: (Transaction) -> Unit,
    defaultType: TransactionType = TransactionType.EXPENSE
) {
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf(defaultType) }
    var paymentMode by remember { mutableStateOf(PaymentMode.UPI) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showPaymentModeMenu by remember { mutableStateOf(false) }

    val expenseCategories = listOf("Food", "Transport", "Entertainment", "Bills", "Shopping", "Healthcare", "Education", "Other")
    val incomeCategories = listOf("Salary", "Business", "Investment", "Gift", "Other")
    val categories = if (transactionType == TransactionType.EXPENSE) expenseCategories else incomeCategories

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add ${if (transactionType == TransactionType.INCOME) "Income" else "Expense"}",
                color = Color(0xFF6A1B9A),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Type Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = transactionType == TransactionType.EXPENSE,
                        onClick = { transactionType = TransactionType.EXPENSE },
                        label = { Text("Expense") },
                        leadingIcon = if (transactionType == TransactionType.EXPENSE) {
                            { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFF5252).copy(alpha = 0.2f),
                            selectedLabelColor = Color(0xFFFF5252)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = transactionType == TransactionType.INCOME,
                        onClick = { transactionType = TransactionType.INCOME },
                        label = { Text("Income") },
                        leadingIcon = if (transactionType == TransactionType.INCOME) {
                            { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF4CAF50).copy(alpha = 0.2f),
                            selectedLabelColor = Color(0xFF4CAF50)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Amount
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Amount") },
                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6A1B9A),
                        focusedLabelColor = Color(0xFF6A1B9A),
                        cursorColor = Color(0xFF6A1B9A)
                    )
                )

                // Category
                ExposedDropdownMenuBox(
                    expanded = showCategoryMenu,
                    onExpandedChange = { showCategoryMenu = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6A1B9A),
                            focusedLabelColor = Color(0xFF6A1B9A)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = showCategoryMenu,
                        onDismissRequest = { showCategoryMenu = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    showCategoryMenu = false
                                }
                            )
                        }
                    }
                }

                // Payment Mode
                ExposedDropdownMenuBox(
                    expanded = showPaymentModeMenu,
                    onExpandedChange = { showPaymentModeMenu = it }
                ) {
                    OutlinedTextField(
                        value = paymentMode.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Mode") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPaymentModeMenu) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6A1B9A),
                            focusedLabelColor = Color(0xFF6A1B9A)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = showPaymentModeMenu,
                        onDismissRequest = { showPaymentModeMenu = false }
                    ) {
                        PaymentMode.values().forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.name) },
                                onClick = {
                                    paymentMode = mode
                                    showPaymentModeMenu = false
                                }
                            )
                        }
                    }
                }

                // Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6A1B9A),
                        focusedLabelColor = Color(0xFF6A1B9A),
                        cursorColor = Color(0xFF6A1B9A)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (amountValue != null && amountValue > 0 && category.isNotEmpty()) {
                        val transaction = Transaction(
                            amount = amountValue,
                            category = category,
                            type = transactionType,
                            date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                            mode = paymentMode,
                            note = note,
                            emoji = getCategoryEmoji(category)
                        )
                        onAdd(transaction)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6A1B9A)
                ),
                enabled = amount.toDoubleOrNull() != null && category.isNotEmpty()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF6A1B9A))
            }
        }
    )
}

private fun getCategoryEmoji(category: String): String {
    return when (category) {
        "Food" -> "🍔"
        "Transport" -> "🚗"
        "Entertainment" -> "🎬"
        "Bills" -> "🏠"
        "Shopping" -> "🛍️"
        "Healthcare" -> "💊"
        "Education" -> "📚"
        "Salary" -> "💰"
        "Business" -> "💼"
        "Investment" -> "📈"
        "Gift" -> "🎁"
        else -> "🎯"
    }
}