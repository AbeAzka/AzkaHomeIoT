package com.indodevstudio.azka_home_iot

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.indodevstudio.azka_home_iot.API.APIRequestData
import com.indodevstudio.azka_home_iot.API.RetroServer

class DataCheckWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        return try {
            // Panggil API secara synchronous karena Worker sudah berada di background thread
            val api = RetroServer.konekRetrofit().create(APIRequestData::class.java)
            val response = api.ardRetrieveData2("").execute()

            if (response.isSuccessful && response.body() != null) {
                val dataList = response.body()!!.data

                if (dataList != null && dataList.isNotEmpty()) {
                    // Ambil data paling baru (asumsi data terbaru ada di index terakhir atau pertama)
                    // Sesuaikan index ini bergantung pada urutan respon API kamu (ASC/DESC)
                    val latestData = dataList[dataList.size - 1]
                    val latestId = latestData.no

                    // Cek ID terakhir yang disimpan di memori HP
                    val sharedPref = applicationContext.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                    val lastSavedId = sharedPref.getInt("last_data_id", -1)

                    if (latestId > lastSavedId) {
                        // Data baru ditemukan, tampilkan notifikasi
                        showNotification(latestData.topic, latestData.message)

                        // Simpan ID terbaru agar tidak notif berulang
                        sharedPref.edit().putInt("last_data_id", latestId).apply()
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry() // Coba lagi nanti jika gagal (misal tidak ada sinyal)
        }
    }

    private fun showNotification(topic: String?, message: String?) {
        val channelId = "iot_notifications"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "IoT Updates", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Sesuaikan dengan icon app kamu
            .setContentTitle("Data Baru: $topic")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}