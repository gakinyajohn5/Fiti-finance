package com.fitifinance.comrade.sms

import com.fitifinance.comrade.data.entity.TransactionSource

/**
 * Result of parsing a raw M-PESA SMS notification body.
 */
data class ParsedMpesaTransaction(
    val amountKes: Double,
    val counterparty: String,
    val source: TransactionSource,
    val accountReference: String? = null
)

/**
 * Reads incoming M-PESA transaction SMS text and extracts amount + counterparty
 * + transaction type, mirroring real Safaricom M-PESA confirmation formats:
 *
 *   Paybill:  "...Ksh500.00 sent to KPLC PREPAID for account 12345678 on..."
 *   Till:     "...Ksh200.00 paid to JAVA HOUSE KIMATHI. on..."
 *   Pochi:    "...Ksh150.00 sent to JOHN KAMAU for Pochi la Biashara on..."
 *   P2P:      "...Ksh500.00 sent to JANE WANJIRU 0712***678 on..."
 */
object SmsParser {

    // Ksh500.00 / Ksh 1,200.50 / KES500
    private val amountRegex = Regex("""Ksh?\.?\s?([\d,]+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE)

    private val paybillRegex = Regex(
        """(?:sent to|paid to)\s+([A-Z0-9 &.'-]+?)\s+for account\s+([A-Za-z0-9\-/]+)""",
        RegexOption.IGNORE_CASE
    )

    private val pochiRegex = Regex(
        """sent to\s+([A-Z ]+?)\s+for Pochi la Biashara""",
        RegexOption.IGNORE_CASE
    )

    private val tillRegex = Regex(
        """paid to\s+([A-Z0-9 &.'-]+?)\.?\s+on\s""",
        RegexOption.IGNORE_CASE
    )

    // Peer-to-peer: "sent to JANE WANJIRU 0712345678 on" (has a phone number, no "for account")
    private val p2pRegex = Regex(
        """sent to\s+([A-Za-z ]+?)\s+(\d{9,12})\s+on\s""",
        RegexOption.IGNORE_CASE
    )

    fun isMpesaTransaction(body: String): Boolean =
        body.contains("Confirmed", ignoreCase = true) &&
            (body.contains("sent to", true) || body.contains("paid to", true))

    fun parse(body: String): ParsedMpesaTransaction? {
        if (!isMpesaTransaction(body)) return null

        val amount = amountRegex.find(body)
            ?.groupValues?.get(1)
            ?.replace(",", "")
            ?.toDoubleOrNull() ?: return null

        // Order matters: check most specific patterns first.
        paybillRegex.find(body)?.let { m ->
            return ParsedMpesaTransaction(
                amountKes = amount,
                counterparty = m.groupValues[1].trim(),
                source = TransactionSource.PAYBILL,
                accountReference = m.groupValues[2].trim()
            )
        }

        pochiRegex.find(body)?.let { m ->
            return ParsedMpesaTransaction(
                amountKes = amount,
                counterparty = m.groupValues[1].trim(),
                source = TransactionSource.POCHI_LA_BIASHARA
            )
        }

        p2pRegex.find(body)?.let { m ->
            return ParsedMpesaTransaction(
                amountKes = amount,
                counterparty = m.groupValues[1].trim(),
                source = TransactionSource.PEER_TO_PEER
            )
        }

        tillRegex.find(body)?.let { m ->
            return ParsedMpesaTransaction(
                amountKes = amount,
                counterparty = m.groupValues[1].trim(),
                source = TransactionSource.TILL_BUY_GOODS
            )
        }

        return null
    }
}
