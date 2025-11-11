package com.example.romanspizza.ui.auth


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback  // ADD THIS IMPORT
import androidx.appcompat.app.AppCompatActivity
import com.example.romanspizza.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.example.romanspizza.ui.main.MainActivity  // CORRECT IMPORT
import com.example.romanspizza.data.database.UserDao
import com.example.romanspizza.utils.SharedPrefsManager
import com.example.romanspizza.utils.ValidationUtils

class LoginActivity : AppCompatActivity() {

    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton

    private lateinit var userDao: UserDao
    private lateinit var sharedPrefsManager: SharedPrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Hide action bar
        supportActionBar?.hide()

        // Initialize
        userDao = UserDao(this)
        sharedPrefsManager = SharedPrefsManager(this)

        // Setup modern back press handler
        setupBackPressHandler()

        // Bind views
        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)

        // Set click listeners
        btnLogin.setOnClickListener { attemptLogin() }

        findViewById<android.widget.TextView>(R.id.tvSignUp).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        findViewById<android.widget.TextView>(R.id.tvForgotPassword).setOnClickListener {
            Toast.makeText(this, "Feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    // FIXED: Modern back press handler
    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Prevent going back to splash - exit app instead
                finishAffinity()
            }
        })
    }

    private fun attemptLogin() {
        // Clear previous errors
        tilEmail.error = null
        tilPassword.error = null

        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Validate inputs
        var isValid = true

        if (email.isEmpty()) {
            tilEmail.error = getString(R.string.error_empty_email)
            isValid = false
        } else if (!ValidationUtils.isValidEmail(email)) {
            tilEmail.error = getString(R.string.error_invalid_email)
            isValid = false
        }

        if (password.isEmpty()) {
            tilPassword.error = getString(R.string.error_empty_password)
            isValid = false
        }

        if (!isValid) return

        // Disable button during login
        btnLogin.isEnabled = false
        btnLogin.text = "Logging in..."

        // Attempt login
        val user = userDao.loginUser(email, password)

        if (user != null) {
            // Login successful
            sharedPrefsManager.saveUserSession(user.id, user.fullName, user.email)

            Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_SHORT).show()

            // Navigate to main activity
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else {
            // Login failed
            btnLogin.isEnabled = true
            btnLogin.text = getString(R.string.login)

            tilPassword.error = getString(R.string.login_failed)
            Toast.makeText(this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
        }
    }
}