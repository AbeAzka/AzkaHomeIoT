package com.indodevstudio.azka_home_iot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Thread.sleep(2000)
        installSplashScreen()
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        setContentView(R.layout.activity_splash)
    }
}