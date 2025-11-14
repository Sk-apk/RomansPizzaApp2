package com.example.romanspizza.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.romanspizza.R
import com.example.romanspizza.data.model.Order
import com.example.romanspizza.utils.CurrencyUtils
import com.example.romanspizza.utils.DateUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class OrderHistoryAdapter(
    private val orders: List<Order>,
    private val onViewDetails: (Order) -> Unit,
    private val onReorder: (Order) -> Unit
) : RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView.findViewById(R.id.cardOrder)
        val tvOrderId: TextView = itemView.findViewById(R.id.tvOrderId)
        val tvOrderDate: TextView = itemView.findViewById(R.id.tvOrderDate)
        val tvOrderTotal: TextView = itemView.findViewById(R.id.tvOrderTotal)
        val tvOrderStatus: TextView = itemView.findViewById(R.id.tvOrderStatus)
        val tvItemCount: TextView = itemView.findViewById(R.id.tvItemCount)
        val btnViewDetails: MaterialButton = itemView.findViewById(R.id.btnViewDetails)
        val btnReorder: MaterialButton = itemView.findViewById(R.id.btnReorder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_history, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        holder.tvOrderId.text = "Order #${order.orderId}"
        holder.tvOrderDate.text = DateUtils.formatDateTime(order.orderDate)
        holder.tvOrderTotal.text = CurrencyUtils.formatPrice(order.totalAmount)
        holder.tvOrderStatus.text = order.status

        val itemCount = order.items.sumOf { it.quantity }
        holder.tvItemCount.text = "$itemCount item${if (itemCount > 1) "s" else ""}"

        // Set status color
        val statusColor = when (order.status) {
            "Pending" -> R.color.pizza_orange
            "Preparing" -> R.color.pizza_yellow
            "Out for Delivery" -> R.color.pizza_orange
            "Delivered" -> R.color.success
            "Cancelled" -> R.color.error
            else -> R.color.text_secondary
        }
        holder.tvOrderStatus.setTextColor(holder.itemView.context.getColor(statusColor))

        holder.btnViewDetails.setOnClickListener {
            onViewDetails(order)
        }

        holder.btnReorder.setOnClickListener {
            onReorder(order)
        }
    }

    override fun getItemCount() = orders.size
}