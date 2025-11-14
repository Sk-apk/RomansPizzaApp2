package com.example.romanspizza.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.romanspizza.R
import com.example.romanspizza.data.database.OrderDao
import com.example.romanspizza.utils.CurrencyUtils
import com.example.romanspizza.utils.DateUtils
import com.google.android.material.appbar.MaterialToolbar

class OrderDetailActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvOrderId: TextView
    private lateinit var tvOrderDate: TextView
    private lateinit var tvOrderStatus: TextView
    private lateinit var tvDeliveryAddress: TextView
    private lateinit var layoutOrderItems: LinearLayout
    private lateinit var tvTotalAmount: TextView

    private lateinit var orderDao: OrderDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

        supportActionBar?.hide()

        initializeViews()
        setupToolbar()
        loadOrderDetails()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        tvOrderId = findViewById(R.id.tvOrderId)
        tvOrderDate = findViewById(R.id.tvOrderDate)
        tvOrderStatus = findViewById(R.id.tvOrderStatus)
        tvDeliveryAddress = findViewById(R.id.tvDeliveryAddress)
        layoutOrderItems = findViewById(R.id.layoutOrderItems)
        tvTotalAmount = findViewById(R.id.tvTotalAmount)

        orderDao = OrderDao(this)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadOrderDetails() {
        val orderId = intent.getIntExtra("ORDER_ID", -1)
        val order = orderDao.getOrderById(orderId)

        if (order != null) {
            tvOrderId.text = "Order #${order.orderId}"
            tvOrderDate.text = DateUtils.formatDateTime(order.orderDate)
            tvOrderStatus.text = "Status: ${order.status}"
            tvDeliveryAddress.text = order.deliveryAddress
            tvTotalAmount.text = CurrencyUtils.formatPrice(order.totalAmount)

            // Set status color
            val statusColor = when (order.status) {
                "Pending" -> R.color.pizza_orange
                "Preparing" -> R.color.pizza_yellow
                "Out for Delivery" -> R.color.pizza_orange
                "Delivered" -> R.color.success
                "Cancelled" -> R.color.error
                else -> R.color.text_secondary
            }
            tvOrderStatus.setTextColor(getColor(statusColor))

            // Add order items
            order.items.forEach { item ->
                val itemView = LayoutInflater.from(this)
                    .inflate(R.layout.item_order_item_detail, layoutOrderItems, false)

                val tvItemName = itemView.findViewById<TextView>(R.id.tvItemName)
                val tvItemDetails = itemView.findViewById<TextView>(R.id.tvItemDetails)
                val tvItemPrice = itemView.findViewById<TextView>(R.id.tvItemPrice)

                tvItemName.text = "${item.quantity}x ${item.itemName}"

                val details = buildString {
                    append("Size: ${item.size} | Crust: ${item.crust}")
                    if (item.toppings.isNotEmpty()) {
                        append("\nToppings: ${item.toppings.joinToString(", ")}")
                    }
                }
                tvItemDetails.text = details

                val totalPrice = item.itemPrice * item.quantity
                tvItemPrice.text = CurrencyUtils.formatPrice(totalPrice)

                layoutOrderItems.addView(itemView)
            }
        }
    }
}