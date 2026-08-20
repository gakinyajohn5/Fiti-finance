package com.fitifinance.comrade.sms

import com.fitifinance.comrade.data.entity.TransactionCategory
import com.fitifinance.comrade.data.entity.TransactionSource

/**
 * Auto-categorization logic (Paybill / Till / Pochi -> no prompt needed).
 * P2P transfers are deliberately NOT auto-categorized here — they trigger
 * the interactive bottom-sheet prompt instead (see TransactionsViewModel).
 */
object TransactionCategorizer {

    private val foodKeywords = listOf("kibanda", "java", "chicken", "eatery", "hotel", "cafe", "restaurant", "smocha")
    private val groceryKeywords = listOf("mama mboga", "market", "supermarket", "naivas", "quickmart", "carrefour", "duka")
    private val utilityKeywords = listOf("kplc", "prepaid", "water", "wifi", "safaricom data", "zuku")
    private val rentKeywords = listOf("rent", "landlord", "hostel fee", "bedsitter")
    private val entertainmentKeywords = listOf("bar", "club", "pub", "lounge", "liquor", "wines", "spirits")

    fun categorize(parsed: ParsedMpesaTransaction): TransactionCategory {
        val merchant = parsed.counterparty.lowercase()

        return when (parsed.source) {
            TransactionSource.POCHI_LA_BIASHARA -> TransactionCategory.GROCERIES // micro-vendors: mama mboga / duka
            TransactionSource.PAYBILL, TransactionSource.TILL_BUY_GOODS -> {
                when {
                    entertainmentKeywords.any { merchant.contains(it) } -> TransactionCategory.ENTERTAINMENT
                    rentKeywords.any { merchant.contains(it) } -> TransactionCategory.RENT
                    utilityKeywords.any { merchant.contains(it) } -> TransactionCategory.UTILITIES
                    groceryKeywords.any { merchant.contains(it) } -> TransactionCategory.GROCERIES
                    foodKeywords.any { merchant.contains(it) } -> TransactionCategory.FOOD
                    else -> TransactionCategory.UNCATEGORIZED
                }
            }
            TransactionSource.PEER_TO_PEER -> TransactionCategory.UNCATEGORIZED // resolved via prompt sheet
            TransactionSource.MANUAL -> TransactionCategory.UNCATEGORIZED
        }
    }

    /** Only P2P transfers require the "What was this for?" bottom sheet. */
    fun requiresPrompt(source: TransactionSource): Boolean = source == TransactionSource.PEER_TO_PEER
}
