// UserManager.kt
// Create this file in: app/src/main/java/com/example/health/

package com.example.health

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * Singleton class to manage user authentication and session
 */
object UserManager {
    // In-memory storage (replace with actual database in production)
    private val users = mutableListOf<User>()

    var currentUser by mutableStateOf<User?>(null)
        private set

    var isLoggedIn by mutableStateOf(false)
        private set

    var currentTheme by mutableStateOf(ThemePreference.SYSTEM)
        private set

    init {
        // Add a demo user for testing
        users.add(
            User(
                name = "Demo User",
                username = "demo",
                password = "demo123",
                dateOfBirth = "1990-01-01",
                email = "demo@example.com"
            )
        )
    }

    fun register(name: String, username: String, password: String, dateOfBirth: String, email: String): Result<User> {
        // Check if username already exists
        if (users.any { it.username == username }) {
            return Result.failure(Exception("Username already exists"))
        }

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

        // Create new user
        val newUser = User(
            name = name,
            username = username,
            password = password,
            dateOfBirth = dateOfBirth,
            email = email
        )

        users.add(newUser)
        return Result.success(newUser)
    }

    fun login(username: String, password: String): Result<User> {
        val user = users.find { it.username == username && it.password == password }

        return if (user != null) {
            currentUser = user
            isLoggedIn = true
            currentTheme = user.themePreference
            Result.success(user)
        } else {
            Result.failure(Exception("Invalid username or password"))
        }
    }

    fun logout() {
        currentUser = null
        isLoggedIn = false
    }

    fun updateUserProfile(updatedUser: User): Result<User> {
        val index = users.indexOfFirst { it.id == updatedUser.id }
        if (index != -1) {
            users[index] = updatedUser
            if (currentUser?.id == updatedUser.id) {
                currentUser = updatedUser
            }
            return Result.success(updatedUser)
        }
        return Result.failure(Exception("User not found"))
    }

    fun changeTheme(theme: ThemePreference) {
        currentTheme = theme
        currentUser?.let { user ->
            val updatedUser = user.copy(themePreference = theme)
            updateUserProfile(updatedUser)
        }
    }

    fun getAllUsers(): List<User> = users.toList()
}