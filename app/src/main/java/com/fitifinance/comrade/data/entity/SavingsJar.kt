package com.fitifinance.comrade.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_jars")
data class SavingsJar(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val goalName: String,
    val targetAmountKes: Double,
    val currentAmountKes: Double = 0.0,
    val targetDateMillis: Long? = null,
    val priority: Int = 1, // lower number = higher priority for auto-allocation
    val isActive: Boolean = true
) {
    val progressFraction: Float
        get() = if (targetAmountKes <= 0) 0f else (currentAmountKes / targetAmountKes).toFloat().coerceIn(0f, 1f)
}
