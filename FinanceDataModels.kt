// FinanceDataModels.kt
// Create this file in: app/src/main/java/com/example/health/

package com.example.health

/**
 * Data models for the Personal Finance module
 */

// Transaction data class
data class Transaction(
    val id: String = java.util.UUID.randomUUID().toString(),
    val amount: Double,
    val category: String,
    val type: TransactionType,
    val date: String,
    val mode: PaymentMode,
    val note: String = "",
    val emoji: String
)

// Transaction type enum
enum class TransactionType {
    INCOME,
    EXPENSE
}

// Payment mode enum
enum class PaymentMode {
    CASH,
    CARD,
    UPI,
    NET_BANKING,
    OTHER
}

// Budget data class
data class Budget(
    val totalMonthly: Double,
    val categoryLimits: Map<String, Double>
)

// Savings goal data class
data class SavingsGoal(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val deadline: String,
    val emoji: String
)

// Financial insight data class
data class FinancialInsight(
    val type: InsightType,
    val message: String,
    val severity: InsightSeverity
)

// Insight type enum
enum class InsightType {
    UNUSUAL_SPENDING,
    PREDICTION,
    TIP,
    WARNING
}

// Insight severity enum
enum class InsightSeverity {
    INFO,
    WARNING,
    CRITICAL
}

// Category spending data (for visualization)
data class CategorySpending(
    val category: String,
    val emoji: String,
    val amount: Double,
    val color: androidx.compose.ui.graphics.Color
)

// Budget category data
data class BudgetCategory(
    val category: String,
    val emoji: String,
    val budgetAmount: Double,
    val spentAmount: Double
)