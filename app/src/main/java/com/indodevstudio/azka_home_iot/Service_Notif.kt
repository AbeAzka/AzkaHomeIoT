package com.indodevstudio.azka_home_iot

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import info.mqtt.android.service.Ack
import info.mqtt.android.service.MqttAndroidClient

import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.concurrent.Executors

const val msg = "messageMQTT"

private val CHANNEL_ID = "channelId"
private val CHANNEL_NAME = "channelName"
private val CHANNEL_ID2 = "channelId2"
private val CHANNEL_NAME2 = "channelName2"
private val NOTIFICATION_ID = 1
var messageMQTT = ""
private val NOTIFICATION_ID_2 = 2
private lateinit var mqttAndroidClient: MqttAndroidClient
private var isDestroyed = false
class CounterNotificationService(
    private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showNotification(Messages : String) {
        val activityIntent = Intent(context, MainActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(
            context,
            1,
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.azkahomeiot)
            .setContentTitle("Pemberitahuan")
            .setContentText(Messages)
            .setContentIntent(activityPendingIntent)
            .setAutoCancel(true)
            .setGroup("Mbuh")
            .setGroupSummary(true)
            .build()

        notificationManager.notify(1, notification)
    }

    companion object {
        var messageMQTT = ""
        const val CHANNEL_ID = "channelId"
        const val CHANNEL_NAME = "channelName"
        const val NOTIFICATION_ID = 0
    }
}

    /*
    =====================================================================================
    ============MQTT MQTT MQTT BEGIN=================
    =====================================================================================
    =====================================================================================
    =====================================================================================
    =====================================================================================
     */

    fun connect(applicationContext : Context) {
        mqttAndroidClient = MqttAndroidClient ( applicationContext,"tcp://103.127.99.151:1883","19453",
            Ack.AUTO_ACK )
        mqttAndroidClient.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d("MQTT", "[SERVICE] Receive message: ${message.toString()} from topic: $topic ")
                messageMQTT = message.toString()
                //showNotification()
//                notif()
//                stopForeground(true)
            }

            override fun connectionLost(cause: Throwable?) {
                Log.d("MQTT", "[SERVICE] Connection lost ${cause.toString()}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                Log.d("MQTT", "[SERVICE] Complete ${token.toString()}")
            }
        })
        try {
            val token = mqttAndroidClient.connect()
            token.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken)                        {
                    Log.i("MQTT", "[SERVICE] success ")
                    subscribe("brankas1")
                    //connectionStatus = true
                    // Give your callback on connection established here
                }
                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    //connectionStatus = false
                    Log.i("MQTT", "[SERVICE] failure")
                    // Give your callback on connection failure here
                    exception.printStackTrace()
                }
            }
        } catch (e: MqttException) {
            // Give your callback on connection failure here
            e.printStackTrace()
        }
    }

    fun subscribe(topic: String, qos: Int = 1) {
        try {
            mqttAndroidClient.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("MQTT", "[SERVICE] Subscribed to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d("MQTT", "[SERVICE] Failed to subscribe $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        try {
            mqttAndroidClient.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("MQTT", "[SERVICE] Disconnected")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d("MQTT", "[SERVICE] Failed to disconnect")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun unsubscribe(topic: String) {
        try {
            mqttAndroidClient.unsubscribe(topic, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("MQTT", "[SERVICE] Unsubscribed to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d("MQTT", "[SERVICE] Failed to unsubscribe $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun publish(topic: String, msg: String, qos: Int = 1, retained: Boolean = false) {
        try {
            val message = MqttMessage()
            message.payload = msg.toByteArray()
            message.qos = qos
            message.isRetained = retained
            mqttAndroidClient.publish(topic, message, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("MQTT", "[SERVICE] $msg published to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d("MQTT", "[SERVICE] Failed to publish $msg to $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    /*
    =====================================================================================
    ============MQTT MQTT MQTT END=================
    =====================================================================================
    =====================================================================================
    =====================================================================================
    =====================================================================================
     */

//    override fun onDestroy() {
//        isDestroyed = true
//        Log.i("MQTT", "[SERVICE] SERVICE STOPPED!")
//        super.onDestroy()
//    }


