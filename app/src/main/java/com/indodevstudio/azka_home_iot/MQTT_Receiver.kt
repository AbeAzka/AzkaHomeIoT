package com.indodevstudio.azka_home_iot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MQTT_Receiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context!!.startService(Intent(context, MQTT_Service::class.java))
    }
}