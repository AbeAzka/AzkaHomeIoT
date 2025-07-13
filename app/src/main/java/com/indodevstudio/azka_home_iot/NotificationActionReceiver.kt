package com.indodevstudio.azka_home_iot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.indodevstudio.azka_home_iot.API.RetrofitClient
import com.indodevstudio.azka_home_iot.API.EventService
import androidx.core.app.NotificationManagerCompat

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "MARK_COMPLETE") {
            val date = intent.getStringExtra("event_date") ?: ""
            val eventList = intent.getStringExtra("event_list") ?: ""

            val events = eventList.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            if (events.isEmpty()) {
                Log.d("NotifReceiver", "Tidak ada event yang ditandai selesai.")
                return
            }

            // Kirim ke API (gunakan coroutine supaya background thread)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val api = RetrofitClient.instance.create(EventService::class.java)
                    val response = api.markEventsAsDone(date, events).execute()

                    if (response.isSuccessful) {
                        Log.d("NotifReceiver", "Berhasil tandai selesai: $events")
                    } else {
                        Log.e("NotifReceiver", "Gagal tandai selesai: ${response.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                    Log.e("NotifReceiver", "Error saat mark as finished", e)
                }
            }

            // Hapus notifikasi
            NotificationManagerCompat.from(context).cancelAll()
        }
    }
}

