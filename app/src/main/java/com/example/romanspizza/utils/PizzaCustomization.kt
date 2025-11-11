package com.example.romanspizza.utils

object PizzaCustomization {

    val SIZES = listOf(
        PizzaSize("Small", 0.0, "7 inch"),
        PizzaSize("Medium", 20.0, "10 inch"),
        PizzaSize("Large", 40.0, "12 inch"),
        PizzaSize("Extra Large", 60.0, "15 inch")
    )

    val CRUSTS = listOf(
        PizzaCrust("Thin & Crispy", 0.0),
        PizzaCrust("Classic Hand-Tossed", 5.0),
        PizzaCrust("Thick & Fluffy", 10.0),
        PizzaCrust("Cheese-Stuffed", 15.0)
    )

    val TOPPINGS = listOf(
        Topping("Extra Cheese", 8.0),
        Topping("Pepperoni", 12.0),
        Topping("Mushrooms", 10.0),
        Topping("Onions", 8.0),
        Topping("Green Peppers", 8.0),
        Topping("Black Olives", 10.0),
        Topping("Bacon", 15.0),
        Topping("Ham", 12.0),
        Topping("Chicken", 15.0),
        Topping("Beef", 15.0),
        Topping("Sausage", 12.0),
        Topping("Pineapple", 10.0),
        Topping("Tomatoes", 8.0),
        Topping("Spinach", 10.0),
        Topping("Feta Cheese", 12.0)
    )

    data class PizzaSize(val name: String, val additionalPrice: Double, val description: String)
    data class PizzaCrust(val name: String, val additionalPrice: Double)
    data class Topping(val name: String, val price: Double)

    fun calculateTotalPrice(
        basePrice: Double,
        size: PizzaSize,
        crust: PizzaCrust,
        selectedToppings: List<Topping>
    ): Double {
        val toppingsTotal = selectedToppings.sumOf { it.price }
        return basePrice + size.additionalPrice + crust.additionalPrice + toppingsTotal
    }
}