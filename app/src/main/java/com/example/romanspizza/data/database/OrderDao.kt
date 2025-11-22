package com.example.romanspizza.data.database

import android.content.ContentValues
import android.content.Context
import com.example.romanspizza.data.model.Order
import com.example.romanspizza.data.model.OrderItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderDao(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    // Update order status
    fun updateOrderStatus(orderId: Int, newStatus: String): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_STATUS, newStatus)
        }

        return try {
            val rows = db.update(
                DatabaseHelper.TABLE_ORDERS,
                values,
                "${DatabaseHelper.COLUMN_ORDER_ID} = ?",
                arrayOf(orderId.toString())
            )
            db.close()
            rows > 0
        } catch (e: Exception) {
            db.close()
            false
        }
    }

    // Get order status step (for progress indicator)
    fun getOrderStatusStep(status: String): Int {
        return when (status) {
            "Pending" -> 1
            "Confirmed" -> 2
            "Preparing" -> 3
            "Out for Delivery" -> 4
            "Delivered" -> 5
            else -> 0
        }
    }

    // Calculate estimated delivery time (30-45 minutes from order time)
    fun getEstimatedDeliveryTime(orderDate: Long, status: String): String {
        val estimatedMinutes = when (status) {
            "Pending", "Confirmed" -> 45
            "Preparing" -> 30
            "Out for Delivery" -> 15
            "Delivered" -> 0
            else -> 45
        }

        val estimatedTime = orderDate + (estimatedMinutes * 60 * 1000)
        val now = System.currentTimeMillis()

        return if (estimatedTime > now) {
            val remainingMinutes = ((estimatedTime - now) / (60 * 1000)).toInt()
            if (remainingMinutes > 0) {
                "$remainingMinutes mins"
            } else {
                "Arriving soon"
            }
        } else {
            "Delivered"
        }
    }


    // Get active orders (not delivered or cancelled)
    fun getActiveOrders(userId: Int): List<Order> {
        val orders = mutableListOf<Order>()
        val db = dbHelper.readableDatabase

        val cursor = db.query(
            DatabaseHelper.TABLE_ORDERS,
            null,
            "${DatabaseHelper.COLUMN_USER_ID} = ? AND ${DatabaseHelper.COLUMN_STATUS} NOT IN (?, ?)",
            arrayOf(userId.toString(), "Delivered", "Cancelled"),
            null, null,
            "${DatabaseHelper.COLUMN_ORDER_DATE} DESC"
        )

        while (cursor.moveToNext()) {
            val orderId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORDER_ID))

            orders.add(
                Order(
                    orderId = orderId,
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID)),
                    orderDate = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORDER_DATE)),
                    totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TOTAL_AMOUNT)),
                    status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STATUS)),
                    deliveryAddress = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DELIVERY_ADDRESS)),
                    items = getOrderItems(orderId)
                )
            )
        }

        cursor.close()
        db.close()
        return orders
    }



    // Get order items for a specific order
    private fun getOrderItems(orderId: Int): List<OrderItem> {
        val items = mutableListOf<OrderItem>()
        val db = dbHelper.readableDatabase

        val cursor = db.query(
            DatabaseHelper.TABLE_ORDER_ITEMS,
            null,
            "${DatabaseHelper.COLUMN_ORDER_ID} = ?",
            arrayOf(orderId.toString()),
            null, null, null
        )

        while (cursor.moveToNext()) {
            val toppingsString = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TOPPINGS))
            val toppings = if (toppingsString.isNullOrEmpty()) {
                emptyList()
            } else {
                toppingsString.split(",")
            }

            items.add(
                OrderItem(
                    orderItemId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORDER_ITEM_ID)),
                    orderId = orderId,
                    itemId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_ID)),
                    itemName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_NAME)),
                    quantity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_QUANTITY)),
                    size = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SIZE)),
                    crust = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CRUST)),
                    toppings = toppings,
                    itemPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_PRICE))
                )
            )
        }

        cursor.close()
        return items
    }



    // Get order by ID with items
    fun getOrderById(orderId: Int): Order? {
        val db = dbHelper.readableDatabase

        val orderCursor = db.query(
            DatabaseHelper.TABLE_ORDERS,
            null,
            "${DatabaseHelper.COLUMN_ORDER_ID} = ?",
            arrayOf(orderId.toString()),
            null, null, null
        )

        if (!orderCursor.moveToFirst()) {
            orderCursor.close()
            db.close()
            return null
        }

        val order = Order(
            orderId = orderCursor.getInt(orderCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORDER_ID)),
            userId = orderCursor.getInt(orderCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID)),
            orderDate = orderCursor.getLong(orderCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORDER_DATE)),
            totalAmount = orderCursor.getDouble(orderCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TOTAL_AMOUNT)),
            status = orderCursor.getString(orderCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STATUS)),
            deliveryAddress = orderCursor.getString(orderCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DELIVERY_ADDRESS)),
            items = getOrderItems(orderId)
        )

        orderCursor.close()
        db.close()
        return order
    }




    fun createOrder(order: Order, orderItems: List<OrderItem>): Long {
        val db = dbHelper.writableDatabase
        db.beginTransaction()

        try {
            // Insert order
            val orderValues = ContentValues().apply {
                put(DatabaseHelper.COLUMN_USER_ID, order.userId)
                put(DatabaseHelper.COLUMN_ORDER_DATE, order.orderDate)
                put(DatabaseHelper.COLUMN_TOTAL_AMOUNT, order.totalAmount)
                put(DatabaseHelper.COLUMN_STATUS, order.status)
                put(DatabaseHelper.COLUMN_DELIVERY_ADDRESS, order.deliveryAddress)
            }

            val orderId = db.insert(DatabaseHelper.TABLE_ORDERS, null, orderValues)

            if (orderId > 0) {
                // Insert order items
                orderItems.forEach { item ->
                    val itemValues = ContentValues().apply {
                        put(DatabaseHelper.COLUMN_ORDER_ID, orderId)
                        put(DatabaseHelper.COLUMN_ITEM_ID, item.itemId)
                        put(DatabaseHelper.COLUMN_ITEM_NAME, item.itemName)
                        put(DatabaseHelper.COLUMN_QUANTITY, item.quantity)
                        put(DatabaseHelper.COLUMN_SIZE, item.size)
                        put(DatabaseHelper.COLUMN_CRUST, item.crust)
                        put(DatabaseHelper.COLUMN_TOPPINGS, item.toppings.joinToString(","))
                        put(DatabaseHelper.COLUMN_ITEM_PRICE, item.itemPrice)
                    }

                    db.insert(DatabaseHelper.TABLE_ORDER_ITEMS, null, itemValues)
                }

                db.setTransactionSuccessful()
            }

            db.endTransaction()
            db.close()
            return orderId

        } catch (e: Exception) {
            db.endTransaction()
            db.close()
            return -1
        }
    }

    // Get user orders
    fun getUserOrders(userId: Int): List<Order> {
        val orders = mutableListOf<Order>()
        val db = dbHelper.readableDatabase

        val cursor = db.query(
            DatabaseHelper.TABLE_ORDERS,
            null,
            "${DatabaseHelper.COLUMN_USER_ID} = ?",
            arrayOf(userId.toString()),
            null, null,
            "${DatabaseHelper.COLUMN_ORDER_DATE} DESC"
        )

        while (cursor.moveToNext()) {
            val orderId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORDER_ID))

            orders.add(
                Order(
                    orderId = orderId,
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID)),
                    orderDate = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORDER_DATE)),
                    totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TOTAL_AMOUNT)),
                    status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STATUS)),
                    deliveryAddress = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DELIVERY_ADDRESS)),
                    items = getOrderItems(orderId)
                )
            )
        }

        cursor.close()
        db.close()
        return orders
    }

    // Format timestamp to readable date
    fun formatOrderDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

}
