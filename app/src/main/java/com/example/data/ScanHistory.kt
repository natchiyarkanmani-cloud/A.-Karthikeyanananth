package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_history")
data class ScanHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val barcodeOrName: String,
    val productName: String,
    val preservatives: String,
    val addedIngredients: String,
    val fssaiRating: String,
    val safetyAdvice: String,
    val timestamp: Long = System.currentTimeMillis()
)
