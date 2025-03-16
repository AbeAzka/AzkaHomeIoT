package com.indodevstudio.azka_home_iot

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.indodevstudio.azka_home_iot.API.EventService
import com.indodevstudio.azka_home_iot.API.RetrofitClient
import okhttp3.ResponseBody

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

const val channelId = "notification_channel"
const val channelName = "com.indodevstudio.azka_home_iot"
class MyFirebaseMessagingService : FirebaseMessagingService(){

    override fun onNewToken(token: String) {
        Log.d("FCM", "Token baru: $token")
        // Simpan token ke server
        val email = getEmailFromSharedPref(applicationContext) // Ambil email dari SharedPreferences
        if (email.isNotEmpty()) {
            saveFcmTokenToServer(applicationContext, email, token)
        }
    }

    private fun getEmailFromSharedPref(context: Context): String {
        val sharedPref = context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE)
        return sharedPref.getString("EMAIL", "") ?: ""
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (!isAppInForeground(this)) {
            remoteMessage.notification?.let {
                showNotification(it.title ?: "Event Hari Ini", it.body ?: "Ada agenda yang harus Anda hadiri.")
            }
        }
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "event_channel"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Event Notifications",
                NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notif_ig)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }

    private fun isAppInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningProcesses = activityManager.runningAppProcesses ?: return false
        return runningProcesses.any { it.processName == context.packageName && it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND }
    }


    fun saveFcmTokenToServer(context: Context, email: String, fcmToken: String) {
        val retrofit = RetrofitClient.instance
        val apiService = retrofit.create(EventService::class.java)

        apiService.sendFcmToken(email, fcmToken).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d("FCM", "Token FCM berhasil disimpan")
                } else {
                    Log.e("FCM", "Gagal menyimpan token")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("FCM", "Error: ${t.message}")
            }
        })
    }

    /*private fun sendTokenToServer(token: String) {
        val sharedPref = getSharedPreferences("USER_DATA", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("USER_ID", -1)
        if (userId == -1) return

        val retrofit = RetrofitClient.instance.create(EventService::class.java)
        retrofit.sendFcmToken(userId, token).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d("FCM", "Token berhasil dikirim ke server")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("FCM", "Gagal mengirim token ke server")
            }
        })
    }*/
}