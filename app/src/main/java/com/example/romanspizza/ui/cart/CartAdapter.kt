package com.example.romanspizza.ui.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.romanspizza.R
import com.example.romanspizza.data.model.CartItem
import com.example.romanspizza.utils.CurrencyUtils

class CartAdapter(
    private var cartItems: MutableList<CartItem>,
    private val onQuantityChange: (CartItem, Int) -> Unit,
    private val onRemoveItem: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvItemName: TextView = itemView.findViewById(R.id.tvCartItemName)
        val tvItemDetails: TextView = itemView.findViewById(R.id.tvCartItemDetails)
        val tvItemPrice: TextView = itemView.findViewById(R.id.tvCartItemPrice)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        val btnDecrease: ImageButton = itemView.findViewById(R.id.btnDecrease)
        val btnIncrease: ImageButton = itemView.findViewById(R.id.btnIncrease)
        val btnRemove: ImageButton = itemView.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartItems[position]

        holder.tvItemName.text = item.menuItem.name

        val details = buildString {
            append("Size: ${item.size}")
            append(" | Crust: ${item.crust}")
            if (item.toppings.isNotEmpty()) {
                append("\nToppings: ${item.toppings.joinToString(", ")}")
            }
        }
        holder.tvItemDetails.text = details

        holder.tvItemPrice.text = CurrencyUtils.formatPrice(item.getTotalPrice())
        holder.tvQuantity.text = item.quantity.toString()

        holder.btnIncrease.setOnClickListener {
            onQuantityChange(item, item.quantity + 1)
        }

        holder.btnDecrease.setOnClickListener {
            if (item.quantity > 1) {
                onQuantityChange(item, item.quantity - 1)
            }
        }

        holder.btnRemove.setOnClickListener {
            onRemoveItem(item)
        }
    }

    override fun getItemCount() = cartItems.size

    fun updateItems(newItems: List<CartItem>) {
        cartItems = newItems.toMutableList()
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        cartItems.removeAt(position)
        notifyItemRemoved(position)
    }
}