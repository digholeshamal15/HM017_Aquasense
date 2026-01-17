// FirebaseUserManager.kt - COMPLETE VERSION WITH ALL DATA
package com.example.health

import android.util.Log
import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object FirebaseUserManager {
    private const val TAG = "FirebaseUserManager"
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Create a scope for Firebase operations
    private val firebaseScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    var currentUser by mutableStateOf<User?>(null)
        private set
    var isLoggedIn by mutableStateOf(false)
        private set
    var currentTheme by mutableStateOf(ThemePreference.SYSTEM)
        private set
    var isLoading by mutableStateOf(false)
        private set

    init {
        auth.currentUser?.let { firebaseUser ->
            firebaseScope.launch {
                loadUserData(firebaseUser.uid)
                syncAllDataFromFirebase()
            }
        }
    }

    // ==================== AUTHENTICATION ====================

    suspend fun register(
        name: String,
        username: String,
        email: String,
        password: String,
        dateOfBirth: String
    ): Result<User> {
        return try {
            isLoading = true

            if (checkUsernameExists(username)) {
                return Result.failure(Exception("Username already taken"))
            }

            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Registration failed")

            val user = User(
                id = firebaseUser.uid,
                name = name,
                username = username,
                password = "",
                dateOfBirth = dateOfBirth,
                email = email,
                themePreference = ThemePreference.SYSTEM
            )

            saveUserToFirestore(user)
            currentUser = user
            isLoggedIn = true

            isLoading = false
            Result.success(user)
        } catch (e: Exception) {
            isLoading = false
            Log.e(TAG, "Registration error: ${e.message}")
            Result.failure(Exception("Registration failed: ${e.message}"))
        }
    }

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            isLoading = true
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Login failed")

            loadUserData(firebaseUser.uid)
            syncAllDataFromFirebase()

            isLoading = false
            Result.success(currentUser!!)
        } catch (e: Exception) {
            isLoading = false
            Result.failure(Exception("Login failed: ${e.message}"))
        }
    }

    suspend fun loginWithUsername(username: String, password: String): Result<User> {
        return try {
            val email = getUserEmailFromUsername(username)
                ?: return Result.failure(Exception("Username not found"))
            login(email, password)
        } catch (e: Exception) {
            Result.failure(Exception("Login failed: ${e.message}"))
        }
    }

    fun logout() {
        auth.signOut()
        currentUser = null
        isLoggedIn = false
        clearLocalData()
    }

    // ==================== MOOD TRACKING ====================

    suspend fun saveMoodEntry(mood: MoodEntry): Result<Unit> {
        return try {
            val userId = currentUser?.id ?: return Result.failure(Exception("Not logged in"))

            val moodData = hashMapOf(
                "date" to mood.date,
                "mood" to mood.mood,
                "score" to mood.score,
                "emoji" to mood.emoji,
                "timestamp" to System.currentTimeMillis()
            )

            firestore.collection("users/$userId/moods")
                .add(moodData)
                .await()

            Log.d(TAG, "Mood saved: ${mood.mood} - ${mood.score}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving mood: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getMoodHistory(limit: Int = 50): List<MoodEntry> {
        return try {
            val userId = currentUser?.id ?: return emptyList()

            val snapshot = firestore.collection("users/$userId/moods")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                MoodEntry(
                    date = doc.getString("date") ?: "",
                    mood = doc.getString("mood") ?: "",
                    score = doc.getLong("score")?.toInt() ?: 5,
                    emoji = doc.getString("emoji") ?: "😐"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading moods: ${e.message}")
            emptyList()
        }
    }

    // ==================== JOURNAL ENTRIES ====================

    suspend fun saveJournalEntry(entry: JournalEntry): Result<Unit> {
        return try {
            val userId = currentUser?.id ?: return Result.failure(Exception("Not logged in"))

            val journalData = hashMapOf(
                "date" to entry.date,
                "content" to entry.content,
                "audioNote" to (entry.audioNote ?: ""),
                "timestamp" to System.currentTimeMillis()
            )

            firestore.collection("users/$userId/journals")
                .add(journalData)
                .await()

            Log.d(TAG, "Journal entry saved")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving journal: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getJournalEntries(limit: Int = 100): List<JournalEntry> {
        return try {
            val userId = currentUser?.id ?: return emptyList()

            val snapshot = firestore.collection("users/$userId/journals")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                JournalEntry(
                    date = doc.getString("date") ?: "",
                    content = doc.getString("content") ?: "",
                    audioNote = doc.getString("audioNote")?.takeIf { it.isNotEmpty() }
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading journals: ${e.message}")
            emptyList()
        }
    }

    // ==================== HABITS ====================

    suspend fun saveHabit(habit: Habit): Result<Unit> {
        return try {
            val userId = currentUser?.id ?: return Result.failure(Exception("Not logged in"))

            val habitData = hashMapOf(
                "id" to habit.id,
                "name" to habit.name,
                "emoji" to habit.emoji,
                "color" to habit.color.value.toString(),
                "targetDays" to habit.targetDays,
                "completedDates" to habit.completedDates,
                "currentStreak" to habit.currentStreak,
                "longestStreak" to habit.longestStreak,
                "totalCompletions" to habit.totalCompletions,
                "timestamp" to System.currentTimeMillis()
            )

            firestore.collection("users/$userId/habits")
                .document(habit.id)
                .set(habitData)
                .await()

            Log.d(TAG, "Habit saved: ${habit.name}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving habit: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getHabits(): List<Habit> {
        return try {
            val userId = currentUser?.id ?: return emptyList()

            val snapshot = firestore.collection("users/$userId/habits")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    val colorString = doc.getString("color") ?: "0xFFFF6B6B"
                    val color = androidx.compose.ui.graphics.Color(colorString.toLong(16))

                    @Suppress("UNCHECKED_CAST")
                    Habit(
                        id = doc.getString("id") ?: "",
                        name = doc.getString("name") ?: "",
                        emoji = doc.getString("emoji") ?: "💪",
                        color = color,
                        targetDays = doc.getLong("targetDays")?.toInt() ?: 7,
                        completedDates = (doc.get("completedDates") as? List<String>)?.toMutableList() ?: mutableListOf(),
                        currentStreak = doc.getLong("currentStreak")?.toInt() ?: 0,
                        longestStreak = doc.getLong("longestStreak")?.toInt() ?: 0,
                        totalCompletions = doc.getLong("totalCompletions")?.toInt() ?: 0
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading habits: ${e.message}")
            emptyList()
        }
    }

    suspend fun deleteHabit(habitId: String): Result<Unit> {
        return try {
            val userId = currentUser?.id ?: return Result.failure(Exception("Not logged in"))

            firestore.collection("users/$userId/habits")
                .document(habitId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== FINANCE - TRANSACTIONS ====================

    suspend fun saveTransaction(transaction: Transaction): Result<Unit> {
        return try {
            val userId = currentUser?.id ?: return Result.failure(Exception("Not logged in"))

            val transactionData = hashMapOf(
                "id" to transaction.id,
                "amount" to transaction.amount,
                "category" to transaction.category,
                "type" to transaction.type.name,
                "date" to transaction.date,
                "mode" to transaction.mode.name,
                "note" to transaction.note,
                "emoji" to transaction.emoji,
                "timestamp" to System.currentTimeMillis()
            )

            firestore.collection("users/$userId/transactions")
                .document(transaction.id)
                .set(transactionData)
                .await()

            Log.d(TAG, "Transaction saved: ${transaction.category} - ₹${transaction.amount}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving transaction: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getTransactions(limit: Int = 200): List<Transaction> {
        return try {
            val userId = currentUser?.id ?: return emptyList()

            val snapshot = firestore.collection("users/$userId/transactions")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    Transaction(
                        id = doc.getString("id") ?: "",
                        amount = doc.getDouble("amount") ?: 0.0,
                        category = doc.getString("category") ?: "",
                        type = TransactionType.valueOf(doc.getString("type") ?: "EXPENSE"),
                        date = doc.getString("date") ?: "",
                        mode = PaymentMode.valueOf(doc.getString("mode") ?: "CASH"),
                        note = doc.getString("note") ?: "",
                        emoji = doc.getString("emoji") ?: "💰"
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading transactions: ${e.message}")
            emptyList()
        }
    }

    suspend fun deleteTransaction(transactionId: String): Result<Unit> {
        return try {
            val userId = currentUser?.id ?: return Result.failure(Exception("Not logged in"))

            firestore.collection("users/$userId/transactions")
                .document(transactionId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== FINANCE - BUDGETS ====================

    suspend fun saveBudgets(budgets: Map<String, Double>): Result<Unit> {
        return try {
            val userId = currentUser?.id ?: return Result.failure(Exception("Not logged in"))

            firestore.collection("users")
                .document(userId)
                .set(hashMapOf("budgets" to budgets), SetOptions.merge())
                .await()

            Log.d(TAG, "Budgets saved")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving budgets: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getBudgets(): Map<String, Double> {
        return try {
            val userId = currentUser?.id ?: return emptyMap()

            val doc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            @Suppress("UNCHECKED_CAST")
            (doc.get("budgets") as? Map<String, Double>) ?: emptyMap()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading budgets: ${e.message}")
            emptyMap()
        }
    }

    // ==================== FINANCE - SAVINGS GOALS ====================

    suspend fun saveSavingsGoal(goal: SavingsGoal): Result<Unit> {
        return try {
            val userId = currentUser?.id ?: return Result.failure(Exception("Not logged in"))

            val goalData = hashMapOf(
                "id" to goal.id,
                "name" to goal.name,
                "targetAmount" to goal.targetAmount,
                "currentAmount" to goal.currentAmount,
                "deadline" to goal.deadline,
                "emoji" to goal.emoji,
                "timestamp" to System.currentTimeMillis()
            )

            firestore.collection("users/$userId/savingsGoals")
                .document(goal.id)
                .set(goalData)
                .await()

            Log.d(TAG, "Savings goal saved: ${goal.name}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving goal: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getSavingsGoals(): List<SavingsGoal> {
        return try {
            val userId = currentUser?.id ?: return emptyList()

            val snapshot = firestore.collection("users/$userId/savingsGoals")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                SavingsGoal(
                    id = doc.getString("id") ?: "",
                    name = doc.getString("name") ?: "",
                    targetAmount = doc.getDouble("targetAmount") ?: 0.0,
                    currentAmount = doc.getDouble("currentAmount") ?: 0.0,
                    deadline = doc.getString("deadline") ?: "",
                    emoji = doc.getString("emoji") ?: "🎯"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading goals: ${e.message}")
            emptyList()
        }
    }

    suspend fun deleteSavingsGoal(goalId: String): Result<Unit> {
        return try {
            val userId = currentUser?.id ?: return Result.failure(Exception("Not logged in"))

            firestore.collection("users/$userId/savingsGoals")
                .document(goalId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== USER PROFILE ====================

    suspend fun updateUserProfile(updatedUser: User): Result<User> {
        return try {
            saveUserToFirestore(updatedUser)
            currentUser = updatedUser
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changeTheme(theme: ThemePreference) {
        currentTheme = theme
        currentUser?.let { user ->
            val updatedUser = user.copy(themePreference = theme)
            updateUserProfile(updatedUser)
        }
    }

    // ==================== DATA SYNC ====================

    private suspend fun syncAllDataFromFirebase() {
        try {
            // Sync moods
            val moods = getMoodHistory()
            Log.d(TAG, "Loaded ${moods.size} moods")

            // Sync journals
            val journals = getJournalEntries()
            Log.d(TAG, "Loaded ${journals.size} journal entries")

            // Sync habits
            val habits = getHabits()
            HabitManager.habits.clear()
            HabitManager.habits.addAll(habits)
            Log.d(TAG, "Loaded ${habits.size} habits")

            // Sync transactions
            val transactions = getTransactions()
            FinanceManager.transactions.clear()
            FinanceManager.transactions.addAll(transactions)
            Log.d(TAG, "Loaded ${transactions.size} transactions")

            // Sync budgets
            val budgets = getBudgets()
            FinanceManager.budgets.clear()
            FinanceManager.budgets.putAll(budgets)
            Log.d(TAG, "Loaded ${budgets.size} budgets")

            // Sync savings goals
            val goals = getSavingsGoals()
            FinanceManager.savingsGoals.clear()
            FinanceManager.savingsGoals.addAll(goals)
            Log.d(TAG, "Loaded ${goals.size} savings goals")

            FinanceManager.calculateTotals()
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing data: ${e.message}")
        }
    }

    private fun clearLocalData() {
        HabitManager.habits.clear()
        FinanceManager.transactions.clear()
        FinanceManager.budgets.clear()
        FinanceManager.savingsGoals.clear()
    }

    // ==================== HELPER FUNCTIONS ====================

    private suspend fun checkUsernameExists(username: String): Boolean {
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("username", username)
                .get()
                .await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun saveUserToFirestore(user: User) {
        val userData = hashMapOf(
            "id" to user.id,
            "name" to user.name,
            "username" to user.username,
            "dateOfBirth" to user.dateOfBirth,
            "email" to user.email,
            "themePreference" to user.themePreference.name,
            "profileImageUrl" to user.profileImageUrl,
            "createdAt" to System.currentTimeMillis()
        )

        firestore.collection("users")
            .document(user.id)
            .set(userData, SetOptions.merge())
            .await()
    }

    private suspend fun loadUserData(userId: String) {
        try {
            val doc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            if (doc.exists()) {
                val user = User(
                    id = doc.getString("id") ?: userId,
                    name = doc.getString("name") ?: "",
                    username = doc.getString("username") ?: "",
                    password = "",
                    dateOfBirth = doc.getString("dateOfBirth") ?: "",
                    email = doc.getString("email") ?: "",
                    profileImageUrl = doc.getString("profileImageUrl") ?: "",
                    themePreference = ThemePreference.valueOf(
                        doc.getString("themePreference") ?: "SYSTEM"
                    )
                )

                currentUser = user
                isLoggedIn = true
                currentTheme = user.themePreference
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading user: ${e.message}")
        }
    }

    private suspend fun getUserEmailFromUsername(username: String): String? {
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                snapshot.documents[0].getString("email")
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
