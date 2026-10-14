package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
    private val shortDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

    fun formatDate(timestamp: Long): String {
        return try {
            dateFormat.format(Date(timestamp))
        } catch (e: Exception) {
            ""
        }
    }

    fun formatShortDate(timestamp: Long): String {
        return try {
            shortDateFormat.format(Date(timestamp))
        } catch (e: Exception) {
            ""
        }
    }

    fun formatCurrency(amount: Double, symbol: String = "R"): String {
        val format = NumberFormat.getNumberInstance(Locale.US)
        format.minimumFractionDigits = 2
        format.maximumFractionDigits = 2
        val cleanSymbol = if (symbol.isBlank()) "R" else symbol
        return "$cleanSymbol ${format.format(amount)}"
    }
}
