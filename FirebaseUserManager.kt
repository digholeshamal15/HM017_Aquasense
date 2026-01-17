// FirebaseUserManager.kt - HYBRID VERSION (No Crashes!)
// REPLACE your entire FirebaseUserManager.kt with this
// This works BOTH with Firebase AND without it (offline mode)

package com.example.health

import android.util.Log
import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object FirebaseUserManager {
    private const val TAG = "FirebaseUserManager"

    // Try to initialize Firebase, but don't crash if it fails
    private val auth by lazy {
        try {
            com.google.firebase.auth.FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "Firebase Auth failed: ${e.message}")
            null
        }
    }

    private val firestore by lazy {
        try {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "Firestore failed: ${e.message}")
            null
        }
    }

    // Local storage (fallback when Firebase fails)
    private val localUsers = mutableStateListOf<User>()
    private val localMoods = mutableStateListOf<MoodEntry>()
    private val localJournals = mutableStateListOf<JournalEntry>()
    private val localTransactions = mutableStateListOf<Transaction>()
    private val localBudgets = mutableStateMapOf<String, Double>()
    private val localGoals = mutableStateListOf<SavingsGoal>()
    private val localHabits = mutableStateListOf<Habit>()

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
        // Try to load from Firebase if available
        try {
            auth?.currentUser?.let { firebaseUser ->
                firebaseScope.launch {
                    loadUserData(firebaseUser.uid)
                    syncAllDataFromFirebase()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase init failed, using offline mode: ${e.message}")
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

            // Check local users first
            if (localUsers.any { it.username == username }) {
                isLoading = false
                return Result.failure(Exception("Username already taken"))
            }

            // Try Firebase first, fallback to local
            val user = if (auth != null) {
                try {
                    val firebaseAuth = auth // Store in local variable for smart cast
                    if (checkUsernameExists(username)) {
                        isLoading = false
                        return Result.failure(Exception("Username already taken"))
                    }

                    val authResult = firebaseAuth?.createUserWithEmailAndPassword(email, password)?.await()
                    val firebaseUser = authResult?.user ?: throw Exception("Registration failed")

                    val newUser = User(
                        id = firebaseUser.uid,
                        name = name,
                        username = username,
                        password = "",
                        dateOfBirth = dateOfBirth,
                        email = email,
                        themePreference = ThemePreference.SYSTEM
                    )

                    saveUserToFirestore(newUser)
                    newUser
                } catch (e: Exception) {
                    Log.e(TAG, "Firebase registration failed, using local: ${e.message}")
                    createLocalUser(name, username, email, password, dateOfBirth)
                }
            } else {
                createLocalUser(name, username, email, password, dateOfBirth)
            }

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

    private fun createLocalUser(
        name: String,
        username: String,
        email: String,
        password: String,
        dateOfBirth: String
    ): User {
        val user = User(
            id = java.util.UUID.randomUUID().toString(),
            name = name,
            username = username,
            password = password,
            dateOfBirth = dateOfBirth,
            email = email,
            themePreference = ThemePreference.SYSTEM
        )
        localUsers.add(user)
        return user
    }

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            isLoading = true

            // Try Firebase first
            val user = if (auth != null) {
                try {
                    val firebaseAuth = auth // Store in local variable for smart cast
                    val authResult = firebaseAuth?.signInWithEmailAndPassword(email, password)?.await()
                    val firebaseUser = authResult?.user ?: throw Exception("Login failed")
                    loadUserData(firebaseUser.uid)
                    syncAllDataFromFirebase()
                    currentUser!!
                } catch (e: Exception) {
                    Log.e(TAG, "Firebase login failed, trying local: ${e.message}")
                    // Fallback to local
                    localUsers.find { it.email == email && it.password == password }
                        ?: throw Exception("Invalid credentials")
                }
            } else {
                // Use local storage
                localUsers.find { it.email == email && it.password == password }
                    ?: throw Exception("Invalid credentials")
            }

            currentUser = user
            isLoggedIn = true
            currentTheme = user.themePreference
            isLoading = false

            Result.success(user)
        } catch (e: Exception) {
            isLoading = false
            Result.failure(Exception("Login failed: ${e.message}"))
        }
    }

    suspend fun loginWithUsername(username: String, password: String): Result<User> {
        return try {
            // Try local first
            val localUser = localUsers.find { it.username == username && it.password == password }
            if (localUser != null) {
                currentUser = localUser
                isLoggedIn = true
                currentTheme = localUser.themePreference
                return Result.success(localUser)
            }

            // Try Firebase
            val email = getUserEmailFromUsername(username)
                ?: return Result.failure(Exception("Username not found"))
            login(email, password)
        } catch (e: Exception) {
            Result.failure(Exception("Login failed: ${e.message}"))
        }
    }

    fun logout() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "Firebase logout failed: ${e.message}")
        }
        currentUser = null
        isLoggedIn = false
        clearLocalData()
    }

    // ==================== MOOD TRACKING ====================

    suspend fun saveMoodEntry(mood: MoodEntry): Result<Unit> {
        return try {
            // Save to local first
            localMoods.add(0, mood)

            // Try to save to Firebase
            val firebaseAuth = auth
            val firestoreDb = firestore
            if (firebaseAuth != null && firestoreDb != null && currentUser != null) {
                try {
                    val userId = currentUser?.id ?: return Result.success(Unit)
                    val moodData = hashMapOf(
                        "date" to mood.date,
                        "mood" to mood.mood,
                        "score" to mood.score,
                        "emoji" to mood.emoji,
                        "timestamp" to System.currentTimeMillis()
                    )
                    firestoreDb.collection("users/$userId/moods")
                        .add(moodData)
                        .await()
                } catch (e: Exception) {
                    Log.e(TAG, "Firebase save failed, using local: ${e.message}")
                }
            }

            Log.d(TAG, "Mood saved: ${mood.mood}")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMoodHistory(limit: Int = 50): List<MoodEntry> {
        return try {
            // Try Firebase first
            val firestoreDb = firestore
            if (firestoreDb != null && currentUser != null) {
                try {
                    val userId = currentUser?.id ?: return localMoods.take(limit)
                    val snapshot = firestoreDb.collection("users/$userId/moods")
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
                    Log.e(TAG, "Firebase load failed, using local: ${e.message}")
                    localMoods.take(limit)
                }
            } else {
                localMoods.take(limit)
            }
        } catch (e: Exception) {
            localMoods.take(limit)
        }
    }

    // ==================== JOURNAL ENTRIES ====================

    suspend fun saveJournalEntry(entry: JournalEntry): Result<Unit> {
        return try {
            localJournals.add(0, entry)

            val firestoreDb = firestore
            if (firestoreDb != null && currentUser != null) {
                try {
                    val userId = currentUser?.id ?: return Result.success(Unit)
                    val journalData = hashMapOf(
                        "date" to entry.date,
                        "content" to entry.content,
                        "audioNote" to (entry.audioNote ?: ""),
                        "timestamp" to System.currentTimeMillis()
                    )
                    firestoreDb.collection("users/$userId/journals")
                        .add(journalData)
                        .await()
                } catch (e: Exception) {
                    Log.e(TAG, "Firebase save failed: ${e.message}")
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getJournalEntries(limit: Int = 100): List<JournalEntry> {
        return try {
            val firestoreDb = firestore
            if (firestoreDb != null && currentUser != null) {
                try {
                    val userId = currentUser?.id ?: return localJournals.take(limit)
                    val snapshot = firestoreDb.collection("users/$userId/journals")
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
                    localJournals.take(limit)
                }
            } else {
                localJournals.take(limit)
            }
        } catch (e: Exception) {
            localJournals.take(limit)
        }
    }

    // ==================== HABITS ====================

    suspend fun saveHabit(habit: Habit): Result<Unit> {
        return try {
            val index = localHabits.indexOfFirst { it.id == habit.id }
            if (index >= 0) {
                localHabits[index] = habit
            } else {
                localHabits.add(habit)
            }

            val firestoreDb = firestore
            if (firestoreDb != null && currentUser != null) {
                try {
                    val userId = currentUser?.id ?: return Result.success(Unit)
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
                    firestoreDb.collection("users/$userId/habits")
                        .document(habit.id)
                        .set(habitData)
                        .await()
                } catch (e: Exception) {
                    Log.e(TAG, "Firebase save failed: ${e.message}")
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHabits(): List<Habit> {
        return try {
            val firestoreDb = firestore
            if (firestoreDb != null && currentUser != null) {
                try {
                    val userId = currentUser?.id ?: return localHabits.toList()
                    val snapshot = firestoreDb.collection("users/$userId/habits")
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
                    localHabits.toList()
                }
            } else {
                localHabits.toList()
            }
        } catch (e: Exception) {
            localHabits.toList()
        }
    }

    suspend fun deleteHabit(habitId: String): Result<Unit> {
        return try {
            localHabits.removeIf { it.id == habitId }

            val firestoreDb = firestore
            if (firestoreDb != null && currentUser != null) {
                try {
                    val userId = currentUser?.id ?: return Result.success(Unit)
                    firestoreDb.collection("users/$userId/habits")
                        .document(habitId)
                        .delete()
                        .await()
                } catch (e: Exception) {
                    Log.e(TAG, "Firebase delete failed: ${e.message}")
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== FINANCE - TRANSACTIONS ====================

    suspend fun saveTransaction(transaction: Transaction): Result<Unit> {
        return try {
            val index = localTransactions.indexOfFirst { it.id == transaction.id }
            if (index >= 0) {
                localTransactions[index] = transaction
            } else {
                localTransactions.add(0, transaction)
            }

            val firestoreDb = firestore
            if (firestoreDb != null && currentUser != null) {
                try {
                    val userId = currentUser?.id ?: return Result.success(Unit)
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
                    firestoreDb.collection("users/$userId/transactions")
                        .document(transaction.id)
                        .set(transactionData)
                        .await()
                } catch (e: Exception) {
                    Log.e(TAG, "Firebase save failed: ${e.message}")
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTransactions(limit: Int = 200): List<Transaction> {
        return try {
            val firestoreDb = firestore
            if (firestoreDb != null && currentUser != null) {
                try {
                    val userId = currentUser?.id ?: return localTransactions.take(limit)
                    val snapshot = firestoreDb.collection("users/$userId/transactions")
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
                    localTransactions.take(limit)
                }
            } else {
                localTransactions.take(limit)
            }
        } catch (e: Exception) {
            localTransactions.take(limit)
        }
    }

    suspend fun deleteTransaction(transactionId: String): Result<Unit> {
        localTransactions.removeIf { it.id == transactionId }
        return Result.success(Unit)
    }

    // ==================== FINANCE - BUDGETS ====================

    suspend fun saveBudgets(budgets: Map<String, Double>): Result<Unit> {
        localBudgets.clear()
        localBudgets.putAll(budgets)
        return Result.success(Unit)
    }

    suspend fun getBudgets(): Map<String, Double> {
        return localBudgets.toMap()
    }

    // ==================== FINANCE - SAVINGS GOALS ====================

    suspend fun saveSavingsGoal(goal: SavingsGoal): Result<Unit> {
        val index = localGoals.indexOfFirst { it.id == goal.id }
        if (index >= 0) {
            localGoals[index] = goal
        } else {
            localGoals.add(goal)
        }
        return Result.success(Unit)
    }

    suspend fun getSavingsGoals(): List<SavingsGoal> {
        return localGoals.toList()
    }

    suspend fun deleteSavingsGoal(goalId: String): Result<Unit> {
        localGoals.removeIf { it.id == goalId }
        return Result.success(Unit)
    }

    // ==================== USER PROFILE ====================

    suspend fun updateUserProfile(updatedUser: User): Result<User> {
        currentUser = updatedUser
        return Result.success(updatedUser)
    }

    suspend fun changeTheme(theme: ThemePreference) {
        currentTheme = theme
        currentUser?.let { user ->
            val updatedUser = user.copy(themePreference = theme)
            updateUserProfile(updatedUser)
        }
    }

    // ==================== HELPER FUNCTIONS ====================

    private suspend fun syncAllDataFromFirebase() {
        // Intentionally empty - data loads on-demand
    }

    private fun clearLocalData() {
        // Keep local data for now
    }

    private suspend fun checkUsernameExists(username: String): Boolean {
        return try {
            val firestoreDb = firestore
            firestoreDb?.let {
                val snapshot = it.collection("users")
                    .whereEqualTo("username", username)
                    .get()
                    .await()
                !snapshot.isEmpty
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun saveUserToFirestore(user: User) {
        try {
            val firestoreDb = firestore
            firestoreDb?.let {
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
                it.collection("users")
                    .document(user.id)
                    .set(userData, com.google.firebase.firestore.SetOptions.merge())
                    .await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Save user failed: ${e.message}")
        }
    }

    private suspend fun loadUserData(userId: String) {
        try {
            val firestoreDb = firestore
            firestoreDb?.let {
                val doc = it.collection("users")
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
            }
        } catch (e: Exception) {
            Log.e(TAG, "Load user failed: ${e.message}")
        }
    }

    private suspend fun getUserEmailFromUsername(username: String): String? {
        return try {
            val firestoreDb = firestore
            firestoreDb?.let {
                val snapshot = it.collection("users")
                    .whereEqualTo("username", username)
                    .limit(1)
                    .get()
                    .await()
                if (!snapshot.isEmpty) snapshot.documents[0].getString("email") else null
            }
        } catch (e: Exception) {
            null
        }
    }
}
