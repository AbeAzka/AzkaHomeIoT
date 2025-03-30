package com.indodevstudio.azka_home_iot.API

import com.indodevstudio.azka_home_iot.Logger
import okhttp3.*
import java.io.IOException

object DeviceSharingService {
    private val client = OkHttpClient()

    // Fungsi untuk mengirim undangan berbagi perangkat
    fun sendInvite(ownerEmail: String, sharedEmail: String, deviceID: String, deviceName: String, callback: (Boolean, String) -> Unit) {
        val requestBody = FormBody.Builder()
            .add("owner_email", ownerEmail)
            .add("shared_email", sharedEmail)
            .add("device_id", deviceID)
            .add("device_name", deviceName)
            .build()

        val request = Request.Builder()
            .url("http://ahi.abeazka.my.id/api/arduino/invite")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Gagal mengirim undangan: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                callback(true, "Undangan terkirim: ${response.body?.string()}")
            }
        })
    }

    fun sendDevice(ownerEmail: String, deviceName: String, deviceID: String, deviceIP: String) {
        val requestBody = FormBody.Builder()
            .add("owner_email", ownerEmail)
            .add("device_name", deviceName)
            .add("device_id", deviceID)
            .add("device_ip", deviceIP)
            .build()

        val request = Request.Builder()
            .url("http://ahi.abeazka.my.id/api/arduino/add_device")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                //callback(false, "Gagal mengirim undangan: ${e.message}")
                Logger.log("INFO", "Gagal add device: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                //callback(true, "Undangan terkirim: ${response.body?.string()}")
            }
        })
    }

    fun addUser(email: String, name: String) {
        val requestBody = FormBody.Builder()
            .add("user_email", email)
            .add("user_name", name)
            .build()

        val request = Request.Builder()
            .url("http://ahi.abeazka.my.id/api/arduino/add_user")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                //callback(false, "Gagal mengirim undangan: ${e.message}")
                Logger.log("INFO", "Gagal add user: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                //callback(true, "Undangan terkirim: ${response.body?.string()}")
            }
        })
    }




    // Fungsi untuk menerima undangan berbagi perangkat
    fun acceptInvite(userEmail: String, deviceID: String, callback: (Boolean, String) -> Unit) {
        val requestBody = FormBody.Builder()
            .add("user_email", userEmail)
            .add("device_id", deviceID)
            .build()

        val request = Request.Builder()
            .url("http://ahi.abeazka.my.id/api/arduino/accept_invite")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Gagal menerima undangan: ${e.message}")
                Logger.log("INFO", "Gagal terima undangan: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                callback(true, "Undangan diterima: ${response.body?.string()}")
            }
        })
    }

    // Fungsi untuk mendapatkan daftar perangkat yang dibagikan
    fun getSharedDevices(userEmail: String, callback: (Boolean, String) -> Unit) {
        val request = Request.Builder()
            .url("http://ahi.abeazka.my.id/api/arduino/shared_devices?user_email=$userEmail")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Gagal mendapatkan perangkat: ${e.message}")
                Logger.log("INFO", "Gagal dapat device: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val devices = response.body?.string()
                callback(true, "Perangkat yang dibagikan: $devices")
                Logger.log("INFO", "Sukses dapat device: $devices")
            }
        })
    }
}