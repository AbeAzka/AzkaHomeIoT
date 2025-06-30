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
class MyFirebaseMessagingService : FirebaseMessagingService(){
//    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        remoteMessage.notification?.let {
//            val ringtoneUriString = PreferenceManager
//                .getDefaultSharedPreferences(this)
//                .getString("notification_ringtone", null)
//
//            val ringtoneUri = if (ringtoneUriString.isNullOrEmpty()) {
//                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//            } else {
//                Uri.parse(ringtoneUriString)
//            }
//            sendNotification(it.title, it.body, ringtoneUri)
//        }
//    }
        override fun onMessageReceived(remoteMessage: RemoteMessage) {
            val title = remoteMessage.notification?.title ?: "Notifikasi Baru"
            val body = remoteMessage.notification?.body ?: "Tekan untuk melihat detail event."

            val data = remoteMessage.data
            val actionType = data["action_type"] ?: ""
            val eventDate = data["event_date"]
            val eventList = data["event_list"]

            val ringtoneUriString = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString("notification_ringtone", null)

            val ringtoneUri = if (ringtoneUriString.isNullOrEmpty()) {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            } else {
                Uri.parse(ringtoneUriString)
            }

            sendNotification(title, body, ringtoneUri, actionType, eventDate, eventList)
        }


    private fun sendNotification(
            title: String,
            messageBody: String,
            ringtone: Uri,
            actionType: String,
            eventDate: String?,
            eventList: String?
        ) {
            val channelId = "event_reminder"
            val notificationId = System.currentTimeMillis().toInt()

            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("openFragment", "EventFragment")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_event)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setSound(ringtone)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            // Tambahkan aksi jika action_type adalah MARK_COMPLETE
            if (actionType == "MARK_COMPLETE" && eventDate != null && eventList != null) {
                val markIntent = Intent(this, NotificationActionReceiver::class.java).apply {
                    action = "MARK_COMPLETE"
                    putExtra("event_date", eventDate)
                    putExtra("event_list", eventList)
                }

                val markPendingIntent = PendingIntent.getBroadcast(
                    this, 1, markIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                builder.addAction(
                    R.drawable.ic_check_circle, // icon
                    "Mark As Finished", // label tombol
                    markPendingIntent
                )
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Event Reminder",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Channel for event reminder."
                }
                notificationManager.createNotificationChannel(channel)
            }

            notificationManager.notify(notificationId, builder.build())
        }

    }