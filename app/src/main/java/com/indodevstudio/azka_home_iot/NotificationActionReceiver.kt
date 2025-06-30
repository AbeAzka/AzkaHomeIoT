package com.indodevstudio.azka_home_iot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "MARK_COMPLETE") {
            val eventDate = intent.getStringExtra("event_date")
            val eventList = intent.getStringExtra("event_list")

            // TODO: Ganti email sesuai user login
//            val email = getEmailLoggedIn(context)

            // Kirim ke server untuk tandai selesai
//            RetrofitClient.instance.create(EventService::class.java)
//                .markEventAsCompleted(email, eventDate ?: "")
//                .enqueue(object : Callback<ResponseBody> {
//                    override fun onResponse(
//                        call: Call<ResponseBody>,
//                        response: Response<ResponseBody>
//                    ) {
//                        Toast.makeText(context, "Agenda ditandai selesai", Toast.LENGTH_SHORT).show()
//                    }
//
//                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                        Toast.makeText(context, "Gagal menandai selesai", Toast.LENGTH_SHORT).show()
//                    }
//                })
//        }
    }

    fun getEmailLoggedIn(context: Context) {
//        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
//        return prefs.getString("loggedInEmail", "") ?: ""
    }
}
    }
