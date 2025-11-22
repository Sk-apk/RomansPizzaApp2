package com.example.romanspizza.ui.tracking

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.romanspizza.R
import com.example.romanspizza.data.database.OrderDao
import com.example.romanspizza.utils.CurrencyUtils
import com.example.romanspizza.utils.DateUtils
import com.example.romanspizza.utils.OrderStatusUtils
import com.google.android.material.appbar.MaterialToolbar

class OrderTrackingActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvOrderId: TextView
    private lateinit var tvOrderDate: TextView
    private lateinit var tvEstimatedTime: TextView
    private lateinit var tvOrderTotal: TextView
    private lateinit var tvItemCount: TextView
    private lateinit var tvDeliveryAddress: TextView
    private lateinit var layoutTimeline: LinearLayout

    private lateinit var orderDao: OrderDao
    private val handler = Handler(Looper.getMainLooper())
    private var orderId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_tracking)

        supportActionBar?.hide()

        orderId = intent.getIntExtra("ORDER_ID", -1)

        initializeViews()
        setupToolbar()
        loadOrderTracking()

        // Auto-refresh every 30 seconds (simulate real-time updates)
        startAutoRefresh()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        tvOrderId = findViewById(R.id.tvOrderId)
        tvOrderDate = findViewById(R.id.tvOrderDate)
        tvEstimatedTime = findViewById(R.id.tvEstimatedTime)
        tvOrderTotal = findViewById(R.id.tvOrderTotal)
        tvItemCount = findViewById(R.id.tvItemCount)
        tvDeliveryAddress = findViewById(R.id.tvDeliveryAddress)
        layoutTimeline = findViewById(R.id.layoutTimeline)

        orderDao = OrderDao(this)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadOrderTracking() {
        val order = orderDao.getOrderById(orderId)

        if (order != null) {
            tvOrderId.text = "Order #${order.orderId}"
            tvOrderDate.text = "Placed ${DateUtils.getRelativeTime(order.orderDate)}"
            tvOrderTotal.text = CurrencyUtils.formatPrice(order.totalAmount)
            tvDeliveryAddress.text = order.deliveryAddress

            // Calculate estimated time
            val estimatedTime = orderDao.getEstimatedDeliveryTime(order.orderDate, order.status)
            tvEstimatedTime.text = estimatedTime

            // Item count
            val itemCount = order.items.sumOf { it.quantity }
            tvItemCount.text = "$itemCount item${if (itemCount > 1) "s" else ""}"

            // Build timeline
            buildStatusTimeline(order.status)
        }
    }

    private fun buildStatusTimeline(currentStatus: String) {
        layoutTimeline.removeAllViews()

        val allStatuses = OrderStatusUtils.getAllStatuses()
        val currentStep = orderDao.getOrderStatusStep(currentStatus)

        allStatuses.forEachIndexed { index, status ->
            val statusStep = orderDao.getOrderStatusStep(status)
            val isCompleted = statusStep <= currentStep
            val isCurrent = status == currentStatus

            val timelineItemView = LayoutInflater.from(this)
                .inflate(R.layout.item_timeline_status, layoutTimeline, false)

            val tvStatusIcon = timelineItemView.findViewById<TextView>(R.id.tvStatusIcon)
            val viewLine = timelineItemView.findViewById<View>(R.id.viewLine)
            val tvStatusTitle = timelineItemView.findViewById<TextView>(R.id.tvStatusTitle)
            val tvStatusDescription = timelineItemView.findViewById<TextView>(R.id.tvStatusDescription)

            // Set icon
            tvStatusIcon.text = OrderStatusUtils.getStatusIcon(status)

            // Set colors based on status
            if (isCompleted) {
                val color = ContextCompat.getColor(this, OrderStatusUtils.getStatusColor(status))
                tvStatusIcon.setBackgroundResource(R.drawable.bg_button)
                tvStatusIcon.backgroundTintList = android.content.res.ColorStateList.valueOf(color)
                tvStatusIcon.setTextColor(Color.WHITE)
                viewLine.setBackgroundColor(color)
            } else {
                tvStatusIcon.setBackgroundResource(R.drawable.bg_input_field)
                tvStatusIcon.setTextColor(ContextCompat.getColor(this, R.color.text_hint))
                viewLine.setBackgroundColor(ContextCompat.getColor(this, R.color.text_hint))
            }

            // Set text
            tvStatusTitle.text = OrderStatusUtils.getStatusDisplayName(status)
            tvStatusDescription.text = OrderStatusUtils.getStatusDescription(status)

            // Style current status
            if (isCurrent) {
                tvStatusTitle.setTextColor(ContextCompat.getColor(this, R.color.pizza_red))
                tvStatusTitle.textSize = 16f
            }

            // Hide line for last item
            if (index == allStatuses.size - 1) {
                viewLine.visibility = View.GONE
            }

            layoutTimeline.addView(timelineItemView)
        }
    }

    private fun startAutoRefresh() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                loadOrderTracking()
                handler.postDelayed(this, 30000) // Refresh every 30 seconds
            }
        }, 30000)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
