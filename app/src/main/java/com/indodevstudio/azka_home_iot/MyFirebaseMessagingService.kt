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
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {

            sendNotification(it.title, it.body)
        }
    }

    private fun sendNotification(title: String?, messageBody: String?) {
        val channelId = "event_reminder"
        val notificationId = System.currentTimeMillis().toInt()

        // Intent untuk membuka MainActivity dengan Fragment Event
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("openFragment", "EventFragment")
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_event) // Ganti dengan icon yang sesuai
            .setContentTitle(title ?: "Notifikasi Baru")
            .setContentText(messageBody ?: "Tekan untuk melihat detail event.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Buat channel jika Android >= Oreo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Reminder Acara",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel untuk mengingatkan event"
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}