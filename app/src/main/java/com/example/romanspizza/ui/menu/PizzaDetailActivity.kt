package com.example.romanspizza.ui.menu

import android.os.Bundle
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.example.romanspizza.R
import com.example.romanspizza.data.database.CartDao
import com.example.romanspizza.data.database.MenuDao
import com.example.romanspizza.data.model.CartItem
import com.example.romanspizza.data.model.MenuItem
import com.example.romanspizza.utils.CurrencyUtils
import com.example.romanspizza.utils.PizzaCustomization
import com.example.romanspizza.utils.SharedPrefsManager

class PizzaDetailActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var tvPizzaName: TextView
    private lateinit var tvPizzaDescription: TextView
    private lateinit var tvBasePrice: TextView
    private lateinit var rgSize: RadioGroup
    private lateinit var rgCrust: RadioGroup
    private lateinit var layoutToppings: LinearLayout
    private lateinit var tvTotalPrice: TextView
    private lateinit var btnAddToCart: MaterialButton

    private lateinit var menuDao: MenuDao
    private lateinit var cartDao: CartDao
    private lateinit var sharedPrefsManager: SharedPrefsManager

    private var menuItem: MenuItem? = null
    private var selectedSize: PizzaCustomization.PizzaSize = PizzaCustomization.SIZES[0]
    private var selectedCrust: PizzaCustomization.PizzaCrust = PizzaCustomization.CRUSTS[0]
    private val selectedToppings = mutableListOf<PizzaCustomization.Topping>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pizza_detail)

        initializeViews()
        loadMenuItemData()
        setupSizeOptions()
        setupCrustOptions()
        setupToppingOptions()
        updateTotalPrice()
    }

    private fun initializeViews() {
        btnBack = findViewById(R.id.btnBack)
        tvPizzaName = findViewById(R.id.tvPizzaName)
        tvPizzaDescription = findViewById(R.id.tvPizzaDescription)
        tvBasePrice = findViewById(R.id.tvBasePrice)
        rgSize = findViewById(R.id.rgSize)
        rgCrust = findViewById(R.id.rgCrust)
        layoutToppings = findViewById(R.id.layoutToppings)
        tvTotalPrice = findViewById(R.id.tvTotalPrice)
        btnAddToCart = findViewById(R.id.btnAddToCart)

        menuDao = MenuDao(this)
        cartDao = CartDao(this)
        sharedPrefsManager = SharedPrefsManager(this)

        btnBack.setOnClickListener { finish() }
        btnAddToCart.setOnClickListener { addToCart() }
    }

    private fun loadMenuItemData() {
        val itemId = intent.getIntExtra("ITEM_ID", -1)
        menuItem = menuDao.getMenuItemById(itemId)

        menuItem?.let {
            tvPizzaName.text = it.name
            tvPizzaDescription.text = it.description
            tvBasePrice.text = "Base Price: ${CurrencyUtils.formatPrice(it.basePrice)}"
        } ?: run {
            Toast.makeText(this, "Error loading pizza", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupSizeOptions() {
        PizzaCustomization.SIZES.forEachIndexed { index, size ->
            val radioButton = RadioButton(this).apply {
                text = "${size.name} (${size.description}) ${if (size.additionalPrice > 0) "+ ${CurrencyUtils.formatPrice(size.additionalPrice)}" else ""}"
                id = index
                isChecked = (index == 0)
                setOnClickListener {
                    selectedSize = size
                    updateTotalPrice()
                }
            }
            rgSize.addView(radioButton)
        }
    }

    private fun setupCrustOptions() {
        PizzaCustomization.CRUSTS.forEachIndexed { index, crust ->
            val radioButton = RadioButton(this).apply {
                text = "${crust.name} ${if (crust.additionalPrice > 0) "+ ${CurrencyUtils.formatPrice(crust.additionalPrice)}" else ""}"
                id = index + 100
                isChecked = (index == 0)
                setOnClickListener {
                    selectedCrust = crust
                    updateTotalPrice()
                }
            }
            rgCrust.addView(radioButton)
        }
    }

    private fun setupToppingOptions() {
        PizzaCustomization.TOPPINGS.forEach { topping ->
            val checkBox = CheckBox(this).apply {
                text = "${topping.name} + ${CurrencyUtils.formatPrice(topping.price)}"
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedToppings.add(topping)
                    } else {
                        selectedToppings.remove(topping)
                    }
                    updateTotalPrice()
                }
            }
            layoutToppings.addView(checkBox)
        }
    }

    private fun updateTotalPrice() {
        menuItem?.let {
            val totalPrice = PizzaCustomization.calculateTotalPrice(
                it.basePrice,
                selectedSize,
                selectedCrust,
                selectedToppings
            )
            tvTotalPrice.text = CurrencyUtils.formatPrice(totalPrice)
        }
    }

    private fun addToCart() {
        val item = menuItem ?: return
        val userId = sharedPrefsManager.getUserId()

        val totalPrice = PizzaCustomization.calculateTotalPrice(
            item.basePrice,
            selectedSize,
            selectedCrust,
            selectedToppings
        )

        val cartItem = CartItem(
            userId = userId,
            menuItem = item,
            quantity = 1,
            size = selectedSize.name,
            crust = selectedCrust.name,
            toppings = selectedToppings.map { it.name },
            itemPrice = totalPrice
        )

        val result = cartDao.addToCart(cartItem)

        if (result > 0) {
            Toast.makeText(this, "Added to cart!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to add to cart", Toast.LENGTH_SHORT).show()
        }
    }
}

