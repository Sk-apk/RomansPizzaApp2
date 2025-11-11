package com.example.romanspizza.ui.menu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.example.romanspizza.R
import com.example.romanspizza.data.model.MenuItem
import com.example.romanspizza.utils.CurrencyUtils

class MenuAdapter(
    private var menuItems: List<MenuItem>,
    private val onItemClick: (MenuItem) -> Unit
) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    inner class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView.findViewById(R.id.cardMenuItem)
        val tvName: TextView = itemView.findViewById(R.id.tvPizzaName)
        val tvDescription: TextView = itemView.findViewById(R.id.tvPizzaDescription)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPizzaPrice)
        val tvCategory: TextView = itemView.findViewById(R.id.tvPizzaCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu_pizza, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val item = menuItems[position]

        holder.tvName.text = item.name
        holder.tvDescription.text = item.description
        holder.tvPrice.text = "From ${CurrencyUtils.formatPrice(item.basePrice)}"
        holder.tvCategory.text = item.category

        holder.cardView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = menuItems.size

    fun updateItems(newItems: List<MenuItem>) {
        menuItems = newItems
        notifyDataSetChanged()
    }
}
