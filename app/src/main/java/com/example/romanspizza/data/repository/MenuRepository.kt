package com.example.romanspizza.data.repository

import com.example.romanspizza.data.model.MenuItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MenuRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val menuCollection = firestore.collection("menuItems")

    // Get all menu items
    suspend fun getAllMenuItems(): Result<List<MenuItem>> {
        return try {
            val snapshot = menuCollection
                .whereEqualTo("isAvailable", true)
                .orderBy("category")
                .get()
                .await()

            val items = snapshot.documents.mapNotNull { doc ->
                MenuItem(
                    id = doc.id.hashCode(), // Generate int ID from document ID
                    name = doc.getString("name") ?: "",
                    description = doc.getString("description") ?: "",
                    category = doc.getString("category") ?: "",
                    basePrice = doc.getDouble("basePrice") ?: 0.0,
                    imageUrl = doc.getString("imageUrl"),
                    isAvailable = doc.getBoolean("isAvailable") ?: true
                )
            }

            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get menu items by category
    suspend fun getMenuItemsByCategory(category: String): Result<List<MenuItem>> {
        return try {
            val snapshot = menuCollection
                .whereEqualTo("category", category)
                .whereEqualTo("isAvailable", true)
                .get()
                .await()

            val items = snapshot.documents.mapNotNull { doc ->
                MenuItem(
                    id = doc.id.hashCode(),
                    name = doc.getString("name") ?: "",
                    description = doc.getString("description") ?: "",
                    category = doc.getString("category") ?: "",
                    basePrice = doc.getDouble("basePrice") ?: 0.0,
                    imageUrl = doc.getString("imageUrl"),
                    isAvailable = doc.getBoolean("isAvailable") ?: true
                )
            }

            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get menu item by ID
    suspend fun getMenuItemById(itemId: Int): Result<MenuItem> {
        return try {
            // Since we're using hashCode as ID, we need to search
            val allItems = getAllMenuItems().getOrThrow()
            val item = allItems.find { it.id == itemId }

            if (item != null) {
                Result.success(item)
            } else {
                Result.failure(Exception("Item not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get all categories
    suspend fun getCategories(): Result<List<String>> {
        return try {
            val snapshot = menuCollection
                .whereEqualTo("isAvailable", true)
                .get()
                .await()

            val categories = snapshot.documents
                .mapNotNull { it.getString("category") }
                .distinct()
                .sorted()

            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Initialize sample menu items (call once)
    suspend fun initializeSampleMenuItems(): Result<Boolean> {
        return try {
            val samplePizzas = listOf(
                hashMapOf(
                    "name" to "Margherita",
                    "description" to "Classic tomato, mozzarella & fresh basil",
                    "category" to "Classic",
                    "basePrice" to 75.50,
                    "imageUrl" to null,
                    "isAvailable" to true
                ),
                hashMapOf(
                    "name" to "BBQ Chicken",
                    "description" to "BBQ sauce, chicken, onions & peppers",
                    "category" to "Classic",
                    "basePrice" to 90.00,
                    "imageUrl" to null,
                    "isAvailable" to true
                ),
                hashMapOf(
                    "name" to "Hawaiian",
                    "description" to "Ham, pineapple & mozzarella",
                    "category" to "Classic",
                    "basePrice" to 80.00,
                    "imageUrl" to null,
                    "isAvailable" to true
                ),
                hashMapOf(
                    "name" to "Pepperoni",
                    "description" to "Pepperoni, mozzarella & tomato sauce",
                    "category" to "Classic",
                    "basePrice" to 85.00,
                    "imageUrl" to null,
                    "isAvailable" to true
                ),
                hashMapOf(
                    "name" to "Chicken Supreme",
                    "description" to "Chicken, mushrooms, peppers, onions & olives",
                    "category" to "Specialty",
                    "basePrice" to 110.00,
                    "imageUrl" to null,
                    "isAvailable" to true
                ),
                hashMapOf(
                    "name" to "Meat Lovers",
                    "description" to "Pepperoni, bacon, beef, ham & sausage",
                    "category" to "Specialty",
                    "basePrice" to 120.00,
                    "imageUrl" to null,
                    "isAvailable" to true
                ),
                hashMapOf(
                    "name" to "Veggie Delight",
                    "description" to "Mushrooms, peppers, onions, tomatoes & olives",
                    "category" to "Vegetarian",
                    "basePrice" to 95.00,
                    "imageUrl" to null,
                    "isAvailable" to true
                ),
                hashMapOf(
                    "name" to "Mediterranean",
                    "description" to "Feta, olives, tomatoes, spinach & garlic",
                    "category" to "Vegetarian",
                    "basePrice" to 100.00,
                    "imageUrl" to null,
                    "isAvailable" to true
                )
            )

            // Check if menu already exists
            val existingItems = menuCollection.limit(1).get().await()
            if (existingItems.isEmpty) {
                // Add sample items
                samplePizzas.forEach { pizza ->
                    menuCollection.add(pizza).await()
                }
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}