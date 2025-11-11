package com.example.romanspizza.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.example.romanspizza.R
import com.example.romanspizza.data.database.UserDao
import com.example.romanspizza.data.model.User
import com.example.romanspizza.ui.main.MainActivity
import com.example.romanspizza.utils.SharedPrefsManager
import com.example.romanspizza.utils.ValidationUtils

class RegisterActivity : AppCompatActivity() {

    private lateinit var tilFullName: TextInputLayout
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPhone: TextInputLayout
    private lateinit var tilAddress: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout

    private lateinit var etFullName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etAddress: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText

    private lateinit var btnRegister: MaterialButton

    private lateinit var userDao: UserDao
    private lateinit var sharedPrefsManager: SharedPrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Hide action bar
        supportActionBar?.hide()

        // Initialize
        userDao = UserDao(this)
        sharedPrefsManager = SharedPrefsManager(this)

        // Bind views
        tilFullName = findViewById(R.id.tilFullName)
        tilEmail = findViewById(R.id.tilEmail)
        tilPhone = findViewById(R.id.tilPhone)
        tilAddress = findViewById(R.id.tilAddress)
        tilPassword = findViewById(R.id.tilPassword)
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword)

        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etAddress = findViewById(R.id.etAddress)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)

        btnRegister = findViewById(R.id.btnRegister)

        // Set click listeners
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        btnRegister.setOnClickListener { attemptRegistration() }

        findViewById<android.widget.TextView>(R.id.tvLogin).setOnClickListener {
            finish()
        }
    }

    private fun attemptRegistration() {
        // Clear previous errors
        clearErrors()

        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val address = etAddress.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        // Validate inputs
        var isValid = true

        if (fullName.isEmpty()) {
            tilFullName.error = getString(R.string.error_empty_name)
            isValid = false
        } else if (!ValidationUtils.isValidName(fullName)) {
            tilFullName.error = "Name must be at least 2 characters"
            isValid = false
        }

        if (email.isEmpty()) {
            tilEmail.error = getString(R.string.error_empty_email)
            isValid = false
        } else if (!ValidationUtils.isValidEmail(email)) {
            tilEmail.error = getString(R.string.error_invalid_email)
            isValid = false
        } else if (userDao.isEmailExists(email)) {
            tilEmail.error = getString(R.string.email_exists)
            isValid = false
        }

        if (phone.isEmpty()) {
            tilPhone.error = getString(R.string.error_empty_phone)
            isValid = false
        } else if (!ValidationUtils.isValidPhone(phone)) {
            tilPhone.error = getString(R.string.error_invalid_phone)
            isValid = false
        }

        if (address.isEmpty()) {
            tilAddress.error = getString(R.string.error_empty_address)
            isValid = false
        } else if (!ValidationUtils.isValidAddress(address)) {
            tilAddress.error = "Address must be at least 5 characters"
            isValid = false
        }

        if (password.isEmpty()) {
            tilPassword.error = getString(R.string.error_empty_password)
            isValid = false
        } else if (!ValidationUtils.isValidPassword(password)) {
            tilPassword.error = getString(R.string.error_short_password)
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.error = "Please confirm password"
            isValid = false
        } else if (password != confirmPassword) {
            tilConfirmPassword.error = getString(R.string.error_password_mismatch)
            isValid = false
        }

        if (!isValid) return

        // Disable button during registration
        btnRegister.isEnabled = false
        btnRegister.text = "Creating Account..."

        // Create user
        val user = User(
            fullName = fullName,
            email = email,
            phone = phone,
            address = address,
            password = password
        )

        val userId = userDao.registerUser(user)

        if (userId > 0) {
            // Registration successful
            sharedPrefsManager.saveUserSession(userId.toInt(), fullName, email)

            Toast.makeText(this, getString(R.string.register_success), Toast.LENGTH_SHORT).show()

            // Navigate to main activity
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else {
            // Registration failed
            btnRegister.isEnabled = true
            btnRegister.text = getString(R.string.register)

            Toast.makeText(this, getString(R.string.register_failed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearErrors() {
        tilFullName.error = null
        tilEmail.error = null
        tilPhone.error = null
        tilAddress.error = null
        tilPassword.error = null
        tilConfirmPassword.error = null
    }
}

