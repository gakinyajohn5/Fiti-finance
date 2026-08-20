package com.fitifinance.comrade.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionCategory {
    FOOD,
    GROCERIES,
    UTILITIES,
    RENT,
    ENTERTAINMENT,
    NIGHT_OUT,
    FAMILY_BLACK_TAX,
    SHARED_HOUSE_EXPENSES,
    LOAN_DEBT_REPAYMENT,
    CHAMA_SOCIAL,
    UNCATEGORIZED
}

enum class TransactionSource {
    PAYBILL,
    TILL_BUY_GOODS,
    POCHI_LA_BIASHARA,
    PEER_TO_PEER,
    MANUAL
}

/**
 * A single money-out event, whether auto-parsed from an M-PESA SMS or logged
 * manually (e.g. a meal card tap or a night-out drink).
 */
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountKes: Double,
    val counterparty: String,
    val category: TransactionCategory,
    val source: TransactionSource,
    val timestampMillis: Long,
    val rawSmsBody: String? = null,
    val needsUserPrompt: Boolean = false, // true right after P2P parse, until user categorizes
    val note: String? = null
)
