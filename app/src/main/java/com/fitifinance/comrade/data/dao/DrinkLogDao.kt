package com.fitifinance.comrade.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.fitifinance.comrade.data.entity.DrinkLog
import kotlinx.coroutines.flow.Flow

@Dao
interface DrinkLogDao {
    @Insert
    suspend fun insert(drinkLog: DrinkLog): Long

    @Query("SELECT * FROM drink_logs ORDER BY timestampMillis DESC")
    fun observeAll(): Flow<List<DrinkLog>>

    @Query("SELECT * FROM drink_logs WHERE timestampMillis BETWEEN :startMillis AND :endMillis ORDER BY timestampMillis DESC")
    fun observeBetween(startMillis: Long, endMillis: Long): Flow<List<DrinkLog>>

    @Query("SELECT COALESCE(SUM(personalShareKes), 0) FROM drink_logs WHERE timestampMillis BETWEEN :startMillis AND :endMillis")
    fun observeSpentBetween(startMillis: Long, endMillis: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(pendingReceivableKes), 0) FROM drink_logs")
    fun observeTotalPendingReceivables(): Flow<Double>
}
