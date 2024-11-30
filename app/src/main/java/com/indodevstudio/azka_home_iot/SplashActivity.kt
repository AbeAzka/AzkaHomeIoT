package com.indodevstudio.azka_home_iot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        /*super.onCreate(savedInstanceState)
        Thread.sleep(3000)
        installSplashScreen()
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        setContentView(R.layout.activity_splash)*/
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)


        // Simulate a loading process (adjust delay as needed)
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }
}