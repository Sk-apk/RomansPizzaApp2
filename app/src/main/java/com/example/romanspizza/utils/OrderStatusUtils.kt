package com.example.romanspizza.utils

import com.example.romanspizza.R

object OrderStatusUtils {

    // Get status display name
    fun getStatusDisplayName(status: String): String {
        return when (status) {
            "Pending" -> "Order Placed"
            "Confirmed" -> "Order Confirmed"
            "Preparing" -> "Preparing Your Order"
            "Out for Delivery" -> "Out for Delivery"
            "Delivered" -> "Delivered"
            "Cancelled" -> "Cancelled"
            else -> status
        }
    }

    // Get status color resource
    fun getStatusColor(status: String): Int {
        return when (status) {
            "Pending" -> R.color.status_pending
            "Confirmed" -> R.color.status_preparing
            "Preparing" -> R.color.status_preparing
            "Out for Delivery" -> R.color.status_delivering
            "Delivered" -> R.color.status_completed
            "Cancelled" -> R.color.status_cancelled
            else -> R.color.text_secondary
        }
    }

    // Get status icon emoji
    fun getStatusIcon(status: String): String {
        return when (status) {
            "Pending" -> "🕐"
            "Confirmed" -> "✅"
            "Preparing" -> "👨‍🍳"
            "Out for Delivery" -> "🚗"
            "Delivered" -> "🎉"
            "Cancelled" -> "❌"
            else -> "📦"
        }
    }

    // Get status description
    fun getStatusDescription(status: String): String {
        return when (status) {
            "Pending" -> "We've received your order and are confirming it"
            "Confirmed" -> "Your order has been confirmed and will be prepared soon"
            "Preparing" -> "Our chefs are preparing your delicious pizza"
            "Out for Delivery" -> "Your order is on its way to you"
            "Delivered" -> "Your order has been delivered. Enjoy!"
            "Cancelled" -> "This order has been cancelled"
            else -> "Order status unknown"
        }
    }

    // Get all possible statuses in order
    fun getAllStatuses(): List<String> {
        return listOf(
            "Pending",
            "Confirmed",
            "Preparing",
            "Out for Delivery",
            "Delivered"
        )
    }

    // Get status progress percentage
    fun getStatusProgress(status: String): Int {
        return when (status) {
            "Pending" -> 20
            "Confirmed" -> 40
            "Preparing" -> 60
            "Out for Delivery" -> 80
            "Delivered" -> 100
            else -> 0
        }
    }

    // Check if order is active
    fun isActiveOrder(status: String): Boolean {
        return status in listOf("Pending", "Confirmed", "Preparing", "Out for Delivery")
    }

    // Get estimated time in minutes based on status
    fun getEstimatedTimeMinutes(status: String): Int {
        return when (status) {
            "Pending" -> 45
            "Confirmed" -> 40
            "Preparing" -> 25
            "Out for Delivery" -> 10
            "Delivered" -> 0
            else -> 45
        }
    }
}
