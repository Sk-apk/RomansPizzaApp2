package com.example.romanspizza.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.romanspizza.R
import com.example.romanspizza.data.database.UserDao
import com.example.romanspizza.utils.SharedPrefsManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class ProfileActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvUserPhone: TextView
    private lateinit var tvUserAddress: TextView
    private lateinit var btnEditProfile: MaterialButton
    private lateinit var btnOrderHistory: MaterialButton

    private lateinit var userDao: UserDao
    private lateinit var sharedPrefsManager: SharedPrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        supportActionBar?.hide()

        initializeViews()
        setupToolbar()
        loadUserData()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        tvUserPhone = findViewById(R.id.tvUserPhone)
        tvUserAddress = findViewById(R.id.tvUserAddress)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        btnOrderHistory = findViewById(R.id.btnOrderHistory)

        userDao = UserDao(this)
        sharedPrefsManager = SharedPrefsManager(this)

        btnEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        btnOrderHistory.setOnClickListener {
            startActivity(Intent(this, OrderHistoryActivity::class.java))
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadUserData() {
        val userId = sharedPrefsManager.getUserId()
        val user = userDao.getUserById(userId)

        if (user != null) {
            tvUserName.text = user.fullName
            tvUserEmail.text = user.email
            tvUserPhone.text = user.phone
            tvUserAddress.text = user.address
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserData() // Refresh data when returning from edit
    }
}