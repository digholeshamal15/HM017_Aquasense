// FinanceScreens.kt
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
// Finance Manager to handle state
object FinanceManager {
    val transactions = mutableStateListOf<Transaction>()
    var currentBalance = mutableStateOf(15000.0)
    var monthlyIncome = mutableStateOf(50000.0)
    var monthlyExpenses = mutableStateOf(35000.0)
    var budgetTotal = mutableStateOf(40000.0)

    val categoryBudgets = mutableStateListOf(
        Triple("🍔 Food", 10000.0, 0.0),
        Triple("🚗 Transport", 7000.0, 0.0),
        Triple("🎬 Entertainment", 5000.0, 0.0),
        Triple("🏠 Bills", 15000.0, 0.0),
        Triple("🛍️ Shopping", 8000.0, 0.0)
    )

    val savingsGoals = mutableStateListOf(
        SavingsGoal(
            name = "Emergency Fund",
            targetAmount = 100000.0,
            currentAmount = 45000.0,
            deadline = "2026-12-31",
            emoji = "🛡️"
        ),
        SavingsGoal(
            name = "Vacation",
            targetAmount = 50000.0,
            currentAmount = 30000.0,
            deadline = "2026-06-30",
            emoji = "✈️"
        )
    )

    init {
        // Add sample transactions
        transactions.addAll(listOf(
            Transaction(
                amount = 5000.0,
                category = "Food",
                type = TransactionType.EXPENSE,
                date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                mode = PaymentMode.UPI,
                note = "Restaurant dinner",
                emoji = "🍔"
            ),
            Transaction(
                amount = 50000.0,
                category = "Salary",
                type = TransactionType.INCOME,
                date = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                mode = PaymentMode.NET_BANKING,
                note = "Monthly salary",
                emoji = "💰"
            )
        ))
        updateFinancialData()
    }

    fun addTransaction(transaction: Transaction) {
        transactions.add(0, transaction)
        updateFinancialData()
    }

    fun updateFinancialData() {
        // Calculate income and expenses
        val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

        monthlyIncome.value = income
        monthlyExpenses.value = expenses
        currentBalance.value = income - expenses

        // Update category spending
        val categorySpending = mutableMapOf<String, Double>()
        transactions.filter { it.type == TransactionType.EXPENSE }.forEach { transaction ->
            val currentSpent = categorySpending[transaction.category] ?: 0.0
            categorySpending[transaction.category] = currentSpent + transaction.amount
        }

        // Update category budgets with actual spending
        categoryBudgets.forEachIndexed { index, (emoji, budget, _) ->
            val categoryName = emoji.substringAfter(" ")
            val spent = categorySpending[categoryName] ?: 0.0
            categoryBudgets[index] = Triple(emoji, budget, spent)
        }
    }

    fun getCategorySpending(): List<Triple<String, Double, Color>> {
        val categoryMap = mutableMapOf<String, Double>()
        transactions.filter { it.type == TransactionType.EXPENSE }.forEach { transaction ->
            val current = categoryMap[transaction.category] ?: 0.0
            categoryMap[transaction.category] = current + transaction.amount
        }

        val colors = listOf(
            Color(0xFFFF6B6B),
            Color(0xFF4ECDC4),
            Color(0xFFFFE66D),
            Color(0xFF95E1D3),
            Color(0xFFA8E6CF),
            Color(0xFFFFAEC9),
            Color(0xFFB4F8C8)
        )

        return categoryMap.entries.mapIndexed { index, entry ->
            Triple(
                getCategoryEmoji(entry.key) + " " + entry.key,
                entry.value,
                colors.getOrElse(index) { Color(0xFF9B9B9B) }
            )
        }
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
}

@Composable
fun PersonalFinanceScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Transactions", "Budget", "Goals", "Insights")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF3E5F5),
                        Color(0xFFF1F8E9),
                        Color(0xFFFFF9C4)
                    )
                )
            )
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF6A1B9A)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    "💰 Personal Finance",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Manage your money, enhance your wellness",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        // FIXED TABS SECTION - Remove the tabIndicatorOffset
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            edgePadding = 16.dp,
            // REMOVED THE PROBLEMATIC indicator PARAMETER
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) Color(0xFF6A1B9A) else Color.Gray
                        )
                    }
                )
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> FinanceOverviewTab()
            1 -> TransactionsTab()
            2 -> BudgetTab()
            3 -> SavingsGoalsTab()
            4 -> InsightsTab()
        }
    }
}

@Composable
fun FinanceOverviewTab() {
    var showAddIncomeDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Balance Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1976D2)
                ),
                elevation = CardDefaults.cardElevation(8.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Current Balance",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "₹${String.format("%,.2f", FinanceManager.currentBalance.value)}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Income", style = MaterialTheme.typography.bodySmall, color = Color.White)
                            Text(
                                "₹${String.format("%,.0f", FinanceManager.monthlyIncome.value)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Expenses", style = MaterialTheme.typography.bodySmall, color = Color.White)
                            Text(
                                "₹${String.format("%,.0f", FinanceManager.monthlyExpenses.value)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF5252)
                            )
                        }
                    }
                }
            }
        }

        // Budget Progress Card
        item {
            val budgetUsed = if (FinanceManager.budgetTotal.value > 0)
                (FinanceManager.monthlyExpenses.value / FinanceManager.budgetTotal.value * 100).toInt()
            else 0

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        budgetUsed >= 90 -> Color(0xFFFFEBEE)
                        budgetUsed >= 70 -> Color(0xFFFFF3E0)
                        else -> Color(0xFFE8F5E9)
                    }
                ),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "📊 Budget Status",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "$budgetUsed%",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                budgetUsed >= 90 -> Color(0xFFD32F2F)
                                budgetUsed >= 70 -> Color(0xFFFF6F00)
                                else -> Color(0xFF388E3C)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = (budgetUsed / 100f).coerceAtMost(1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = when {
                            budgetUsed >= 90 -> Color(0xFFD32F2F)
                            budgetUsed >= 70 -> Color(0xFFFF6F00)
                            else -> Color(0xFF388E3C)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "₹${String.format("%,.0f", FinanceManager.monthlyExpenses.value)} of ₹${String.format("%,.0f", FinanceManager.budgetTotal.value)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        when {
                            budgetUsed >= 90 -> "⚠️ Warning! You've exceeded your budget limit"
                            budgetUsed >= 70 -> "⚡ Careful! You're approaching your budget limit"
                            else -> "✅ Great! You're managing your budget well"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Quick Actions
        item {
            Text(
                "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6A1B9A)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    emoji = "➕",
                    title = "Add Income",
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f),
                    onClick = { showAddIncomeDialog = true }
                )
                QuickActionCard(
                    emoji = "➖",
                    title = "Add Expense",
                    color = Color(0xFFFF5252),
                    modifier = Modifier.weight(1f),
                    onClick = { showAddExpenseDialog = true }
                )
            }
        }

        // Category Breakdown
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Spending by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6A1B9A)
            )
        }

        val categorySpending = FinanceManager.getCategorySpending()
        items(categorySpending) { (category, amount, color) ->
            CategorySpendingItem(category, amount, FinanceManager.monthlyExpenses.value, color)
        }
    }

    // Add Income Dialog
    if (showAddIncomeDialog) {
        AddTransactionDialog(
            onDismiss = { showAddIncomeDialog = false },
            onAdd = { transaction ->
                FinanceManager.addTransaction(transaction)
                showAddIncomeDialog = false
            },
            defaultType = TransactionType.INCOME
        )
    }

    // Add Expense Dialog
    if (showAddExpenseDialog) {
        AddTransactionDialog(
            onDismiss = { showAddExpenseDialog = false },
            onAdd = { transaction ->
                FinanceManager.addTransaction(transaction)
                showAddExpenseDialog = false
            },
            defaultType = TransactionType.EXPENSE
        )
    }
}
// Add these functions to the SAME FinanceScreens.kt file (continuation)

// Tab 2: Transactions
@Composable
fun TransactionsTab() {
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recent Transactions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6A1B9A)
                    )
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, "Add Transaction", tint = Color(0xFF6A1B9A))
                    }
                }
            }

            items(FinanceManager.transactions) { transaction ->
                TransactionItem(transaction)
            }

            if (FinanceManager.transactions.isEmpty()) {
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
                                "📝",
                                style = MaterialTheme.typography.displayLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No transactions yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Start tracking your income and expenses",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddTransactionDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { transaction ->
                    FinanceManager.addTransaction(transaction)
                    showAddDialog = false
                }
            )
        }
    }
}

// Tab 3: Budget
@Composable
fun BudgetTab() {
    var showEditDialog by remember { mutableStateOf(false) }
    var editBudgetAmount by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Monthly Budget",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6A1B9A)
                )
                IconButton(onClick = {
                    editBudgetAmount = FinanceManager.budgetTotal.value.toInt().toString()
                    showEditDialog = true
                }) {
                    Icon(Icons.Default.Edit, "Edit Budget", tint = Color(0xFF6A1B9A))
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Total Monthly Budget",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "₹${String.format("%,.0f", FinanceManager.budgetTotal.value)}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF388E3C)
                    )
                }
            }
        }

        item {
            Text(
                "Category Budgets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6A1B9A)
            )
        }

        items(FinanceManager.categoryBudgets) { (category, budget, spent) ->
            BudgetCategoryItem(category, budget, spent)
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Monthly Budget", color = Color(0xFF6A1B9A)) },
            text = {
                OutlinedTextField(
                    value = editBudgetAmount,
                    onValueChange = { editBudgetAmount = it.filter { char -> char.isDigit() } },
                    label = { Text("Budget Amount") },
                    leadingIcon = { Text("₹") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6A1B9A),
                        focusedLabelColor = Color(0xFF6A1B9A),
                        cursorColor = Color(0xFF6A1B9A)
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        editBudgetAmount.toDoubleOrNull()?.let { amount ->
                            FinanceManager.budgetTotal.value = amount
                        }
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6A1B9A)
                    )
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = Color(0xFF6A1B9A))
                }
            }
        )
    }
}

// Tab 4: Savings Goals
@Composable
fun SavingsGoalsTab() {
    var showAddGoalDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Savings Goals",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6A1B9A)
                )
                IconButton(onClick = { showAddGoalDialog = true }) {
                    Icon(Icons.Default.Add, "Add Goal", tint = Color(0xFF6A1B9A))
                }
            }
        }

        items(FinanceManager.savingsGoals) { goal ->
            SavingsGoalItem(goal)
        }

        if (FinanceManager.savingsGoals.isEmpty()) {
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
                        Text("🎯", style = MaterialTheme.typography.displayLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No savings goals yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Set your first savings goal to start building wealth",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "💡 Savings Tips",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF6A1B9A)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf(
                        "Set up automatic transfers to savings",
                        "Track your progress weekly",
                        "Celebrate small milestones",
                        "Review and adjust goals quarterly"
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
}

// Tab 5: Insights
@Composable
fun InsightsTab() {
    val insights = remember {
        listOf(
            FinancialInsight(
                type = InsightType.WARNING,
                message = "Your spending has increased by 15% this month",
                severity = InsightSeverity.WARNING
            ),
            FinancialInsight(
                type = InsightType.TIP,
                message = "You could save ₹5,000 more by reducing dining out",
                severity = InsightSeverity.INFO
            ),
            FinancialInsight(
                type = InsightType.PREDICTION,
                message = "At this rate, you'll exceed your budget by ₹3,000",
                severity = InsightSeverity.CRITICAL
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Financial Insights",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6A1B9A)
            )
        }

        // Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE1F5FE)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "📊 Monthly Summary",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val savingsRate = if (FinanceManager.monthlyIncome.value > 0) {
                        ((FinanceManager.monthlyIncome.value - FinanceManager.monthlyExpenses.value) /
                                FinanceManager.monthlyIncome.value * 100).toInt()
                    } else 0

                    Text(
                        "Your savings rate this month is $savingsRate%",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "💡 Tip: Aim for at least 20% savings rate for financial health",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1976D2),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Financial Insights
        items(insights) { insight ->
            InsightCard(insight)
        }

        // Spending Patterns
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Spending Patterns",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6A1B9A)
            )
        }

        item {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SpendingPattern("This Month", "Total: ₹${String.format("%,.0f", FinanceManager.monthlyExpenses.value)}", Color(0xFFFF6B6B))
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    SpendingPattern("Budget Used", "${if(FinanceManager.budgetTotal.value > 0) (FinanceManager.monthlyExpenses.value/FinanceManager.budgetTotal.value*100).toInt() else 0}%", Color(0xFF4ECDC4))
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    SpendingPattern("Savings", "₹${String.format("%,.0f", FinanceManager.monthlyIncome.value - FinanceManager.monthlyExpenses.value)}", Color(0xFF4CAF50))
                }
            }
        }

        // Recommendations
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Personalized Recommendations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6A1B9A)
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E9)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Ways to improve your finances:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf(
                        "Track every expense for better awareness",
                        "Set realistic category budgets",
                        "Build an emergency fund of 6 months expenses",
                        "Review and adjust your budget monthly"
                    ).forEach { rec ->
                        Text(
                            "✓ $rec",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}