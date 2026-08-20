package com.fitifinance.comrade.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drink_logs")
data class DrinkLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val drinkId: String,
    val drinkName: String,
    val totalBillKes: Double,
    val isSplit: Boolean,
    val splitCount: Int = 1,
    val personalShareKes: Double,
    val pendingReceivableKes: Double = 0.0, // amount owed back to the user by comrades
    val timestampMillis: Long
)
