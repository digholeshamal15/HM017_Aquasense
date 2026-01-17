// UserDataModels.kt
// Create this file in: app/src/main/java/com/example/health/

package com.example.health

/**
 * Data models for User Authentication and Profile
 */

data class User(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val username: String,
    val password: String,
    val dateOfBirth: String,
    val email: String = "",
    val profileImageUrl: String = "",
    val themePreference: ThemePreference = ThemePreference.SYSTEM
)

enum class ThemePreference {
    LIGHT,
    DARK,
    SYSTEM
}

data class UserSession(
    val user: User,
    val isLoggedIn: Boolean = true,
    val loginTime: Long = System.currentTimeMillis()
)

// User preferences for app settings
data class AppSettings(
    val theme: ThemePreference = ThemePreference.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val biometricEnabled: Boolean = false,
    val language: String = "English"
)