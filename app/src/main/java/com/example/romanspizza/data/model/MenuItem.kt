package com.example.romanspizza.data.model

data class MenuItem(
    val id: Int,
    val name: String,
    val description: String,
    val category: String,
    val basePrice: Double,
    val imageUrl: String? = null,
    val isAvailable: Boolean = true
)