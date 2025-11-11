package com.example.romanspizza.data.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.romanspizza.data.model.CartItem
import com.example.romanspizza.data.model.MenuItem

class CartDao(context: Context) {
    private val dbHelper = DatabaseHelper(context)
    private val menuDao = MenuDao(context)

    fun addToCart(cartItem: CartItem): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_USER_ID, cartItem.userId)
            put(DatabaseHelper.COLUMN_ITEM_ID, cartItem.menuItem.id)
            put(DatabaseHelper.COLUMN_QUANTITY, cartItem.quantity)
            put(DatabaseHelper.COLUMN_SIZE, cartItem.size)
            put(DatabaseHelper.COLUMN_CRUST, cartItem.crust)
            put(DatabaseHelper.COLUMN_TOPPINGS, cartItem.toppings.joinToString(","))
            put(DatabaseHelper.COLUMN_ITEM_PRICE, cartItem.itemPrice)
        }

        val result = db.insert(DatabaseHelper.TABLE_CART, null, values)
        db.close()
        return result
    }

    fun getCartItems(userId: Int): List<CartItem> {
        val items = mutableListOf<CartItem>()
        val db = dbHelper.readableDatabase

        val cursor = db.query(
            DatabaseHelper.TABLE_CART,
            null,
            "${DatabaseHelper.COLUMN_USER_ID} = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        while (cursor.moveToNext()) {
            val itemId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_ID))
            val menuItem = menuDao.getMenuItemById(itemId)

            if (menuItem != null) {
                items.add(getCartItemFromCursor(cursor, menuItem, userId))
            }
        }

        cursor.close()
        db.close()
        return items
    }

    fun updateCartItemQuantity(cartId: Int, quantity: Int): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_QUANTITY, quantity)
        }

        val rows = db.update(
            DatabaseHelper.TABLE_CART,
            values,
            "${DatabaseHelper.COLUMN_CART_ID} = ?",
            arrayOf(cartId.toString())
        )

        db.close()
        return rows > 0
    }

    fun removeFromCart(cartId: Int): Boolean {
        val db = dbHelper.writableDatabase
        val rows = db.delete(
            DatabaseHelper.TABLE_CART,
            "${DatabaseHelper.COLUMN_CART_ID} = ?",
            arrayOf(cartId.toString())
        )

        db.close()
        return rows > 0
    }

    fun clearCart(userId: Int): Boolean {
        val db = dbHelper.writableDatabase
        val rows = db.delete(
            DatabaseHelper.TABLE_CART,
            "${DatabaseHelper.COLUMN_USER_ID} = ?",
            arrayOf(userId.toString())
        )

        db.close()
        return rows > 0
    }

    fun getCartTotal(userId: Int): Double {
        val items = getCartItems(userId)
        return items.sumOf { it.getTotalPrice() }
    }

    fun getCartItemCount(userId: Int): Int {
        val items = getCartItems(userId)
        return items.sumOf { it.quantity }
    }

    private fun getCartItemFromCursor(cursor: Cursor, menuItem: MenuItem, userId: Int): CartItem {
        val toppingsString = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TOPPINGS))
        val toppings = if (toppingsString.isNullOrEmpty()) {
            emptyList()
        } else {
            toppingsString.split(",")
        }

        return CartItem(
            cartId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CART_ID)),
            userId = userId,
            menuItem = menuItem,
            quantity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_QUANTITY)),
            size = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SIZE)),
            crust = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CRUST)),
            toppings = toppings,
            itemPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_PRICE))
        )
    }
}
