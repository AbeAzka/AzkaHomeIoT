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
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
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
class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
//        val title = remoteMessage.notification?.title ?: "New Event"
//        val body = remoteMessage.notification?.body ?: "You have a new event"
        val title = data["title"] ?: "New Event"
        val body = data["body"] ?: "You have a new event"


        Log.d("FCM", "Notifikasi diterima: title=$title, body=$body, data=$data")

        val actionType = data["action_type"]
        val date = data["event_date"] ?: ""
        val rawList = data["event_list"]
        val eventList = rawList?.split(",")?.map { it.trim() } ?: emptyList()
        Log.d("FCM", "Data event_date: ${data["event_date"]}")
        Log.d("FCM", "Data event_list: ${data["event_list"]}")
        Log.d("FCM", "Data payload: $data")
        Log.d("FCM", "Action type: ${data["action_type"]}")
        if (actionType == "MARK_COMPLETE") {
            if (isAppInForeground()) {
                Log.d("FCM", "Foreground: memanggil handleInAppMarkComplete")
                handleInAppMarkComplete(date, eventList)
                return
            }
        }

        // Jika background atau bukan MARK_COMPLETE, tampilkan notif
        showNotification(title, body, data)
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
            putExtra("fragment_to_open", "event")  // contoh string
            putStringArrayListExtra("event_list", ArrayList(eventNames))
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        val channelId = "event_reminder"
        val notificationId = System.currentTimeMillis().toInt()
        val ringtoneUri = getNotificationSound()

        val contentIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("openFragment", "EventFragment")
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_event)
            .setContentTitle(title)
            .setContentText(body)
            .setSound(ringtoneUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
        Log.d("DEBUG", "DATA: $data")
        Log.d("DEBUG", "data[\"action_type\"] = ${data["action_type"]}")
        // Jika ada aksi MARK_COMPLETE, tambahkan tombol
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

            builder.addAction(
                R.drawable.ic_check_circle,
                "Tandai Selesai",
                markPendingIntent
            )
            Log.d("Notification", "Adding action button because action_type is MARK_COMPLETE")

        } else {
            Log.d("Notification", "No action button added, action_type is not MARK_COMPLETE")
        }



        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Event Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for event reminders"
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(notificationId, builder.build())
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