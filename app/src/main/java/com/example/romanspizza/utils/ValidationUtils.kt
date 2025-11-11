package com.example.romanspizza.utils

import android.util.Patterns

object ValidationUtils {

    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun isValidPhone(phone: String): Boolean {
        // South African phone format: 10 digits
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        return cleanPhone.length == 10
    }

    fun isValidName(name: String): Boolean {
        return name.length >= 2
    }

    fun isValidAddress(address: String): Boolean {
        return address.length >= 5
    }
}