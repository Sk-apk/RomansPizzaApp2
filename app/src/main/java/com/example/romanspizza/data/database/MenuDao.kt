package com.example.romanspizza.data.database

import android.content.Context
import android.database.Cursor
import com.example.romanspizza.data.model.MenuItem

class MenuDao(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun getAllMenuItems(): List<MenuItem> {
        val items = mutableListOf<MenuItem>()
        val db = dbHelper.readableDatabase

        val cursor = db.query(
            DatabaseHelper.TABLE_MENU_ITEMS,
            null,
            "${DatabaseHelper.COLUMN_IS_AVAILABLE} = ?",
            arrayOf("1"),
            null, null,
            DatabaseHelper.COLUMN_CATEGORY
        )

        while (cursor.moveToNext()) {
            items.add(getMenuItemFromCursor(cursor))
        }

        cursor.close()
        db.close()
        return items
    }

    fun getMenuItemsByCategory(category: String): List<MenuItem> {
        val items = mutableListOf<MenuItem>()
        val db = dbHelper.readableDatabase

        val cursor = db.query(
            DatabaseHelper.TABLE_MENU_ITEMS,
            null,
            "${DatabaseHelper.COLUMN_CATEGORY} = ? AND ${DatabaseHelper.COLUMN_IS_AVAILABLE} = ?",
            arrayOf(category, "1"),
            null, null,
            DatabaseHelper.COLUMN_ITEM_NAME
        )

        while (cursor.moveToNext()) {
            items.add(getMenuItemFromCursor(cursor))
        }

        cursor.close()
        db.close()
        return items
    }

    fun getMenuItemById(itemId: Int): MenuItem? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_MENU_ITEMS,
            null,
            "${DatabaseHelper.COLUMN_ITEM_ID} = ?",
            arrayOf(itemId.toString()),
            null, null, null
        )

        val item = if (cursor.moveToFirst()) {
            getMenuItemFromCursor(cursor)
        } else null

        cursor.close()
        db.close()
        return item
    }

    fun getCategories(): List<String> {
        val categories = mutableListOf<String>()
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            "SELECT DISTINCT ${DatabaseHelper.COLUMN_CATEGORY} FROM ${DatabaseHelper.TABLE_MENU_ITEMS} WHERE ${DatabaseHelper.COLUMN_IS_AVAILABLE} = 1",
            null
        )

        while (cursor.moveToNext()) {
            categories.add(cursor.getString(0))
        }

        cursor.close()
        db.close()
        return categories
    }

    private fun getMenuItemFromCursor(cursor: Cursor): MenuItem {
        return MenuItem(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_ID)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_NAME)),
            description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION)),
            category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY)),
            basePrice = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BASE_PRICE)),
            imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE_URL)),
            isAvailable = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_AVAILABLE)) == 1
        )
    }
}
