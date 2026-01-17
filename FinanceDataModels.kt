// FinanceDataModels.kt - UPDATED
package com.example.health

import androidx.compose.runtime.*
import java.time.LocalDate

// Keep your existing data classes and enums
data class Transaction(
    val id: String = java.util.UUID.randomUUID().toString(),
    val amount: Double,
    val category: String,
    val type: TransactionType,
    val date: String,
    val mode: PaymentMode,
    val note: String = "",
    val emoji: String = "💰"
)

enum class TransactionType {
    INCOME,
    EXPENSE
}

enum class PaymentMode {
    CASH,
    UPI,
    CARD,
    NET_BANKING
}

data class Budget(
    val category: String,
    val amount: Double,
    val spent: Double = 0.0
)

data class SavingsGoal(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val deadline: String,
    val emoji: String = "🎯"
)

data class FinancialInsight(
    val message: String,
    val severity: InsightSeverity
)

enum class InsightSeverity {
    INFO,
    WARNING,
    CRITICAL
}

object FinanceManager {
    val transactions = mutableStateListOf<Transaction>(
        Transaction(
            amount = 50000.0,
            category = "Salary",
            type = TransactionType.INCOME,
            date = LocalDate.now().toString(),
            mode = PaymentMode.NET_BANKING,
            note = "Monthly salary",
            emoji = "💰"
        ),
        Transaction(
            amount = 1500.0,
            category = "Food",
            type = TransactionType.EXPENSE,
            date = LocalDate.now().toString(),
            mode = PaymentMode.UPI,
            note = "Groceries",
            emoji = "🍔"
        ),
        Transaction(
            amount = 800.0,
            category = "Transport",
            type = TransactionType.EXPENSE,
            date = LocalDate.now().minusDays(1).toString(),
            mode = PaymentMode.CARD,
            note = "Fuel",
            emoji = "🚗"
        ),
        Transaction(
            amount = 2000.0,
            category = "Bills",
            type = TransactionType.EXPENSE,
            date = LocalDate.now().minusDays(2).toString(),
            mode = PaymentMode.NET_BANKING,
            note = "Electricity bill",
            emoji = "🏠"
        )
    )

    val budgets = mutableStateMapOf(
        "Food" to 5000.0,
        "Transport" to 3000.0,
        "Entertainment" to 2000.0,
        "Bills" to 4000.0,
        "Shopping" to 3000.0,
        "Healthcare" to 2000.0
    )

    val savingsGoals = mutableStateListOf(
        SavingsGoal(
            name = "Emergency Fund",
            targetAmount = 100000.0,
            currentAmount = 45000.0,
            deadline = "2026-12-31",
            emoji = "🏦"
        ),
        SavingsGoal(
            name = "Vacation",
            targetAmount = 50000.0,
            currentAmount = 18000.0,
            deadline = "2026-06-30",
            emoji = "✈️"
        ),
        SavingsGoal(
            name = "New Laptop",
            targetAmount = 80000.0,
            currentAmount = 25000.0,
            deadline = "2026-09-30",
            emoji = "💻"
        )
    )

    val totalIncome = mutableStateOf(0.0)
    val totalExpenses = mutableStateOf(0.0)
    val currentBalance = mutableStateOf(0.0)
    val monthlyExpenses = mutableStateOf(0.0)
    val budgetTotal = mutableStateOf(0.0)

    val showAddExpenseDialog = mutableStateOf(false)
    val showAddIncomeDialog = mutableStateOf(false)

    init {
        calculateTotals()
    }

    fun addTransaction(transaction: Transaction) {
        transactions.add(0, transaction)
        calculateTotals()
    }

    // CHANGED: Made public instead of private
    fun calculateTotals() {
        val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

        totalIncome.value = income
        totalExpenses.value = expenses
        currentBalance.value = income - expenses
        monthlyExpenses.value = expenses
        budgetTotal.value = budgets.values.sum()
    }

    fun getCategorySpending(category: String): Double {
        return transactions
            .filter { it.type == TransactionType.EXPENSE && it.category == category }
            .sumOf { it.amount }
    }

    fun getBiggestExpenseCategory(): String {
        val categoryTotals = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { t -> t.amount } }

        return categoryTotals.maxByOrNull { it.value }?.let {
            "${it.key}: ₹${String.format("%,.0f", it.value)}"
        } ?: "No expenses"
    }

    fun getAverageDailySpending(): Double {
        val days = 30
        return if (days > 0) totalExpenses.value / days else 0.0
    }

    fun getFinancialInsights(): List<FinancialInsight> {
        val insights = mutableListOf<FinancialInsight>()

        budgets.forEach { (category, budget) ->
            val spent = getCategorySpending(category)
            val percentage = if (budget > 0) (spent / budget * 100).toInt() else 0

            when {
                percentage >= 100 -> insights.add(
                    FinancialInsight(
                        "You've exceeded your $category budget by ₹${String.format("%,.0f", spent - budget)}",
                        InsightSeverity.CRITICAL
                    )
                )
                percentage >= 80 -> insights.add(
                    FinancialInsight(
                        "You're at $percentage% of your $category budget",
                        InsightSeverity.WARNING
                    )
                )
            }
        }

        val savingsRate = if (totalIncome.value > 0) {
            ((totalIncome.value - totalExpenses.value) / totalIncome.value * 100).toInt()
        } else 0

        when {
            savingsRate < 10 -> insights.add(
                FinancialInsight(
                    "Your savings rate is only $savingsRate%. Try to save at least 20% of your income",
                    InsightSeverity.WARNING
                )
            )
            savingsRate >= 20 -> insights.add(
                FinancialInsight(
                    "Great job! You're saving $savingsRate% of your income",
                    InsightSeverity.INFO
                )
            )
        }

        if (totalExpenses.value > totalIncome.value) {
            insights.add(
                FinancialInsight(
                    "Warning: Your expenses (₹${String.format("%,.0f", totalExpenses.value)}) exceed your income (₹${String.format("%,.0f", totalIncome.value)})",
                    InsightSeverity.CRITICAL
                )
            )
        }

        if (insights.isEmpty()) {
            insights.add(
                FinancialInsight(
                    "Your finances are looking good! Keep tracking your expenses",
                    InsightSeverity.INFO
                )
            )
        }

        return insights
    }
}
