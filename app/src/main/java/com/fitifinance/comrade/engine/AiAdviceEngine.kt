package com.fitifinance.comrade.engine

import com.fitifinance.comrade.data.entity.SavingsJar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/** A single piece of AI-generated financial advice shown on the Savings screen. */
data class AdviceResult(val message: String, val isFromCloud: Boolean)

interface AiAdviceEngine {
    suspend fun purchasingAdvice(surplusKes: Double, jars: List<SavingsJar>): AdviceResult
    suspend fun spendingSpeedNudge(weekendSpendKes: Double, monthlyBudgetRemainingKes: Double, daysLeftInMonth: Int): AdviceResult?
}

/**
 * On-device heuristic engine — mirrors the "Gemini Nano parses locally for
 * total privacy" behavior from the blueprint. Used as the default engine and
 * as a fallback when there's no network connectivity for the cloud engine.
 */
class LocalHeuristicAdviceEngine : AiAdviceEngine {

    override suspend fun purchasingAdvice(surplusKes: Double, jars: List<SavingsJar>): AdviceResult {
        if (surplusKes <= 0) {
            return AdviceResult("No surplus right now — stay the course on your current jars.", false)
        }
        val topJar = jars.filter { it.isActive }.minByOrNull { it.priority }
        val message = if (topJar != null) {
            val room = topJar.targetAmountKes - topJar.currentAmountKes
            if (surplusKes >= room && room > 0) {
                "You have KES ${surplusKes.toInt()} surplus. That fully funds your ${topJar.goalName} goal today!"
            } else {
                "You have KES ${surplusKes.toInt()} surplus. Fund ${surplusKes.toInt()} KES toward your " +
                    "${topJar.goalName} goal, or reserve it as ${(surplusKes / 300).toInt()} days of meal money."
            }
        } else {
            "You have KES ${surplusKes.toInt()} surplus. Consider starting a savings jar for your next goal."
        }
        return AdviceResult(message, false)
    }

    override suspend fun spendingSpeedNudge(
        weekendSpendKes: Double,
        monthlyBudgetRemainingKes: Double,
        daysLeftInMonth: Int
    ): AdviceResult? {
        if (daysLeftInMonth <= 0) return null
        val safeDailyRate = monthlyBudgetRemainingKes / daysLeftInMonth
        val impliedMonthlyRate = weekendSpendKes / 2.0 // weekend = 2 days, rough projection
        return if (impliedMonthlyRate > safeDailyRate * 1.5) {
            AdviceResult(
                "Heads up — this weekend's spending pace is running ahead of your budget. " +
                    "At this rate you could run a deficit before month-end. Consider dialing back.",
                false
            )
        } else null
    }
}

/**
 * Cloud-backed engine using the Gemini API for richer natural-language
 * advice. Requires a `GEMINI_API_KEY` to be supplied at runtime (e.g. via
 * BuildConfig / a secure local.properties value) — never hardcode it.
 */
class GeminiAdviceEngine(private val apiKey: String?) : AiAdviceEngine {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val fallback = LocalHeuristicAdviceEngine()
    private val jsonMediaType = "application/json".toMediaType()

    @Serializable
    private data class GeminiPart(val text: String)
    @Serializable
    private data class GeminiContent(val parts: List<GeminiPart>)
    @Serializable
    private data class GeminiRequest(val contents: List<GeminiContent>)

    override suspend fun purchasingAdvice(surplusKes: Double, jars: List<SavingsJar>): AdviceResult {
        if (apiKey.isNullOrBlank()) return fallback.purchasingAdvice(surplusKes, jars)

        val jarSummary = jars.joinToString("; ") { "${it.goalName}: KES ${it.currentAmountKes.toInt()}/${it.targetAmountKes.toInt()}" }
        val prompt = "A Kenyan university student (comrade) has a surplus of KES ${surplusKes.toInt()} today. " +
            "Their active savings jars are: $jarSummary. Give one short, practical, encouraging suggestion " +
            "(max 2 sentences) on whether to fund a jar or hold the money as a meal-budget buffer."

        return runCatching { callGemini(prompt) }
            .map { AdviceResult(it, true) }
            .getOrElse { fallback.purchasingAdvice(surplusKes, jars) }
    }

    override suspend fun spendingSpeedNudge(
        weekendSpendKes: Double,
        monthlyBudgetRemainingKes: Double,
        daysLeftInMonth: Int
    ): AdviceResult? {
        if (apiKey.isNullOrBlank()) return fallback.spendingSpeedNudge(weekendSpendKes, monthlyBudgetRemainingKes, daysLeftInMonth)

        val prompt = "A student spent KES ${weekendSpendKes.toInt()} this weekend. They have KES " +
            "${monthlyBudgetRemainingKes.toInt()} left in their monthly budget with $daysLeftInMonth days remaining. " +
            "If their spending pace risks a deficit, give one short warning (max 2 sentences). " +
            "If they're fine, reply with exactly: OK"

        val result = runCatching { callGemini(prompt) }.getOrElse {
            return fallback.spendingSpeedNudge(weekendSpendKes, monthlyBudgetRemainingKes, daysLeftInMonth)
        }
        return if (result.trim().equals("OK", ignoreCase = true)) null else AdviceResult(result, true)
    }

    private suspend fun callGemini(prompt: String): String = withContext(Dispatchers.IO) {
        val body = Json.encodeToString(
            GeminiRequest.serializer(),
            GeminiRequest(contents = listOf(GeminiContent(parts = listOf(GeminiPart(prompt)))))
        )

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
            .post(body.toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Gemini API call failed: ${response.code}")
            val text = response.body?.string().orEmpty()
            extractFirstText(text)
        }
    }

    /** Minimal, dependency-free extraction of the first candidate's text from the Gemini response JSON. */
    private fun extractFirstText(rawJson: String): String {
        val marker = "\"text\""
        val idx = rawJson.indexOf(marker)
        if (idx == -1) error("Unexpected Gemini response shape")
        val colon = rawJson.indexOf(':', idx)
        val startQuote = rawJson.indexOf('"', colon + 1)
        val endQuote = rawJson.indexOf('"', startQuote + 1)
        if (startQuote == -1 || endQuote == -1) error("Unexpected Gemini response shape")
        return rawJson.substring(startQuote + 1, endQuote)
            .replace("\\n", "\n")
            .replace("\\\"", "\"")
    }
}

/**
 * Conversational Goal Editing — parses natural text like
 * "Put KES 500 in my Laptop fund and KES 200 in food" into discrete jar
 * top-up commands, purely on-device (no network needed).
 */
data class JarTopUpCommand(val jarNameFragment: String, val amountKes: Double)

object GoalEditingParser {
    private val clauseRegex = Regex(
        """(?:ksh?|kes)?\s*(\d+(?:\.\d+)?)\s*(?:ksh?|kes)?\s+(?:in|to|towards|toward)\s+(?:my\s+)?([a-zA-Z ]+?)(?:\s+fund|\s+jar)?(?=(?:,|and|$))""",
        RegexOption.IGNORE_CASE
    )

    fun parse(text: String): List<JarTopUpCommand> =
        clauseRegex.findAll(text).mapNotNull { match ->
            val amount = match.groupValues[1].toDoubleOrNull() ?: return@mapNotNull null
            val name = match.groupValues[2].trim()
            if (name.isBlank()) null else JarTopUpCommand(name, amount)
        }.toList()
}
