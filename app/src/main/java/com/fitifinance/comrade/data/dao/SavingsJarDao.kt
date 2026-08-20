package com.fitifinance.comrade.data.dao

import androidx.room.*
import com.fitifinance.comrade.data.entity.SavingsJar
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsJarDao {
    @Insert
    suspend fun insert(jar: SavingsJar): Long

    @Update
    suspend fun update(jar: SavingsJar)

    @Delete
    suspend fun delete(jar: SavingsJar)

    @Query("SELECT * FROM savings_jars WHERE isActive = 1 ORDER BY priority ASC")
    fun observeActiveJars(): Flow<List<SavingsJar>>

    @Query("SELECT * FROM savings_jars WHERE isActive = 1 ORDER BY priority ASC")
    suspend fun getActiveJars(): List<SavingsJar>
}
