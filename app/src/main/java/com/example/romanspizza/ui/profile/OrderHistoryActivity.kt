package com.example.romanspizza.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.romanspizza.R
import com.example.romanspizza.data.database.CartDao
import com.example.romanspizza.data.database.OrderDao
import com.example.romanspizza.data.model.CartItem
import com.example.romanspizza.data.model.Order
import com.example.romanspizza.utils.SharedPrefsManager
import com.google.android.material.appbar.MaterialToolbar

class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var rvOrderHistory: RecyclerView
    private lateinit var layoutEmpty: LinearLayout

    private lateinit var orderDao: OrderDao
    private lateinit var cartDao: CartDao
    private lateinit var sharedPrefsManager: SharedPrefsManager
    private lateinit var orderAdapter: OrderHistoryAdapter

    private var orders = listOf<Order>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        supportActionBar?.hide()

        initializeViews()
        setupToolbar()
        loadOrders()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        rvOrderHistory = findViewById(R.id.rvOrderHistory)
        layoutEmpty = findViewById(R.id.layoutEmpty)

        orderDao = OrderDao(this)
        cartDao = CartDao(this)
        sharedPrefsManager = SharedPrefsManager(this)

        rvOrderHistory.layoutManager = LinearLayoutManager(this)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadOrders() {
        val userId = sharedPrefsManager.getUserId()
        orders = orderDao.getUserOrders(userId)

        if (orders.isEmpty()) {
            layoutEmpty.visibility = View.VISIBLE
            rvOrderHistory.visibility = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            rvOrderHistory.visibility = View.VISIBLE

            orderAdapter = OrderHistoryAdapter(
                orders,
                onViewDetails = { order ->
                    viewOrderDetails(order)
                },
                onReorder = { order ->
                    reorderItems(order)
                }
            )
            rvOrderHistory.adapter = orderAdapter
        }
    }

    private fun viewOrderDetails(order: Order) {
        val intent = Intent(this, OrderDetailActivity::class.java)
        intent.putExtra("ORDER_ID", order.orderId)
        startActivity(intent)
    }

    private fun reorderItems(order: Order) {
        AlertDialog.Builder(this)
            .setTitle("Reorder")
            .setMessage("Add all items from this order to your cart?")
            .setPositiveButton("Yes") { _, _ ->
                val userId = sharedPrefsManager.getUserId()
                var itemsAdded = 0

                order.items.forEach { orderItem ->
                    // Get menu item to create cart item
                    val menuDao = com.example.romanspizza.data.database.MenuDao(this)
                    val menuItem = menuDao.getMenuItemById(orderItem.itemId)

                    if (menuItem != null) {
                        val cartItem = CartItem(
                            userId = userId,
                            menuItem = menuItem,
                            quantity = orderItem.quantity,
                            size = orderItem.size,
                            crust = orderItem.crust,
                            toppings = orderItem.toppings,
                            itemPrice = orderItem.itemPrice
                        )

                        val result = cartDao.addToCart(cartItem)
                        if (result > 0) itemsAdded++
                    }
                }

                if (itemsAdded > 0) {
                    Toast.makeText(
                        this,
                        "$itemsAdded item${if (itemsAdded > 1) "s" else ""} added to cart!",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to add items to cart", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}