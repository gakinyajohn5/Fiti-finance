package com.fitifinance.comrade.engine

import com.fitifinance.comrade.data.entity.SavingsJar

data class AllocationPlan(
    val updatedJars: List<SavingsJar>,
    val unallocatedRemainderKes: Double
)

/**
 * Automatically directs surplus daily meal/expense funds into active goal
 * jars based on priority (lowest priority number fills first), stopping
 * once a jar reaches its target.
 */
object SavingsAllocator {

    fun allocateSurplus(surplusKes: Double, jars: List<SavingsJar>): AllocationPlan {
        if (surplusKes <= 0.0 || jars.isEmpty()) {
            return AllocationPlan(jars, surplusKes.coerceAtLeast(0.0))
        }

        var remaining = surplusKes
        val ordered = jars.sortedBy { it.priority }
        val updated = mutableListOf<SavingsJar>()

        for (jar in ordered) {
            if (remaining <= 0.0) {
                updated += jar
                continue
            }
            val room = (jar.targetAmountKes - jar.currentAmountKes).coerceAtLeast(0.0)
            val allocation = minOf(room, remaining)
            remaining -= allocation
            updated += jar.copy(currentAmountKes = jar.currentAmountKes + allocation)
        }

        // Preserve original ordering for stable UI display.
        val byId = updated.associateBy { it.id }
        val reordered = jars.map { byId[it.id] ?: it }

        return AllocationPlan(reordered, remaining)
    }
}
