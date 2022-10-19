package com.application.subhamastu

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat


class SpalshActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Handler(Looper.getMainLooper()).postDelayed({
            window.statusBarColor = resources.getColor(R.color.white, null)
        }, 500)
        WindowCompat.getInsetsController(
            window,
            window.decorView
        )?.isAppearanceLightStatusBars = true
        window.decorView.setBackgroundColor(
            resources.getColor(
                R.color.white,
                null
            )
        )
        setContentView(R.layout.spalsh_activity)
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            val i = Intent(this@SpalshActivity, MainActivity::class.java)
            startActivity(i)
            finish()
        }, 3000)
    }
}