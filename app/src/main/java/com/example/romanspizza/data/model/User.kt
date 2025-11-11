package com.example.romanspizza.data.model


data class User(
    val id: Int = 0,
    val fullName: String,
    val email: String,
    val phone: String,
    val address: String,
    val password: String,
    val createdAt: Long = System.currentTimeMillis()
)
