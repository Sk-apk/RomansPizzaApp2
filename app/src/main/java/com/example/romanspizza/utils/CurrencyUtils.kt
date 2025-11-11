package com.example.romanspizza.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    fun formatPrice(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        return format.format(amount)
    }
}