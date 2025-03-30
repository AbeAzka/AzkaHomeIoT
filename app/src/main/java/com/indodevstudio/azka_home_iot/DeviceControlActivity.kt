package com.indodevstudio.azka_home_iot

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException


class DeviceControlActivity : AppCompatActivity() {

    private lateinit var textViewStatus: TextView
    private lateinit var buttonOn: Button
    private lateinit var buttonOff: Button
    private lateinit var buttonOn2: Button
    private lateinit var buttonOff2: Button

    private lateinit var textSwitch1 : TextView
    private lateinit var textSwitch2: TextView

    private val client = OkHttpClient()
    private lateinit var baseUrl: String

    var device_id = ""

    var sharedPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_control)

        textViewStatus = findViewById(R.id.textViewStatus)
        buttonOn = findViewById(R.id.buttonOn)
        buttonOff = findViewById(R.id.buttonOff)
        buttonOn2 = findViewById(R.id.buttonOn2)
        buttonOff2 = findViewById(R.id.buttonOff2)

        textSwitch2 = findViewById(R.id.textSwitch2)
        textSwitch1 = findViewById(R.id.textSwitch1)

        val sharedPreferences2 = getSharedPreferences("DevicePrefs", Context.MODE_PRIVATE)
        device_id = sharedPreferences2?.getString("device_id", null).toString()
        sharedPreferences = getSharedPreferences("MyPrefs", 0)

        // 🔹 Set teks yang tersimpan
        textSwitch1.text = sharedPreferences?.getString("switch1", "Switch 1")
        textSwitch2.text = sharedPreferences?.getString("switch2", "Switch 2")
        textSwitch1.setOnClickListener { v: View? ->
            showEditDialog(
                textSwitch1, "switch1"
            )
        }
        textSwitch2.setOnClickListener { v: View? ->
            showEditDialog(
                textSwitch2, "switch2"
            )
        }

        val deviceName = intent.getStringExtra("deviceName") ?: "Unknown Device"
        baseUrl = "http://taryem.my.id/Lab01/ahi.php"
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()?.setDisplayShowHomeEnabled(true);
        supportActionBar?.title = "Control $deviceName"

        buttonOn.setOnClickListener { sendCommand("on", device_id) }
        buttonOff.setOnClickListener { sendCommand("off", device_id) }

        buttonOn2.setOnClickListener { sendCommand("on2", device_id) }
        buttonOff2.setOnClickListener { sendCommand("off2", device_id) }

        toolbar.setNavigationOnClickListener(View.OnClickListener {
            //What to do on back clicked
            onBackPressed()
        })
    }

    private fun showEditDialog(textView: TextView, key: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Nama")
        val input = EditText(this)
        input.setText(textView.text.toString())
        builder.setView(input)
        builder.setPositiveButton("OK") { dialog: DialogInterface?, which: Int ->
            val newText = input.text.toString()
            textView.text = newText

            // 🔹 Simpan teks yang diedit ke SharedPreferences

            val editor = sharedPreferences?.edit()
            editor?.putString(key, newText)
            editor?.apply()

        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog: DialogInterface, which: Int -> dialog.cancel() }
        builder.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fragment_container, manual_book_fragment()).commit()
        onBackPressedDispatcher.onBackPressed()
    }

    private fun sendCommand(command: String, deviceID : String) {
        val url = "$baseUrl?type=$command&device_id=$deviceID"

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
