package com.example.romanspizza.data.database

import android.content.ContentValues
import android.content.Context
import com.example.romanspizza.data.model.Order
import com.example.romanspizza.data.model.OrderItem

class OrderDao(context: Context) {
    private val dbHelper = DatabaseHelper(context)

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
                    deliveryAddress = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DELIVERY_ADDRESS))
                )
            )
        }

        cursor.close()
        db.close()
        return orders
    }
}
