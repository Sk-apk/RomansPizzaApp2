package com.example.romanspizza.data.repository

import com.example.romanspizza.data.model.CartItem
import com.example.romanspizza.data.model.MenuItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CartRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val menuRepository = MenuRepository()

    // Add item to cart
    suspend fun addToCart(userId: String, cartItem: CartItem): Result<String> {
        return try {
            val cartData = hashMapOf(
                "menuItemId" to cartItem.menuItem.id.toString(),
                "menuItemName" to cartItem.menuItem.name,
                "quantity" to cartItem.quantity,
                "size" to cartItem.size,
                "crust" to cartItem.crust,
                "toppings" to cartItem.toppings,
                "itemPrice" to cartItem.itemPrice
            )

            val docRef = firestore.collection("carts")
                .document(userId)
                .collection("items")
                .add(cartData)
                .await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get cart items for user
    suspend fun getCartItems(userId: String): Result<List<CartItem>> {
        return try {
            val snapshot = firestore.collection("carts")
                .document(userId)
                .collection("items")
                .get()
                .await()

            val items = mutableListOf<CartItem>()

            for (doc in snapshot.documents) {
                val menuItemId = doc.getString("menuItemId")?.toIntOrNull() ?: continue
                val menuItemResult = menuRepository.getMenuItemById(menuItemId)

                if (menuItemResult.isSuccess) {
                    val menuItem = menuItemResult.getOrNull() ?: continue
                    val toppingsList = doc.get("toppings") as? List<String> ?: emptyList()

                    items.add(
                        CartItem(
                            cartId = doc.id.hashCode(),
                            userId = 0, // Not used with Firebase
                            menuItem = menuItem,
                            quantity = doc.getLong("quantity")?.toInt() ?: 1,
                            size = doc.getString("size") ?: "",
                            crust = doc.getString("crust") ?: "",
                            toppings = toppingsList,
                            itemPrice = doc.getDouble("itemPrice") ?: 0.0
                        )
                    )
                }
            }

            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update cart item quantity
    suspend fun updateCartItemQuantity(userId: String, cartItemDocId: String, quantity: Int): Result<Boolean> {
        return try {
            firestore.collection("carts")
                .document(userId)
                .collection("items")
                .document(cartItemDocId)
                .update("quantity", quantity)
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Remove item from cart
    suspend fun removeFromCart(userId: String, cartItemDocId: String): Result<Boolean> {
        return try {
            firestore.collection("carts")
                .document(userId)
                .collection("items")
                .document(cartItemDocId)
                .delete()
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Clear entire cart
    suspend fun clearCart(userId: String): Result<Boolean> {
        return try {
            val snapshot = firestore.collection("carts")
                .document(userId)
                .collection("items")
                .get()
                .await()

            for (doc in snapshot.documents) {
                doc.reference.delete().await()
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get cart total
    suspend fun getCartTotal(userId: String): Double {
        val cartItems = getCartItems(userId).getOrNull() ?: return 0.0
        return cartItems.sumOf { it.getTotalPrice() }
    }

    // Get cart item count
    suspend fun getCartItemCount(userId: String): Int {
        val cartItems = getCartItems(userId).getOrNull() ?: return 0
        return cartItems.sumOf { it.quantity }
    }
}