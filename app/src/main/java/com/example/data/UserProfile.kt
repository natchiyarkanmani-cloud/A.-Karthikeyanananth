package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Single-row profile table
    val gender: String,
    val age: Int,
    val healthIssues: String, // Comma-separated list or custom text description
    val height: Double, // in cm
    val weight: Double // in kg
)
