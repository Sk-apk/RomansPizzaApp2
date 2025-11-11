package com.example.romanspizza.ui.menu

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.example.romanspizza.R
import com.example.romanspizza.data.database.CartDao
import com.example.romanspizza.data.database.MenuDao
import com.example.romanspizza.data.model.MenuItem
import com.example.romanspizza.ui.auth.LoginActivity
import com.example.romanspizza.ui.cart.CartActivity
import com.example.romanspizza.utils.SharedPrefsManager

class MenuActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var chipGroupCategories: ChipGroup
    private lateinit var rvMenuItems: RecyclerView
    private lateinit var layoutEmpty: View
    private lateinit var btnCart: ImageButton
    private lateinit var tvCartBadge: TextView

    private lateinit var menuDao: MenuDao
    private lateinit var cartDao: CartDao
    private lateinit var sharedPrefsManager: SharedPrefsManager
    private lateinit var menuAdapter: MenuAdapter

    private var allMenuItems = listOf<MenuItem>()
    private var currentCategory = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        initializeViews()
        setupToolbar()
        loadMenuData()
        setupCategoryFilters()
        updateCartBadge()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        chipGroupCategories = findViewById(R.id.chipGroupCategories)
        rvMenuItems = findViewById(R.id.rvMenuItems)
        layoutEmpty = findViewById(R.id.layoutEmpty)
        btnCart = findViewById(R.id.btnCart)
        tvCartBadge = findViewById(R.id.tvCartBadge)

        menuDao = MenuDao(this)
        cartDao = CartDao(this)
        sharedPrefsManager = SharedPrefsManager(this)

        rvMenuItems.layoutManager = LinearLayoutManager(this)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)

        // Add menu button
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_sort_by_size)
        toolbar.setNavigationOnClickListener {
            showMenuOptions()
        }

        btnCart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
    }

    private fun showMenuOptions() {
        val options = arrayOf("Profile", "Order History", "Settings", "Logout")

        AlertDialog.Builder(this)
            .setTitle("Menu")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showToast("Profile - Coming soon!")
                    1 -> showToast("Order History - Coming soon!")
                    2 -> showToast("Settings - Coming soon!")
                    3 -> performLogout()
                }
            }
            .show()
    }

    private fun loadMenuData() {
        allMenuItems = menuDao.getAllMenuItems()

        if (allMenuItems.isEmpty()) {
            layoutEmpty.visibility = View.VISIBLE
            rvMenuItems.visibility = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            rvMenuItems.visibility = View.VISIBLE

            menuAdapter = MenuAdapter(allMenuItems) { menuItem ->
                openPizzaDetail(menuItem)
            }
            rvMenuItems.adapter = menuAdapter
        }
    }

    private fun setupCategoryFilters() {
        val categories = listOf("All") + menuDao.getCategories()

        categories.forEach { category ->
            val chip = Chip(this).apply {
                text = category
                isCheckable = true
                isChecked = (category == "All")
                setOnClickListener {
                    currentCategory = category
                    filterMenuItems(category)
                }
            }
            chipGroupCategories.addView(chip)
        }
    }

    private fun filterMenuItems(category: String) {
        val filteredItems = if (category == "All") {
            allMenuItems
        } else {
            allMenuItems.filter { it.category == category }
        }

        menuAdapter.updateItems(filteredItems)
    }

    private fun openPizzaDetail(menuItem: MenuItem) {
        val intent = Intent(this, PizzaDetailActivity::class.java)
        intent.putExtra("ITEM_ID", menuItem.id)
        startActivity(intent)
    }

    private fun updateCartBadge() {
        val userId = sharedPrefsManager.getUserId()
        val itemCount = cartDao.getCartItemCount(userId)

        if (itemCount > 0) {
            tvCartBadge.visibility = View.VISIBLE
            tvCartBadge.text = if (itemCount > 9) "9+" else itemCount.toString()
        } else {
            tvCartBadge.visibility = View.GONE
        }
    }

    private fun performLogout() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                sharedPrefsManager.clearUserSession()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        updateCartBadge()
    }

     fun OnBackPressedDispatcher() {
        AlertDialog.Builder(this)
            .setTitle("Exit App")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Yes") { _, _ ->
                finishAffinity()
            }
            .setNegativeButton("No", null)
            .show()
    }
}
