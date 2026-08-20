package com.fitifinance.comrade.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * "Always remember this rule for [Name]" — lets P2P transfers to a saved
 * name skip the categorization prompt sheet on future transactions.
 */
@Entity(tableName = "recipients")
data class Recipient(
    @PrimaryKey val fullName: String,
    val defaultCategory: TransactionCategory,
    val alwaysRemember: Boolean = true
)
