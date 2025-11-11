package com.example.romanspizza.data.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.romanspizza.data.model.User
import java.security.MessageDigest

class UserDao(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    // Hash password using SHA-256
    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // Register new user
    fun registerUser(user: User): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_FULL_NAME, user.fullName)
            put(DatabaseHelper.COLUMN_EMAIL, user.email.lowercase())
            put(DatabaseHelper.COLUMN_PHONE, user.phone)
            put(DatabaseHelper.COLUMN_ADDRESS, user.address)
            put(DatabaseHelper.COLUMN_PASSWORD, hashPassword(user.password))
            put(DatabaseHelper.COLUMN_CREATED_AT, user.createdAt)
        }

        return try {
            db.insert(DatabaseHelper.TABLE_USERS, null, values)
        } catch (e: Exception) {
            -1
        } finally {
            db.close()
        }
    }

    // Check if email exists
    fun isEmailExists(email: String): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            arrayOf(DatabaseHelper.COLUMN_EMAIL),
            "${DatabaseHelper.COLUMN_EMAIL} = ?",
            arrayOf(email.lowercase()),
            null, null, null
        )

        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    // Login user
    fun loginUser(email: String, password: String): User? {
        val db = dbHelper.readableDatabase
        val hashedPassword = hashPassword(password)

        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            null,
            "${DatabaseHelper.COLUMN_EMAIL} = ? AND ${DatabaseHelper.COLUMN_PASSWORD} = ?",
            arrayOf(email.lowercase(), hashedPassword),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            val user = getUserFromCursor(cursor)
            cursor.close()
            db.close()
            user
        } else {
            cursor.close()
            db.close()
            null
        }
    }

    // Get user by ID
    fun getUserById(userId: Int): User? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            null,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            val user = getUserFromCursor(cursor)
            cursor.close()
            db.close()
            user
        } else {
            cursor.close()
            db.close()
            null
        }
    }

    private fun getUserFromCursor(cursor: Cursor): User {
        return User(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
            fullName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FULL_NAME)),
            email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL)),
            phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHONE)),
            address = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ADDRESS)),
            password = "", // Don't return password
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATED_AT))
        )
    }
}