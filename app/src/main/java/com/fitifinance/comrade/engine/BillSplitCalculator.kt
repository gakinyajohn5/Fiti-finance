package com.fitifinance.comrade.engine

data class BillSplitResult(
    val personalShareKes: Double,
    val pendingReceivableKes: Double
)

/**
 * Full Payment: entire transaction deducted from Night-Out Budget.
 * Split Bill: input total bill + number of comrades -> deducts only the
 * personal share, and logs the remainder as "Pending Receivables".
 */
object BillSplitCalculator {

    fun fullPayment(totalBillKes: Double): BillSplitResult =
        BillSplitResult(personalShareKes = totalBillKes, pendingReceivableKes = 0.0)

    fun splitBill(totalBillKes: Double, numberOfComrades: Int): BillSplitResult {
        require(numberOfComrades >= 1) { "Number of comrades must be at least 1" }
        val personalShare = totalBillKes / numberOfComrades
        val receivable = totalBillKes - personalShare
        return BillSplitResult(
            personalShareKes = roundToCents(personalShare),
            pendingReceivableKes = roundToCents(receivable)
        )
    }

    private fun roundToCents(value: Double): Double = Math.round(value * 100.0) / 100.0
}

/**
 * Emergency Fare Shield: locks away a fixed ride-home reserve so it can't be
 * spent on drinks. Returns the amount actually spendable from the night-out
 * budget after protecting the fare reserve.
 */
object FareShield {
    fun spendableBalance(nightOutBudgetKes: Double, fareShieldKes: Double): Double =
        (nightOutBudgetKes - fareShieldKes).coerceAtLeast(0.0)

    fun wouldBreachShield(currentSpendKes: Double, nightOutBudgetKes: Double, fareShieldKes: Double): Boolean =
        currentSpendKes > spendableBalance(nightOutBudgetKes, fareShieldKes)
}
