package com.example.romanspizza.data.model

data class CartItem(
    val cartId: Int = 0,
    val userId: Int,
    val menuItem: MenuItem,
    var quantity: Int = 1,
    val size: String,
    val crust: String,
    val toppings: List<String> = emptyList(),
    val itemPrice: Double
) {
    fun getTotalPrice(): Double = itemPrice * quantity
}