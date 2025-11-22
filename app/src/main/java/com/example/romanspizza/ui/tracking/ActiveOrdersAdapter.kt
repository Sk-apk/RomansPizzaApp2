package com.example.romanspizza.ui.tracking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.romanspizza.R
import com.example.romanspizza.data.model.Order
import com.example.romanspizza.utils.CurrencyUtils
import com.example.romanspizza.utils.DateUtils
import com.example.romanspizza.utils.OrderStatusUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class ActiveOrdersAdapter(
    private val orders: List<Order>,
    private val onTrackOrder: (Order) -> Unit
) : RecyclerView.Adapter<ActiveOrdersAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView.findViewById(R.id.cardOrder)
        val tvOrderId: TextView = itemView.findViewById(R.id.tvOrderId)
        val tvOrderDate: TextView = itemView.findViewById(R.id.tvOrderDate)
        val tvOrderTotal: TextView = itemView.findViewById(R.id.tvOrderTotal)
        val tvOrderStatus: TextView = itemView.findViewById(R.id.tvOrderStatus)
        val tvStatusIcon: TextView = itemView.findViewById(R.id.tvStatusIcon)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        val tvProgress: TextView = itemView.findViewById(R.id.tvProgress)
        val btnTrackOrder: MaterialButton = itemView.findViewById(R.id.btnTrackOrder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_active_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        holder.tvOrderId.text = "Order #${order.orderId}"
        holder.tvOrderDate.text = DateUtils.getRelativeTime(order.orderDate)
        holder.tvOrderTotal.text = CurrencyUtils.formatPrice(order.totalAmount)
        holder.tvOrderStatus.text = OrderStatusUtils.getStatusDisplayName(order.status)
        holder.tvStatusIcon.text = OrderStatusUtils.getStatusIcon(order.status)

        // Set progress
        val progress = OrderStatusUtils.getStatusProgress(order.status)
        holder.progressBar.progress = progress
        holder.tvProgress.text = "$progress%"

        // Set status color
        val statusColor = OrderStatusUtils.getStatusColor(order.status)
        holder.tvOrderStatus.setTextColor(holder.itemView.context.getColor(statusColor))

        holder.btnTrackOrder.setOnClickListener {
            onTrackOrder(order)
        }
    }

    override fun getItemCount() = orders.size
}