package com.example.romanspizza.data.repository

import com.example.romanspizza.data.model.Order
import com.example.romanspizza.data.model.OrderItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class OrderRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val ordersCollection = firestore.collection("orders")

    // Create new order
    suspend fun createOrder(order: Order, orderItems: List<OrderItem>): Result<String> {
        return try {
            val orderData = hashMapOf(
                "userId" to order.userId.toString(),
                "orderDate" to order.orderDate,
                "totalAmount" to order.totalAmount,
                "status" to order.status,
                "deliveryAddress" to order.deliveryAddress,
                "items" to orderItems.map { item ->
                    hashMapOf(
                        "menuItemId" to item.itemId.toString(),
                        "menuItemName" to item.itemName,
                        "quantity" to item.quantity,
                        "size" to item.size,
                        "crust" to item.crust,
                        "toppings" to item.toppings,
                        "itemPrice" to item.itemPrice
                    )
                }
            )

            val docRef = ordersCollection.add(orderData).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get user orders
    suspend fun getUserOrders(userId: String): Result<List<Order>> {
        return try {
            val snapshot = ordersCollection
                .whereEqualTo("userId", userId)
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .get()
                .await()

            val orders = snapshot.documents.mapNotNull { doc ->
                val itemsList = doc.get("items") as? List<Map<String, Any>> ?: emptyList()

                val orderItems = itemsList.map { itemMap ->
                    val toppings = itemMap["toppings"] as? List<String> ?: emptyList()
                    OrderItem(
                        orderItemId = 0,
                        orderId = 0,
                        itemId = (itemMap["menuItemId"] as? String)?.toIntOrNull() ?: 0,
                        itemName = itemMap["menuItemName"] as? String ?: "",
                        quantity = (itemMap["quantity"] as? Long)?.toInt() ?: 1,
                        size = itemMap["size"] as? String ?: "",
                        crust = itemMap["crust"] as? String ?: "",
                        toppings = toppings,
                        itemPrice = itemMap["itemPrice"] as? Double ?: 0.0
                    )
                }

                Order(
                    orderId = doc.id.hashCode(),
                    userId = (doc.getString("userId"))?.toIntOrNull() ?: 0,
                    orderDate = doc.getLong("orderDate") ?: 0L,
                    totalAmount = doc.getDouble("totalAmount") ?: 0.0,
                    status = doc.getString("status") ?: "Pending",
                    deliveryAddress = doc.getString("deliveryAddress") ?: "",
                    items = orderItems
                )
            }

            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update order status (for admin/staff)
    suspend fun updateOrderStatus(orderId: String, newStatus: String): Result<Boolean> {
        return try {
            ordersCollection
                .document(orderId)
                .update("status", newStatus)
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}