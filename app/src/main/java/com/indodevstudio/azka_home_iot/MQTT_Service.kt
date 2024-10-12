package com.indodevstudio.azka_home_iot

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import info.mqtt.android.service.Ack
import info.mqtt.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage


class MQTT_Service : Service() {

    var messageMQTT = ""

    private lateinit var mqttAndroidClient: MqttAndroidClient

    override fun onBind(intent: Intent?): IBinder? {
        // Nah, not today. No binding here!
        return null
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
    }

    fun startForegroundService() {
        Log.i("MQTT", "Foreground is running!")
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Azka Home IoT")
            .setContentText("Running in the background")
            .setSmallIcon(R.drawable.azkahomeiot) // Your app's icon


            .build()

        startForeground(2, notification)
    }

    fun stopForegroundService(){
        stopForeground(true)
    }

    // Create a notification channel for Android O and above
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "MQTT Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            serviceChannel.setShowBadge(false)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)

        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("MQTT", "MQTT_SERVICE STARTED......")

        startForegroundService()
        this.connect(this)
        //performLongTask()
         // If the org.eclipse.paho.android.service is killed, it will be automatically restarted
        return START_STICKY
    }

    override fun stopService(intent: Intent?): Boolean {
        Log.i("MQTT", "MQTT_SERVICE STOPED......")
        //stopForegroundService()
        disconnect()
        return super.stopService(intent)
    }

    private fun performLongTask() {
        // Imagine doing something that takes a long time here
        Thread.sleep(5000)
    }



    override fun onDestroy() {
        super.onDestroy()
        Log.i("MQTT", "MQTT_SERVICE STOPED......")
        Log.i("MQTT", "Good bye")
    }

    fun log(str:String){
        Log.d("TAG", "log: $str")
    }

    companion object{

        private val CHANNEL_ID = "channelId"
        private val CHANNEL_NAME = "channelName"
        private val NOTIFICATION_ID = 0
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
        mqttAndroidClient = MqttAndroidClient ( applicationContext,"tcp://103.127.99.151:1883","19453", Ack.AUTO_ACK)

        mqttAndroidClient.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d("MQTT", "[SERVICE] Receive message: ${message.toString()} from topic: $topic ")
                messageMQTT = message.toString()
                val service = CounterNotificationService(applicationContext)
                service.showNotification(message.toString())
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
                    unsubscribe("brankas1")
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
                    unsubscribe("brankas1")
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
}