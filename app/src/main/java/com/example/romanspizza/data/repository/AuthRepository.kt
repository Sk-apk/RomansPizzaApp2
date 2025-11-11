package com.example.romanspizza.data.repository

import com.example.romanspizza.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Register new user
    suspend fun registerUser(user: User, password: String): Result<String> {
        return try {
            // Create Firebase Auth account
            val authResult = auth.createUserWithEmailAndPassword(user.email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("User ID is null")

            // Save user data to Firestore
            val userData = hashMapOf(
                "fullName" to user.fullName,
                "email" to user.email,
                "phone" to user.phone,
                "address" to user.address,
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(userId)
                .set(userData)
                .await()

            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Login user
    suspend fun loginUser(email: String, password: String): Result<String> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("User ID is null")
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Logout user
    fun logoutUser() {
        auth.signOut()
    }

    // Get current user
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    // Check if user is logged in
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    // Get user data from Firestore
    suspend fun getUserData(userId: String): Result<User> {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                val user = User(
                    id = 0, // Not used with Firebase
                    fullName = document.getString("fullName") ?: "",
                    email = document.getString("email") ?: "",
                    phone = document.getString("phone") ?: "",
                    address = document.getString("address") ?: "",
                    password = "", // Never retrieve password
                    createdAt = document.getLong("createdAt") ?: 0L
                )
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update user address
    suspend fun updateUserAddress(userId: String, newAddress: String): Result<Boolean> {
        return try {
            firestore.collection("users")
                .document(userId)
                .update("address", newAddress)
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
