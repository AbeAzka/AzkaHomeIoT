package com.indodevstudio.azka_home_iot

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.indodevstudio.azka_home_iot.API.DeviceSharingService
import org.json.JSONObject
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class LoginSuccessActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data: Uri? = intent.data
        val token = data?.getQueryParameter("token")
        val username = data?.getQueryParameter("username")
        val rawEmail = data?.getQueryParameter("email")
        val avatar = data?.getQueryParameter("avatar")
        val email = rawEmail?.let { decodeUrlEncodedString(it) }
        if (!token.isNullOrEmpty() && !username.isNullOrEmpty()) {
            saveUserData(token, username, email, avatar)
            email?.let { decodedEmail ->
                // Gunakan email yang sudah di-decode
                DeviceSharingService.addUser(decodedEmail, username)
            }
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            intent.putExtra("isFirebase", false)
        }

        finish() // Tutup activity redirect
    }

    private fun decodeUrlEncodedString(encoded: String): String {
        return try {
            URLDecoder.decode(encoded, StandardCharsets.UTF_8.name())
        } catch (e: Exception) {
            encoded // Jika gagal decode, kembalikan string aslinya
        }
    }

    private fun saveUserData(token: String, username: String, email: String?, avatar: String?) {
        val prefs = getSharedPreferences("my_prefs", MODE_PRIVATE)
        prefs.edit()
            .putString("auth_token", token)
            .putString("username", username)
            .putString("email", email)
            .putString("avatar", avatar)
            .putString("isFirebase", "false")
            .putString("isVerified", "true")
            .apply()

//        val account = email?.let {
//            AccountData(
//                email = it,
//                token = token,
//                provider = AuthProvider.CUSTOM, // atau AuthProvider.FIREBASE
//                avatarUrl = avatar,
//                username = username,
//                isVerified = true
//            )
//        }
//
//        if (account != null) {
//            AccountManager.saveAccount(this, account)
//        }
//        if (email != null) {
//            AccountManager.setCurrentAccount(this, email)
//        }


    }
}


