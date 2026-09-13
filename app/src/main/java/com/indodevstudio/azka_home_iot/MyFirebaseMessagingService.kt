package com.indodevstudio.azka_home_iot

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        Log.d("FCM", "Payload diterima: $data")

        // ROUTING LOGIC: Cek apakah payload memiliki key "topic" (dari PHP IoT)
        if (data.containsKey("topic")) {
            showIotNotification(data)
        } else {
            // Jika tidak ada "topic", proses sebagai Event Reminder
            handleEventNotification(data)
        }
    }

    // =========================================================================
    // 1. FUNGSI UNTUK NOTIFIKASI IOT (SENSOR & TANDON)
    // =========================================================================
    private fun showIotNotification(data: Map<String, String>) {
        val topic = data["topic"] ?: "Update IoT"
        val message = data["message"] ?: "Ada pembaruan sensor."

        val iotChannelId = "iot_realtime"
        val notificationId = System.currentTimeMillis().toInt()

        // Notif IoT saat diklik akan membuka InboxActivity
        val contentIntent = Intent(this, InboxActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this, notificationId, contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, iotChannelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Ganti icon jika perlu
            .setContentTitle(topic)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                iotChannelId,
                "IoT Realtime Updates",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(notificationId, builder.build())
    }

    // =========================================================================
    // 2. FUNGSI UNTUK NOTIFIKASI EVENT (KODE ASLI KAMU)
    // =========================================================================
    private fun handleEventNotification(data: Map<String, String>) {
        val title = data["title"] ?: "New Event"
        val body = data["body"] ?: "You have a new event"
        val actionType = data["action_type"]
        val date = data["event_date"] ?: ""
        val rawList = data["event_list"]
        val eventList = rawList?.split(",")?.map { it.trim() } ?: emptyList()

        if (actionType == "MARK_COMPLETE") {
            if (isAppInForeground()) {
                handleInAppMarkComplete(date, eventList)
                return
            }
        }
        showEventNotification(title, body, data)
    }

    private fun showEventNotification(title: String, body: String, data: Map<String, String>) {
        val eventChannelId = "event_reminder"
        val notificationId = System.currentTimeMillis().toInt()
        val ringtoneUri = getNotificationSound()

        // Notif Event saat diklik membuka MainActivity (EventFragment)
        val contentIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("openFragment", "EventFragment")
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this, notificationId, contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, eventChannelId)
            .setSmallIcon(R.drawable.ic_event) // Pastikan drawable ini ada
            .setContentTitle(title)
            .setContentText(body)
            .setSound(ringtoneUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (data["action_type"]?.equals("MARK_COMPLETE") == true) {
            val markIntent = Intent(this, NotificationActionReceiver::class.java).apply {
                action = "MARK_COMPLETE"
                putExtra("event_date", data["event_date"])
                putExtra("event_list", data["event_list"])
            }
            val markPendingIntent = PendingIntent.getBroadcast(
                this, 1, markIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(R.drawable.ic_check_circle, "Tandai Selesai", markPendingIntent)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                eventChannelId,
                "Event Reminder",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(notificationId, builder.build())
    }

    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningProcesses = activityManager.runningAppProcesses ?: return false
        return runningProcesses.any {
            it.processName == packageName && it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
        }
    }

    private fun handleInAppMarkComplete(date: String, eventNames: List<String>) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("show_dialog", true)
            putExtra("event_date", date)
            putExtra("fragment_to_open", "event")
            putStringArrayListExtra("event_list", ArrayList(eventNames))
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    private fun getNotificationSound(): Uri {
        val ringtoneUriString = PreferenceManager
            .getDefaultSharedPreferences(this)
            .getString("notification_ringtone", null)
        return if (ringtoneUriString.isNullOrEmpty()) {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        } else {
            Uri.parse(ringtoneUriString)
        }
    }
}