package com.example.romanspizza.ui.tracking

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.romanspizza.R
import com.example.romanspizza.data.database.OrderDao
import com.example.romanspizza.data.model.Order
import com.example.romanspizza.utils.SharedPrefsManager
import com.google.android.material.appbar.MaterialToolbar

class ActiveOrdersActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var rvActiveOrders: RecyclerView
    private lateinit var layoutEmpty: LinearLayout

    private lateinit var orderDao: OrderDao
    private lateinit var sharedPrefsManager: SharedPrefsManager
    private lateinit var orderAdapter: ActiveOrdersAdapter

    private var orders = listOf<Order>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_active_orders)

        supportActionBar?.hide()

        initializeViews()
        setupToolbar()
        loadActiveOrders()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        rvActiveOrders = findViewById(R.id.rvActiveOrders)
        layoutEmpty = findViewById(R.id.layoutEmpty)

        orderDao = OrderDao(this)
        sharedPrefsManager = SharedPrefsManager(this)

        rvActiveOrders.layoutManager = LinearLayoutManager(this)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadActiveOrders() {
        val userId = sharedPrefsManager.getUserId()
        orders = orderDao.getActiveOrders(userId)

        if (orders.isEmpty()) {
            layoutEmpty.visibility = View.VISIBLE
            rvActiveOrders.visibility = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            rvActiveOrders.visibility = View.VISIBLE

            orderAdapter = ActiveOrdersAdapter(orders) { order ->
                trackOrder(order)
            }
            rvActiveOrders.adapter = orderAdapter
        }
    }

    private fun trackOrder(order: Order) {
        val intent = Intent(this, OrderTrackingActivity::class.java)
        intent.putExtra("ORDER_ID", order.orderId)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        loadActiveOrders() // Refresh when returning
    }
}
