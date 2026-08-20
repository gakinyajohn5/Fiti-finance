package com.fitifinance.comrade.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MealWindow { BREAKFAST, LUNCH, SUPPER, LATE_NIGHT }

@Entity(tableName = "meal_logs")
data class MealLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealItemId: String,
    val mealName: String,
    val costKes: Double,
    val window: MealWindow,
    val vendorTag: String,
    val timestampMillis: Long
)
