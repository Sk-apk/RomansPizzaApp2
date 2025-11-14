package com.example.romanspizza.ui.profile

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.romanspizza.R
import com.example.romanspizza.data.database.UserDao
import com.example.romanspizza.utils.SharedPrefsManager
import com.example.romanspizza.utils.ValidationUtils
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class EditProfileActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tilFullName: TextInputLayout
    private lateinit var tilPhone: TextInputLayout
    private lateinit var tilAddress: TextInputLayout
    private lateinit var etFullName: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etAddress: TextInputEditText
    private lateinit var btnSave: MaterialButton

    private lateinit var userDao: UserDao
    private lateinit var sharedPrefsManager: SharedPrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        supportActionBar?.hide()

        initializeViews()
        setupToolbar()
        loadCurrentData()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        tilFullName = findViewById(R.id.tilFullName)
        tilPhone = findViewById(R.id.tilPhone)
        tilAddress = findViewById(R.id.tilAddress)
        etFullName = findViewById(R.id.etFullName)
        etPhone = findViewById(R.id.etPhone)
        etAddress = findViewById(R.id.etAddress)
        btnSave = findViewById(R.id.btnSave)

        userDao = UserDao(this)
        sharedPrefsManager = SharedPrefsManager(this)

        btnSave.setOnClickListener {
            saveProfile()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadCurrentData() {
        val userId = sharedPrefsManager.getUserId()
        val user = userDao.getUserById(userId)

        if (user != null) {
            etFullName.setText(user.fullName)
            etPhone.setText(user.phone)
            etAddress.setText(user.address)
        }
    }

    private fun saveProfile() {
        // Clear errors
        tilFullName.error = null
        tilPhone.error = null
        tilAddress.error = null

        val fullName = etFullName.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val address = etAddress.text.toString().trim()

        // Validate
        var isValid = true

        if (fullName.isEmpty()) {
            tilFullName.error = getString(R.string.error_empty_name)
            isValid = false
        } else if (!ValidationUtils.isValidName(fullName)) {
            tilFullName.error = "Name must be at least 2 characters"
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

        if (!isValid) return

        // Save changes
        btnSave.isEnabled = false
        btnSave.text = "Saving..."

        val userId = sharedPrefsManager.getUserId()
        val success = userDao.updateUser(userId, fullName, phone, address)

        if (success) {
            // Update shared preferences
            sharedPrefsManager.saveUserSession(userId, fullName, sharedPrefsManager.getUserEmail() ?: "")

            Toast.makeText(this, getString(R.string.profile_updated), Toast.LENGTH_SHORT).show()
            finish()
        } else {
            btnSave.isEnabled = true
            btnSave.text = getString(R.string.save_changes)
            Toast.makeText(this, getString(R.string.update_failed), Toast.LENGTH_SHORT).show()
        }
    }
}