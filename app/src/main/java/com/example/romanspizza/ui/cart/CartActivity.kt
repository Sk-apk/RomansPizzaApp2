package com.example.romanspizza.ui.cart



import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.example.romanspizza.R
import com.example.romanspizza.data.database.CartDao
import com.example.romanspizza.data.database.OrderDao
import com.example.romanspizza.data.database.UserDao
import com.example.romanspizza.data.model.CartItem
import com.example.romanspizza.data.model.Order
import com.example.romanspizza.data.model.OrderItem
import com.example.romanspizza.utils.CurrencyUtils
import com.example.romanspizza.utils.SharedPrefsManager

class CartActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var rvCartItems: RecyclerView
    private lateinit var layoutEmptyCart: LinearLayout
    private lateinit var btnBrowseMenu: MaterialButton
    private lateinit var tvSubtotal: TextView
    private lateinit var tvDeliveryFee: TextView
    private lateinit var tvTotal: TextView
    private lateinit var btnCheckout: MaterialButton

    private lateinit var cartDao: CartDao
    private lateinit var orderDao: OrderDao
    private lateinit var userDao: UserDao
    private lateinit var sharedPrefsManager: SharedPrefsManager
    private lateinit var cartAdapter: CartAdapter

    private var cartItems = mutableListOf<CartItem>()
    private val deliveryFee = 30.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        initializeViews()
        setupToolbar()
        loadCartItems()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        rvCartItems = findViewById(R.id.rvCartItems)
        layoutEmptyCart = findViewById(R.id.layoutEmptyCart)
        btnBrowseMenu = findViewById(R.id.btnBrowseMenu)
        tvSubtotal = findViewById(R.id.tvSubtotal)
        tvDeliveryFee = findViewById(R.id.tvDeliveryFee)
        tvTotal = findViewById(R.id.tvTotal)
        btnCheckout = findViewById(R.id.btnCheckout)

        cartDao = CartDao(this)
        orderDao = OrderDao(this)
        userDao = UserDao(this)
        sharedPrefsManager = SharedPrefsManager(this)

        rvCartItems.layoutManager = LinearLayoutManager(this)

        btnBrowseMenu.setOnClickListener { finish() }
        btnCheckout.setOnClickListener { proceedToCheckout() }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadCartItems() {
        val userId = sharedPrefsManager.getUserId()
        cartItems = cartDao.getCartItems(userId).toMutableList()

        if (cartItems.isEmpty()) {
            showEmptyCart()
        } else {
            showCartItems()
        }
    }

    private fun showEmptyCart() {
        layoutEmptyCart.visibility = View.VISIBLE
        rvCartItems.visibility = View.GONE
        btnCheckout.isEnabled = false
    }

    private fun showCartItems() {
        layoutEmptyCart.visibility = View.GONE
        rvCartItems.visibility = View.VISIBLE
        btnCheckout.isEnabled = true

        cartAdapter = CartAdapter(
            cartItems,
            onQuantityChange = { item, newQuantity ->
                updateQuantity(item, newQuantity)
            },
            onRemoveItem = { item ->
                removeItem(item)
            }
        )
        rvCartItems.adapter = cartAdapter

        updateOrderSummary()
    }

    private fun updateQuantity(item: CartItem, newQuantity: Int) {
        val success = cartDao.updateCartItemQuantity(item.cartId, newQuantity)
        if (success) {
            item.quantity = newQuantity
            cartAdapter.notifyDataSetChanged()
            updateOrderSummary()
        } else {
            Toast.makeText(this, "Failed to update quantity", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeItem(item: CartItem) {
        AlertDialog.Builder(this)
            .setTitle("Remove Item")
            .setMessage("Remove ${item.menuItem.name} from cart?")
            .setPositiveButton("Remove") { _, _ ->
                val success = cartDao.removeFromCart(item.cartId)
                if (success) {
                    cartItems.remove(item)
                    cartAdapter.updateItems(cartItems)

                    if (cartItems.isEmpty()) {
                        showEmptyCart()
                    } else {
                        updateOrderSummary()
                    }

                    Toast.makeText(this, "Item removed", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateOrderSummary() {
        val subtotal = cartItems.sumOf { it.getTotalPrice() }
        val total = subtotal + deliveryFee

        tvSubtotal.text = CurrencyUtils.formatPrice(subtotal)
        tvDeliveryFee.text = CurrencyUtils.formatPrice(deliveryFee)
        tvTotal.text = CurrencyUtils.formatPrice(total)
    }

    private fun proceedToCheckout() {
        val userId = sharedPrefsManager.getUserId()
        val user = userDao.getUserById(userId)

        if (user == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            return
        }

        val subtotal = cartItems.sumOf { it.getTotalPrice() }
        val total = subtotal + deliveryFee

        // Create order
        val order = Order(
            userId = userId,
            orderDate = System.currentTimeMillis(),
            totalAmount = total,
            status = "Pending",
            deliveryAddress = user.address
        )

        // Create order items
        val orderItems = cartItems.map { cartItem ->
            OrderItem(
                orderId = 0,
                itemId = cartItem.menuItem.id,
                itemName = cartItem.menuItem.name,
                quantity = cartItem.quantity,
                size = cartItem.size,
                crust = cartItem.crust,
                toppings = cartItem.toppings,
                itemPrice = cartItem.itemPrice
            )
        }

        // Save order
        val orderId = orderDao.createOrder(order, orderItems)

        if (orderId > 0) {
            // Clear cart
            cartDao.clearCart(userId)

            // Show success dialog with tracking option
            AlertDialog.Builder(this)
                .setTitle("Order Placed!")
                .setMessage("Your order has been placed successfully!\nOrder ID: $orderId\nTotal: ${CurrencyUtils.formatPrice(total)}\n\nDelivering to: ${user.address}")
                .setPositiveButton("Track Order") { _, _ ->
                    // Navigate to order tracking
                    val trackIntent = Intent(this, com.example.romanspizza.ui.tracking.OrderTrackingActivity::class.java)
                    trackIntent.putExtra("ORDER_ID", orderId.toInt())
                    startActivity(trackIntent)
                    finish()
                }
                .setNegativeButton("Back to Menu") { _, _ ->
                    finish()
                }
                .setCancelable(false)
                .show()
        } else {
            Toast.makeText(this, "Failed to place order", Toast.LENGTH_SHORT).show()
        }
    }
}
