package com.fitifinance.comrade.data.dao

import androidx.room.*
import com.fitifinance.comrade.data.entity.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY timestampMillis DESC")
    fun observeAll(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE needsUserPrompt = 1 ORDER BY timestampMillis DESC")
    fun observePendingPrompts(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE timestampMillis BETWEEN :startMillis AND :endMillis ORDER BY timestampMillis DESC")
    fun observeBetween(startMillis: Long, endMillis: Long): Flow<List<Transaction>>

    @Query("SELECT COALESCE(SUM(amountKes), 0) FROM transactions WHERE category = :category AND timestampMillis BETWEEN :startMillis AND :endMillis")
    suspend fun totalForCategoryBetween(category: String, startMillis: Long, endMillis: Long): Double
}
