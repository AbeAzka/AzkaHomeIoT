package com.indodevstudio.azka_home_iot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.indodevstudio.azka_home_iot.API.DeviceSharingService

class InviteActivity : AppCompatActivity() {
    private lateinit var emailInput: EditText
    private lateinit var inviteButton: Button
    private lateinit var textDeviceID: TextView
    private lateinit var textStats: TextView
    private var deviceID: String? = null // Simpan ID perangkat
    private var devicename: String? = null // Simpan ID perangkat
    private var email: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite)

        emailInput = findViewById(R.id.etUserEmail)
        inviteButton = findViewById(R.id.btnSendInvite)
        textDeviceID = findViewById(R.id.tvDeviceId)
        textStats = findViewById(R.id.tvStats)

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if(firebaseUser != null){
            email = firebaseUser.email
        }
        val userData = getUserData()
        val prefs = getSharedPreferences("my_prefs", MODE_PRIVATE)
        val authToken = prefs.getString("auth_token", null)
        if(authToken != null) {
            email = userData["email"]
        }

        // Ambil ID perangkat yang dikirim dari adapter
        deviceID = intent.getStringExtra("device_id")
        devicename = intent.getStringExtra("device_nama")
        textDeviceID.text = "Device ID: ${deviceID ?: "-"} | ${devicename ?: "-"}"
        inviteButton.setOnClickListener {
            val sharedEmail = emailInput.text.toString().trim()
            if (sharedEmail.isNotEmpty() && deviceID != null) {
                kirimUndanganKeServer(deviceID!!, sharedEmail)
            } else {
                Toast.makeText(this, "Masukkan email valid!", Toast.LENGTH_SHORT).show()
            }
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()?.setDisplayShowHomeEnabled(true);
        supportActionBar?.title = "Send invitation"
        toolbar.setNavigationOnClickListener(View.OnClickListener {
            //What to do on back clicked
            onBackPressed()
        })
    }

    private fun getUserData(): Map<String, String?> {
        val prefs = getSharedPreferences("my_prefs", MODE_PRIVATE)
        return mapOf(
            "token" to prefs.getString("auth_token", null),
            "username" to prefs.getString("username", null),
            "email" to prefs.getString("email", null),
            "avatar" to prefs.getString("avatar", null),
            "isVerified" to prefs.getString("isVerified", null)
        )
    }

    private fun kirimUndanganKeServer(deviceID: String, sharedEmail: String) {
        val ownerEmail = email // Ambil dari session/login
        if (ownerEmail != null) {
            devicename?.let {
                DeviceSharingService.sendInvite(ownerEmail, sharedEmail, deviceID,
                    it
                ) { success, message ->
                    runOnUiThread {
                        if (success) {
                            textStats.text = "Status: Sukses mengirim email ke @$sharedEmail"
                            Toast.makeText(this, "Undangan terkirim!", Toast.LENGTH_SHORT).show()
                            finish() // Kembali ke halaman sebelumnya
                        } else {
                            textStats.text = "Status: Gagal mengirim email"
                            Toast.makeText(this, "Gagal: $message", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}