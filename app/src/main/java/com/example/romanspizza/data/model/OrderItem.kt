package com.example.romanspizza.data.model

data class OrderItem(
    val orderItemId: Int = 0,
    val orderId: Int,
    val itemId: Int,
    val itemName: String,
    val quantity: Int,
    val size: String,
    val crust: String,
    val toppings: List<String>,
    val itemPrice: Double
)