package com.fitifinance.comrade.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.fitifinance.comrade.FitiApplication
import com.fitifinance.comrade.data.entity.Transaction
import com.fitifinance.comrade.data.entity.TransactionSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Listens for android.provider.Telephony.SMS_RECEIVED, filters for M-PESA
 * confirmation texts, and either auto-logs the transaction (Paybill / Till /
 * Pochi) or stores it flagged `needsUserPrompt = true` (Peer-to-Peer) so the
 * UI can show the "What was this for?" bottom sheet on next app open.
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val fullBody = messages.joinToString(separator = "") { it.messageBody ?: "" }
        val sender = messages.firstOrNull()?.originatingAddress ?: ""

        // M-PESA sender IDs are typically "MPESA" — still fall back to content sniffing.
        if (!sender.contains("MPESA", ignoreCase = true) && !SmsParser.isMpesaTransaction(fullBody)) {
            return
        }

        val parsed = SmsParser.parse(fullBody) ?: return
        val app = context.applicationContext as? FitiApplication ?: return
        val repo = app.repository

        CoroutineScope(Dispatchers.IO).launch {
            val category = TransactionCategorizer.categorize(parsed)
            val needsPrompt = TransactionCategorizer.requiresPrompt(parsed.source)

            // If this P2P recipient already has a remembered rule, auto-apply it.
            val resolvedCategory = if (needsPrompt) {
                repo.findRecipientRule(parsed.counterparty)?.defaultCategory ?: category
            } else category

            val stillNeedsPrompt = needsPrompt &&
                repo.findRecipientRule(parsed.counterparty) == null

            repo.insertTransaction(
                Transaction(
                    amountKes = parsed.amountKes,
                    counterparty = parsed.counterparty,
                    category = resolvedCategory,
                    source = parsed.source,
                    timestampMillis = System.currentTimeMillis(),
                    rawSmsBody = fullBody,
                    needsUserPrompt = stillNeedsPrompt
                )
            )

            // Real-time deduction against the relevant budget (meal / night-out / etc.)
            repo.applySpendToBudget(resolvedCategory, parsed.amountKes)
        }
    }
}
