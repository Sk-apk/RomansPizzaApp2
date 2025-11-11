package com.example.romanspizza.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {

    companion object {
        private const val DATABASE_NAME = "RomansPizza.db"
        private const val DATABASE_VERSION = 3 // Updated version

        // Users Table
        const val TABLE_USERS = "users"
        const val COLUMN_ID = "id"
        const val COLUMN_FULL_NAME = "full_name"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PHONE = "phone"
        const val COLUMN_ADDRESS = "address"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_CREATED_AT = "created_at"

        // Menu Items Table
        const val TABLE_MENU_ITEMS = "menu_items"
        const val COLUMN_ITEM_ID = "item_id"
        const val COLUMN_ITEM_NAME = "item_name"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_BASE_PRICE = "base_price"
        const val COLUMN_IMAGE_URL = "image_url"
        const val COLUMN_IS_AVAILABLE = "is_available" // Changed from COLUMN_AVAILABLE

        // Cart Table
        const val TABLE_CART = "cart"
        const val COLUMN_CART_ID = "cart_id"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_SIZE = "size"
        const val COLUMN_CRUST = "crust"
        const val COLUMN_TOPPINGS = "toppings"
        const val COLUMN_QUANTITY = "quantity"
        const val COLUMN_ITEM_PRICE = "item_price" // Changed from COLUMN_PRICE

        // Orders Table
        const val TABLE_ORDERS = "orders"
        const val COLUMN_ORDER_ID = "order_id"
        const val COLUMN_ORDER_DATE = "order_date"
        const val COLUMN_TOTAL_AMOUNT = "total_amount"
        const val COLUMN_STATUS = "status"
        const val COLUMN_DELIVERY_ADDRESS = "delivery_address"

        // Order Items Table
        const val TABLE_ORDER_ITEMS = "order_items"
        const val COLUMN_ORDER_ITEM_ID = "order_item_id"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Users Table
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_FULL_NAME TEXT NOT NULL,
                $COLUMN_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_PHONE TEXT NOT NULL,
                $COLUMN_ADDRESS TEXT NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL,
                $COLUMN_CREATED_AT INTEGER NOT NULL
            )
        """.trimIndent()

        // Menu Items Table
        val createMenuItemsTable = """
            CREATE TABLE $TABLE_MENU_ITEMS (
                $COLUMN_ITEM_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_ITEM_NAME TEXT NOT NULL,
                $COLUMN_DESCRIPTION TEXT,
                $COLUMN_CATEGORY TEXT NOT NULL,
                $COLUMN_BASE_PRICE REAL NOT NULL,
                $COLUMN_IMAGE_URL TEXT,
                $COLUMN_IS_AVAILABLE INTEGER DEFAULT 1
            )
        """.trimIndent()

        // Cart Table - FIXED: Added COLUMN_ITEM_ID
        val createCartTable = """
            CREATE TABLE $TABLE_CART (
                $COLUMN_CART_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID INTEGER NOT NULL,
                $COLUMN_ITEM_ID INTEGER NOT NULL,
                $COLUMN_QUANTITY INTEGER DEFAULT 1,
                $COLUMN_SIZE TEXT NOT NULL,
                $COLUMN_CRUST TEXT NOT NULL,
                $COLUMN_TOPPINGS TEXT,
                $COLUMN_ITEM_PRICE REAL NOT NULL,
                FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID),
                FOREIGN KEY($COLUMN_ITEM_ID) REFERENCES $TABLE_MENU_ITEMS($COLUMN_ITEM_ID)
            )
        """.trimIndent()

        // Orders Table
        val createOrdersTable = """
            CREATE TABLE $TABLE_ORDERS (
                $COLUMN_ORDER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID INTEGER NOT NULL,
                $COLUMN_ORDER_DATE INTEGER NOT NULL,
                $COLUMN_TOTAL_AMOUNT REAL NOT NULL,
                $COLUMN_STATUS TEXT DEFAULT 'Pending',
                $COLUMN_DELIVERY_ADDRESS TEXT NOT NULL,
                FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID)
            )
        """.trimIndent()

        // Order Items Table - FIXED: Added COLUMN_ITEM_ID
        val createOrderItemsTable = """
            CREATE TABLE $TABLE_ORDER_ITEMS (
                $COLUMN_ORDER_ITEM_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_ORDER_ID INTEGER NOT NULL,
                $COLUMN_ITEM_ID INTEGER NOT NULL,
                $COLUMN_ITEM_NAME TEXT NOT NULL,
                $COLUMN_QUANTITY INTEGER NOT NULL,
                $COLUMN_SIZE TEXT NOT NULL,
                $COLUMN_CRUST TEXT NOT NULL,
                $COLUMN_TOPPINGS TEXT,
                $COLUMN_ITEM_PRICE REAL NOT NULL,
                FOREIGN KEY($COLUMN_ORDER_ID) REFERENCES $TABLE_ORDERS($COLUMN_ORDER_ID),
                FOREIGN KEY($COLUMN_ITEM_ID) REFERENCES $TABLE_MENU_ITEMS($COLUMN_ITEM_ID)
            )
        """.trimIndent()

        try {
            db?.execSQL(createUsersTable)
            db?.execSQL(createMenuItemsTable)
            db?.execSQL(createCartTable)
            db?.execSQL(createOrdersTable)
            db?.execSQL(createOrderItemsTable)

            // Insert sample menu items
            insertSampleMenuItems(db)

            Log.d("DatabaseHelper", "All tables created successfully")
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error creating tables: ${e.message}")
        }
    }

    private fun insertSampleMenuItems(db: SQLiteDatabase?) {
        val menuItems = listOf(
            // Classic Pizzas
            "('Margherita', 'Classic tomato, mozzarella & fresh basil', 'Classic', 75.50, NULL, 1)",
            "('BBQ Chicken', 'BBQ sauce, chicken, onions & peppers', 'Classic', 90.00, NULL, 1)",
            "('Hawaiian', 'Ham, pineapple & mozzarella', 'Classic', 80.00, NULL, 1)",
            "('Pepperoni', 'Pepperoni, mozzarella & tomato sauce', 'Classic', 85.00, NULL, 1)",

            // Specialty Pizzas
            "('Chicken Supreme', 'Chicken, mushrooms, peppers, onions & olives', 'Specialty', 110.00, NULL, 1)",
            "('Meat Lovers', 'Pepperoni, bacon, beef, ham & sausage', 'Specialty', 120.00, NULL, 1)",

            // Vegetarian Pizzas
            "('Veggie Delight', 'Mushrooms, peppers, onions, tomatoes & olives', 'Vegetarian', 95.00, NULL, 1)",
            "('Mediterranean', 'Feta, olives, tomatoes, spinach & garlic', 'Vegetarian', 100.00, NULL, 1)"
        )

        menuItems.forEach { item ->
            try {
                db?.execSQL("INSERT INTO $TABLE_MENU_ITEMS ($COLUMN_ITEM_NAME, $COLUMN_DESCRIPTION, $COLUMN_CATEGORY, $COLUMN_BASE_PRICE, $COLUMN_IMAGE_URL, $COLUMN_IS_AVAILABLE) VALUES $item")
            } catch (e: Exception) {
                Log.e("DatabaseHelper", "Error inserting menu item: ${e.message}")
            }
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_ORDER_ITEMS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_ORDERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CART")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_MENU_ITEMS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }
}