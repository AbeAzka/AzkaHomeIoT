package com.indodevstudio.azka_home_iot.API

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.indodevstudio.azka_home_iot.Logger
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit

import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

object DeviceSharingService {
    private val client = OkHttpClient()
    val deviceStatus = MutableLiveData<String?>()
    val deviceStatus2 = MutableLiveData<String?>()
    val status = MutableLiveData<String?>()

    private var pollingHandler: Handler? = null
    private var pollingRunnable: Runnable? = null

    fun startPollingDeviceStatus(deviceId: String, intervalMillis: Long = 3000L) {
        pollingHandler = Handler(Looper.getMainLooper())
        pollingRunnable = object : Runnable {
            override fun run() {
                getDeviceStatus(deviceId)
                pollingHandler?.postDelayed(this, intervalMillis)
            }
        }
        pollingHandler?.post(pollingRunnable!!)
    }

    fun stopPolling() {
        pollingHandler?.removeCallbacks(pollingRunnable!!)
    }
    fun getDeviceStatus(deviceId: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://ahi.abeazka.my.id/api/arduino/") // Ganti dengan URL server kamu
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ArduinoService::class.java)

        apiService.getDeviceStatus(deviceId).enqueue(object : retrofit2.Callback<DeviceStatusResponse> {
            override fun onResponse(
                call: retrofit2.Call<DeviceStatusResponse>,
                response: retrofit2.Response<DeviceStatusResponse>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    deviceStatus.value = response.body()?.status_s1
                    deviceStatus2.value = response.body()?.status_s2
                } else {
                    deviceStatus.value = "Unknown"
                    deviceStatus2.value = "Unknown"
                }
            }

            override fun onFailure(call: retrofit2.Call<DeviceStatusResponse>, t: Throwable) {
                deviceStatus.value = "Error"
                deviceStatus2.value = "Error"
            }
        })
    }



    fun getStatus(deviceId: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://ahi.abeazka.my.id/api/arduino/") // Ganti dengan URL server kamu
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ArduinoService::class.java)

        apiService.getStatus(deviceId).enqueue(object : retrofit2.Callback<StatusResponse> {
            override fun onResponse(
                call: retrofit2.Call<StatusResponse>,
                response: retrofit2.Response<StatusResponse>
            ) {
                if (response.isSuccessful) {
                    val statuss = response.body()?.status
                    status.value = response.body()?.status

                    Log.d("tesr","$deviceId = Status device: $statuss")
                    // kamu bisa pakai status ini untuk update UI atau logika lainnya
                } else {
                    status.value = "Unknown"

                    println("Gagal mendapatkan response. Kode: ${response.code()}")
                }
            }

            override fun onFailure(call: retrofit2.Call<StatusResponse>, t: Throwable) {
                status.value = "Error"
            }
        })
    }


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
//        val requestBody = FormBody.Builder()
//            .add("owner_email", ownerEmail)
//            .add("device_name", deviceName)
//            .add("device_id", deviceID)
//            .add("device_ip", deviceIP)
//            .build()
        val json = """
        {
            "owner_email": "$ownerEmail",
            "device_name": "$deviceName",
            "device_id": "$deviceID",
            "device_ip": "$deviceIP"
        }
    """.trimIndent()

        // Buat RequestBody dengan tipe JSON
        val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())


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