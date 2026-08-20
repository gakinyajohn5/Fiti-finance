package com.fitifinance.comrade.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.fitifinance.comrade.data.entity.MealLog
import kotlinx.coroutines.flow.Flow

@Dao
interface MealLogDao {
    @Insert
    suspend fun insert(mealLog: MealLog): Long

    @Query("SELECT * FROM meal_logs WHERE timestampMillis BETWEEN :startMillis AND :endMillis ORDER BY timestampMillis DESC")
    fun observeBetween(startMillis: Long, endMillis: Long): Flow<List<MealLog>>

    @Query("SELECT COALESCE(SUM(costKes), 0) FROM meal_logs WHERE timestampMillis BETWEEN :startMillis AND :endMillis")
    fun observeTotalSpentBetween(startMillis: Long, endMillis: Long): Flow<Double>
}
