package com.example.romanspizza.ui.auth


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.romanspizza.ui.main.MainActivity
import com.example.romanspizza.utils.SharedPrefsManager
import com.example.romanspizza.R


class SplashActivity : AppCompatActivity() {

    private lateinit var sharedPrefsManager: SharedPrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Hide action bar
        supportActionBar?.hide()

        sharedPrefsManager = SharedPrefsManager(this)

        // Delay for 2 seconds then check login status
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, 2000)
    }

    private fun navigateToNextScreen() {
        val intent = if (sharedPrefsManager.isLoggedIn()) {
            // User is logged in, go to main activity
            Intent(this, MainActivity::class.java)
        } else {
            // User not logged in, go to login
            Intent(this, LoginActivity::class.java)
        }

        startActivity(intent)
        finish()
    }
}
