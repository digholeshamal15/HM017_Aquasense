// FirebaseUserManager.kt
// CREATE THIS FILE in: app/src/main/java/com/example/health/

package com.example.health

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Firebase User Manager
 * Handles all Firebase Authentication and Firestore operations
 */
object FirebaseUserManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    var currentUser by mutableStateOf<User?>(null)
        private set

    var isLoggedIn by mutableStateOf(false)
        private set

    var currentTheme by mutableStateOf(ThemePreference.SYSTEM)
        private set

    var isLoading by mutableStateOf(false)
        private set

    init {
        // Check if user is already logged in
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            CoroutineScope(Dispatchers.Main).launch {
                loadUserData(firebaseUser.uid)
            }
        }
    }

    /**
     * Register new user with Firebase Authentication and Firestore
     */
    suspend fun register(
        name: String,
        username: String,
        email: String,
        password: String,
        dateOfBirth: String
    ): Result<User> {
        return try {
            isLoading = true

            // Validate inputs
            if (username.length < 3) {
                return Result.failure(Exception("Username must be at least 3 characters"))
            }
            if (password.length < 6) {
                return Result.failure(Exception("Password must be at least 6 characters"))
            }
            if (name.isBlank()) {
                return Result.failure(Exception("Name cannot be empty"))
            }
            if (email.isBlank() || !email.contains("@")) {
                return Result.failure(Exception("Valid email is required"))
            }

            // Check if username already exists
            val usernameExists = checkUsernameExists(username)
            if (usernameExists) {
                return Result.failure(Exception("Username already exists"))
            }

            // Create Firebase Auth user
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Failed to create user"))

            // Create user object
            val user = User(
                id = firebaseUser.uid,
                name = name,
                username = username,
                password = "", // Don't store password in Firestore
                dateOfBirth = dateOfBirth,
                email = email,
                themePreference = ThemePreference.SYSTEM
            )

            // Save user data to Firestore
            saveUserToFirestore(user)

            // Set current user
            currentUser = user
            isLoggedIn = true
            currentTheme = user.themePreference

            isLoading = false
            Result.success(user)
        } catch (e: Exception) {
            isLoading = false
            Result.failure(Exception("Registration failed: ${e.message}"))
        }
    }

    /**
     * Login user with Firebase Authentication
     */
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            isLoading = true

            // Sign in with Firebase Auth
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Login failed"))

            // Load user data from Firestore
            loadUserData(firebaseUser.uid)

            isLoading = false
            Result.success(currentUser!!)
        } catch (e: Exception) {
            isLoading = false
            Result.failure(Exception("Login failed: ${e.message}"))
        }
    }

    /**
     * Login with username instead of email
     */
    suspend fun loginWithUsername(username: String, password: String): Result<User> {
        return try {
            isLoading = true

            // Get email from username
            val email = getUserEmailFromUsername(username)
            if (email == null) {
                isLoading = false
                return Result.failure(Exception("Username not found"))
            }

            // Login with email
            login(email, password)
        } catch (e: Exception) {
            isLoading = false
            Result.failure(Exception("Login failed: ${e.message}"))
        }
    }

    /**
     * Logout current user
     */
    fun logout() {
        auth.signOut()
        currentUser = null
        isLoggedIn = false
    }

    /**
     * Update user profile in Firestore
     */
    suspend fun updateUserProfile(updatedUser: User): Result<User> {
        return try {
            val userId = currentUser?.id ?: return Result.failure(Exception("No user logged in"))

            saveUserToFirestore(updatedUser)
            currentUser = updatedUser

            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(Exception("Update failed: ${e.message}"))
        }
    }

    /**
     * Change theme preference
     */
    suspend fun changeTheme(theme: ThemePreference) {
        currentTheme = theme
        currentUser?.let { user ->
            val updatedUser = user.copy(themePreference = theme)
            updateUserProfile(updatedUser)
        }
    }

    /**
     * Save mood entry to Firestore
     */
    suspend fun saveMoodEntry(moodEntry: MoodEntry): Result<Unit> {
        return try {
            val userId = currentUser?.id ?: return Result.failure(Exception("No user logged in"))

            val moodData = hashMapOf(
                "date" to moodEntry.date,
                "mood" to moodEntry.mood,
                "score" to moodEntry.score,
                "emoji" to moodEntry.emoji,
                "timestamp" to System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(userId)
                .collection("moods")
                .add(moodData)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save mood: ${e.message}"))
        }
    }

    /**
     * Get mood history from Firestore
     */
    suspend fun getMoodHistory(limit: Int = 30): List<MoodEntry> {
        return try {
            val userId = currentUser?.id ?: return emptyList()

            val querySnapshot = firestore.collection("users")
                .document(userId)
                .collection("moods")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            querySnapshot.documents.mapNotNull { doc ->
                MoodEntry(
                    date = doc.getString("date") ?: "",
                    mood = doc.getString("mood") ?: "",
                    score = doc.getLong("score")?.toInt() ?: 5,
                    emoji = doc.getString("emoji") ?: "😐"
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Save journal entry to Firestore
     */
    suspend fun saveJournalEntry(journalEntry: JournalEntry): Result<Unit> {
        return try {
            val userId = currentUser?.id ?: return Result.failure(Exception("No user logged in"))

            val journalData = hashMapOf(
                "date" to journalEntry.date,
                "content" to journalEntry.content,
                "audioNote" to journalEntry.audioNote,
                "timestamp" to System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(userId)
                .collection("journals")
                .add(journalData)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save journal: ${e.message}"))
        }
    }

    /**
     * Get journal entries from Firestore
     */
    suspend fun getJournalEntries(limit: Int = 50): List<JournalEntry> {
        return try {
            val userId = currentUser?.id ?: return emptyList()

            val querySnapshot = firestore.collection("users")
                .document(userId)
                .collection("journals")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            querySnapshot.documents.mapNotNull { doc ->
                JournalEntry(
                    date = doc.getString("date") ?: "",
                    content = doc.getString("content") ?: "",
                    audioNote = doc.getString("audioNote")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Save transaction to Firestore
     */
    suspend fun saveTransaction(transaction: Transaction): Result<Unit> {
        return try {
            val userId = currentUser?.id ?: return Result.failure(Exception("No user logged in"))

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

            firestore.collection("users")
                .document(userId)
                .collection("transactions")
                .document(transaction.id)
                .set(transactionData)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save transaction: ${e.message}"))
        }
    }

    /**
     * Get transactions from Firestore
     */
    suspend fun getTransactions(limit: Int = 100): List<Transaction> {
        return try {
            val userId = currentUser?.id ?: return emptyList()

            val querySnapshot = firestore.collection("users")
                .document(userId)
                .collection("transactions")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            querySnapshot.documents.mapNotNull { doc ->
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
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Save budget to Firestore
     */
    suspend fun saveBudgets(budgets: Map<String, Double>): Result<Unit> {
        return try {
            val userId = currentUser?.id ?: return Result.failure(Exception("No user logged in"))

            firestore.collection("users")
                .document(userId)
                .set(hashMapOf("budgets" to budgets), SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save budgets: ${e.message}"))
        }
    }

    /**
     * Get budgets from Firestore
     */
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
            emptyMap()
        }
    }

    /**
     * Save savings goal to Firestore
     */
    suspend fun saveSavingsGoal(goal: SavingsGoal): Result<Unit> {
        return try {
            val userId = currentUser?.id ?: return Result.failure(Exception("No user logged in"))

            val goalData = hashMapOf(
                "id" to goal.id,
                "name" to goal.name,
                "targetAmount" to goal.targetAmount,
                "currentAmount" to goal.currentAmount,
                "deadline" to goal.deadline,
                "emoji" to goal.emoji,
                "timestamp" to System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(userId)
                .collection("savingsGoals")
                .document(goal.id)
                .set(goalData)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save goal: ${e.message}"))
        }
    }

    /**
     * Get savings goals from Firestore
     */
    suspend fun getSavingsGoals(): List<SavingsGoal> {
        return try {
            val userId = currentUser?.id ?: return emptyList()

            val querySnapshot = firestore.collection("users")
                .document(userId)
                .collection("savingsGoals")
                .get()
                .await()

            querySnapshot.documents.mapNotNull { doc ->
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
            emptyList()
        }
    }

    /**
     * Save habit to Firestore
     */
    suspend fun saveHabit(habit: Habit): Result<Unit> {
        return try {
            val userId = currentUser?.id ?: return Result.failure(Exception("No user logged in"))

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

            firestore.collection("users")
                .document(userId)
                .collection("habits")
                .document(habit.id)
                .set(habitData)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save habit: ${e.message}"))
        }
    }

    /**
     * Get habits from Firestore
     */
    suspend fun getHabits(): List<Habit> {
        return try {
            val userId = currentUser?.id ?: return emptyList()

            val querySnapshot = firestore.collection("users")
                .document(userId)
                .collection("habits")
                .get()
                .await()

            querySnapshot.documents.mapNotNull { doc ->
                val colorString = doc.getString("color") ?: "0xFFFF6B6B"
                val color = try {
                    androidx.compose.ui.graphics.Color(colorString.toLong(16))
                } catch (e: Exception) {
                    androidx.compose.ui.graphics.Color(0xFFFF6B6B)
                }

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
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Private helper functions

    private suspend fun checkUsernameExists(username: String): Boolean {
        return try {
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("username", username)
                .get()
                .await()
            !querySnapshot.isEmpty
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
            // Handle error
        }
    }

    private suspend fun getUserEmailFromUsername(username: String): String? {
        return try {
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                querySnapshot.documents[0].getString("email")
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
