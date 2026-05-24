package com.example.data.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyHelper {
    fun formatCOP(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
            maximumFractionDigits = 0
        }
        return "${format.format(amount)} COP"
    }

    fun formatCOPSimple(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
            maximumFractionDigits = 0
        }
        return format.format(amount)
    }
}
