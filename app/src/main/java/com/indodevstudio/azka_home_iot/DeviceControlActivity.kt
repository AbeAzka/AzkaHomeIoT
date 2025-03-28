package com.indodevstudio.azka_home_iot

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import okhttp3.*
import java.io.IOException

class DeviceControlActivity : AppCompatActivity() {

    private lateinit var textViewStatus: TextView
    private lateinit var buttonOn: Button
    private lateinit var buttonOff: Button
    private lateinit var buttonOn2: Button
    private lateinit var buttonOff2: Button

    private val client = OkHttpClient()
    private lateinit var baseUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_control)

        textViewStatus = findViewById(R.id.textViewStatus)
        buttonOn = findViewById(R.id.buttonOn)
        buttonOff = findViewById(R.id.buttonOff)
        buttonOn2 = findViewById(R.id.buttonOn2)
        buttonOff2 = findViewById(R.id.buttonOff2)

        val deviceName = intent.getStringExtra("deviceName") ?: "Unknown Device"
        baseUrl = "http://taryem.my.id/Lab01/labx.php"
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()?.setDisplayShowHomeEnabled(true);
        supportActionBar?.title = "Control $deviceName"

        buttonOn.setOnClickListener { sendCommand("on") }
        buttonOff.setOnClickListener { sendCommand("off") }

        buttonOn2.setOnClickListener { sendCommand("on2") }
        buttonOff2.setOnClickListener { sendCommand("off2") }

        toolbar.setNavigationOnClickListener(View.OnClickListener {
            //What to do on back clicked
            onBackPressed()
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fragment_container, manual_book_fragment()).commit()
        onBackPressedDispatcher.onBackPressed()
    }

    private fun sendCommand(command: String) {
        val url = "$baseUrl?type=$command"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@DeviceControlActivity, "Failed to send command", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(this@DeviceControlActivity, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        runOnUiThread {
                            textViewStatus.text = "Last Command: $command"
                            Toast.makeText(this@DeviceControlActivity, "Command $command sent", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }
}
