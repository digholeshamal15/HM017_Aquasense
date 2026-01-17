package com.example.health

// Data Classes for Wellness Features
data class MoodEntry(
    val date: String,
    val mood: String,
    val score: Int,
    val emoji: String
)

data class JournalEntry(
    val date: String,
    val content: String,
    val audioNote: String? = null
)

data class WeeklyProgress(
    val lastWeek: Int,
    val thisWeek: Int,
    val improvement: Int
)
