package com.fitifinance.comrade.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fitifinance.comrade.data.entity.Recipient
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(recipient: Recipient)

    @Query("SELECT * FROM recipients WHERE fullName = :name LIMIT 1")
    suspend fun findByName(name: String): Recipient?

    @Query("SELECT * FROM recipients ORDER BY fullName ASC")
    fun observeAll(): Flow<List<Recipient>>
}
