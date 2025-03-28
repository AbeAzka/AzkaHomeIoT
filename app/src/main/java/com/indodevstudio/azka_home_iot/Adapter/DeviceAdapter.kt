package com.indodevstudio.azka_home_iot.Adapter

import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.indodevstudio.azka_home_iot.DeviceControlActivity
import com.indodevstudio.azka_home_iot.Logger
import com.indodevstudio.azka_home_iot.Model.DeviceModel
import com.indodevstudio.azka_home_iot.R
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONObject
import java.io.IOException

class DeviceAdapter(
    private val deviceList: MutableList<DeviceModel>,
    private val listener: DeviceActionListener
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    interface DeviceActionListener {
        fun onRenameDevice(device: DeviceModel, position: Int)
        fun onDeleteDevice(device: DeviceModel, position: Int)
        fun onResetWiFi(device: DeviceModel)

        fun onPublish(deviceId: String)

        /*fun onTopic(device: DeviceModel)*/
    }

    private val viewHolders = mutableListOf<DeviceViewHolder>() // Menyimpan semua ViewHolder

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
        val holder = DeviceViewHolder(view)
        viewHolders.add(holder) // Simpan ViewHolder agar bisa di-disconnect nanti
        return holder
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = deviceList[position]
        holder.bind(device, listener, position)

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DeviceControlActivity::class.java)
            intent.putExtra("deviceName", device.name)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = deviceList.size

    fun updateData(newDevices: List<DeviceModel>) {
        deviceList.clear()
        deviceList.addAll(newDevices)
        notifyDataSetChanged()
    }

    fun disconnectAllMqttClients() {
        // Panggil fungsi disconnect untuk semua ViewHolder yang tersimpan
        for (holder in viewHolders) {
            holder.disconnectMqtt()
        }
    }

    fun publish(topic: String, deviceId: String, command: String){
        for (holder in viewHolders){
            holder.publishMessage(topic, deviceId, command)
        }
    }



    class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDeviceName: TextView = itemView.findViewById(R.id.textViewDeviceName)
        private val btnRename: ImageButton = itemView.findViewById(R.id.btnRename)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        private val btnRefresh: ImageButton = itemView.findViewById(R.id.btnRefresh)
        val deviceStatus: TextView = itemView.findViewById(R.id.textStatus)
        val ip: TextView = itemView.findViewById(R.id.textIP)
        lateinit var mqttClient: MqttClient

        fun bind(device: DeviceModel, listener: DeviceActionListener, position: Int) {
            tvDeviceName.text = device.name

            btnRename.setOnClickListener { listener.onRenameDevice(device, position) }
            btnDelete.setOnClickListener { listener.onDeleteDevice(device, position) }
            btnRefresh.setOnClickListener{listener.onPublish("arduino_1")}
            setupMqttClient()
            listener.onPublish("arduino_1")

            //publishMqttTopic("set_mqtt_topic", topic1, topic2)
        }

        private fun setupMqttClient() {
            val brokerUrl = "tcp://taryem.my.id:1883"
            val clientId = "kotlin123"
            val persistence = MemoryPersistence()

            try {
                mqttClient = MqttClient(brokerUrl, clientId, persistence)
                val options = MqttConnectOptions()
                options.isAutomaticReconnect = true
                options.isCleanSession = true
                options.connectionTimeout = 10
                options.keepAliveInterval = 10

                mqttClient.connect(options)

                Log.d("MQTT", "Konek")
                Logger.log("MQTT", "KONEK")
                mqttClient.setCallback(object : MqttCallback {
                    override fun connectionLost(cause: Throwable?) {}

                    override fun messageArrived(topic: String?, message: MqttMessage?) {
                        itemView.post {
                            deviceStatus.text = "Online"
                            deviceStatus.setTextColor(Color.GREEN)

                            val payload = message?.payload?.toString(Charsets.UTF_8) ?: ""
                            println("📩 Pesan Diterima dari $topic: $payload")

                            // Cek apakah pesan adalah IP Address
                            if (isValidIPAddress(payload)) {
                                println("✅ IP Address Terbaca: $payload")
                                ip.text = payload
                            } else {
                                println("⚠️ Bukan IP Address: $payload")
                                ip.text = "0.0.0.0"
                            }
                        }
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) {}
                })


                mqttClient.subscribe("sending_telemetri2")


            } catch (e: MqttException) {
                e.printStackTrace()
            }
        }

        fun isValidIPAddress(ip: String): Boolean {
            val ipRegex = Regex(
                "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
            )
            return ip.matches(ipRegex)
        }


        //NORMAL PUBLISH
        fun publishMessage(topic: String, message: String) {
            if (::mqttClient.isInitialized && mqttClient.isConnected) {
                try {
                    val mqttMessage = MqttMessage()
                    mqttMessage.payload = message.toByteArray()
                    mqttClient.publish(topic, mqttMessage) // Kirim pesan ke topik MQTT
                } catch (e: MqttException) {
                    e.printStackTrace()
                }
            }
        }
        //WITH DEVICEID PUBLISH
        fun publishMessage(topic: String, deviceId: String, command: String) {
            try {
                if (::mqttClient.isInitialized && mqttClient.isConnected) {
                    // Buat JSON payload
                    val jsonPayload = JSONObject().apply {
                        put("device_id", deviceId)
                        put("command", command)
                    }

                    // Konversi ke String
                    val message = jsonPayload.toString()

                    // Buat MQTT Message
                    val mqttMessage = MqttMessage().apply {
                        payload = message.toByteArray()
                        qos = 1  // QoS 1 agar pesan pasti sampai
                        isRetained = false
                    }

                    // Publish ke MQTT
                    mqttClient.publish(topic, mqttMessage)
                    println("✅ Pesan dikirim: $message ke topik $topic")

                } else {
                    println("❌ MQTT Client tidak terhubung. Tidak bisa mengirim pesan.")
                    Logger.log("MQTT", "❌ MQTT Client tidak terhubung. Tidak bisa mengirim pesan.")
                }
            } catch (e: MqttException) {
                e.printStackTrace()
                println("❌ Gagal mengirim pesan MQTT: ${e.message}")
                Logger.log("MQTT", "❌ Gagal mengirim pesan MQTT: ${e.message}")
            } catch (e: Exception) {
                e.printStackTrace()
                println("❌ Error lainnya: ${e.message}")
                Logger.log("MQTT", "❌ Error lainnya: ${e.message}")
            }
        }

        fun publishMqttTopic(mqttTopic: String, newTopic: String, newTopic2: String) {
            try {
                // Membuat payload dalam format JSON
                val jsonPayload = JSONObject().apply {
                    put("topic", newTopic)
                    put("topic2", newTopic2)
                }

                // Konversi JSON ke string
                val message = jsonPayload.toString()
                val mqttMessage = MqttMessage().apply {
                    payload = message.toByteArray()
                    qos = 1  // QoS 1 agar pesan pasti sampai
                    isRetained = false
                }


                // Publish pesan ke MQTT broker
                mqttClient.publish(mqttTopic, mqttMessage)
                println("📡 MQTT Topic Sent: $message")

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }



        fun disconnectMqtt() {
            if (::mqttClient.isInitialized && mqttClient.isConnected) {
                try {
                    mqttClient.disconnect()
                    mqttClient.close()
                    Log.d("MQTT", "Disconnect")
                    Logger.log("MQTT", "Disconnect")
                } catch (e: MqttException) {
                    e.printStackTrace()
                }
            }
        }
    }
}


