package com.example.romanspizza.data.model

data class Order(
    val orderId: Int = 0,
    val userId: Int,
    val orderDate: Long,
    val totalAmount: Double,
    val status: String = "Pending",
    val deliveryAddress: String,
    val items: List<OrderItem> = emptyList()
)