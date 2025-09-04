package com.example.smartexpensetracker.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object Formatters {
    private val indianLocale: Locale = Locale("en", "IN")
    private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(indianLocale)
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    fun paiseToRupeesString(amountInPaise: Long): String {
        val rupees = amountInPaise.toDouble() / 100.0
        return currencyFormat.format(rupees)
    }

    /**
     * Parse a human-entered rupees string (e.g., "1,234.50", "₹ 99") into paise.
     * Returns null if invalid or <= 0.
     */
    fun parseRupeesToPaiseOrNull(input: String): Long? {
        val sanitized = input
            .replace("₹", "")
            .replace(",", "")
            .trim()
        if (sanitized.isEmpty()) return null
        return try {
            val bd = BigDecimal(sanitized)
            val paise = bd.movePointRight(2).setScale(0, RoundingMode.HALF_UP)
            val value = paise.longValueExact()
            if (value > 0L) value else null
        } catch (t: Throwable) {
            null
        }
    }

    fun instantToLocalDate(instant: Instant, zoneId: ZoneId = ZoneId.systemDefault()): LocalDate =
        instant.atZone(zoneId).toLocalDate()

    fun instantToLocalTime(instant: Instant, zoneId: ZoneId = ZoneId.systemDefault()): LocalTime =
        instant.atZone(zoneId).toLocalTime()

    fun formatDate(date: LocalDate): String = date.format(dateFormatter)

    fun formatTime(time: LocalTime): String = time.format(timeFormatter)
}