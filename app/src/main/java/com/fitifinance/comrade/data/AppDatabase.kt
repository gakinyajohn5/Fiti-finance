package com.fitifinance.comrade.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fitifinance.comrade.data.dao.*
import com.fitifinance.comrade.data.entity.*

@Database(
    entities = [
        UserProfile::class,
        Transaction::class,
        MealLog::class,
        DrinkLog::class,
        SavingsJar::class,
        Recipient::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun transactionDao(): TransactionDao
    abstract fun mealLogDao(): MealLogDao
    abstract fun drinkLogDao(): DrinkLogDao
    abstract fun savingsJarDao(): SavingsJarDao
    abstract fun recipientDao(): RecipientDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fiti_finance.db"
                ).build().also { INSTANCE = it }
            }
    }
}
