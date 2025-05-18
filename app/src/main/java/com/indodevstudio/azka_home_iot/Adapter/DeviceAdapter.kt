package com.indodevstudio.azka_home_iot.Adapter

import android.content.Context
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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.indodevstudio.azka_home_iot.API.DeviceSharingService
import com.indodevstudio.azka_home_iot.DeviceControlActivity
import com.indodevstudio.azka_home_iot.InviteActivity
import com.indodevstudio.azka_home_iot.Logger
import com.indodevstudio.azka_home_iot.Model.DeviceModel
import com.indodevstudio.azka_home_iot.R
import okhttp3.internal.notify
import okhttp3.internal.notifyAll
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
    private val listener: DeviceActionListener,
    private val lifecycleOwner: LifecycleOwner // tambahkan ini

) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    interface DeviceActionListener {
        fun onRenameDevice(device: DeviceModel, position: Int)
        fun onDeleteDevice(device: DeviceModel, position: Int)
        fun onResetWiFi(device: DeviceModel)

        fun onPublish(device: String)

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
        holder.bind(device, listener, position, lifecycleOwner)

        val sharedPreferences = holder.itemView.context.getSharedPreferences("Bagogo", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Save device details only once
        editor.putString("device_id", device.id)
        editor.putString("device_ip", device.ipAddress)
        editor.apply()

        holder.itemView.setOnClickListener {
            val deviceIdd = device.id
            val deviceNam = device.name
            Toast.makeText(holder.itemView.context, "Device ID: $deviceIdd", Toast.LENGTH_SHORT).show()

            // Again, no need to save shared prefs twice, it's already done earlier
            DeviceSharingService.getDeviceStatus(deviceIdd)

            // Check if status is available
            if (!DeviceSharingService.deviceStatus.value.isNullOrEmpty()) {
                val status = DeviceSharingService.deviceStatus.value
                val status2 = DeviceSharingService.deviceStatus2.value
                Log.d("CheckStatus", "Status sekarang: $status")
                Log.d("CheckStatus", "Status sekarang: $status2")
            }

            val currentStatus = DeviceSharingService.deviceStatus.value
            val currentStatus2 = DeviceSharingService.deviceStatus2.value

            // Save status to shared preferences
            val shd = holder.itemView.context.getSharedPreferences("STAT", Context.MODE_PRIVATE)
            val editorStat = shd.edit()
            editorStat.putString("STATUS1_$deviceIdd", currentStatus)
            editorStat.putString("STATUS2_$deviceIdd", currentStatus2)
            editorStat.apply()

            // Start the DeviceControlActivity with device details
            val intent = Intent(holder.itemView.context, DeviceControlActivity::class.java)
            intent.putExtra("device_id", deviceIdd)
            intent.putExtra("deviceName", deviceNam)
            holder.itemView.context.startActivity(intent)
        }

        holder.itemView.setOnLongClickListener {
            if (device.isShared) {
                Toast.makeText(holder.itemView.context, "You don't have permission to modify this device", Toast.LENGTH_SHORT).show()
                return@setOnLongClickListener true
            }
            showInviteDialog(holder.itemView.context, device)
            true
        }
    }


    override fun getItemCount(): Int = deviceList.size

    private fun showInviteDialog(context: Context, device: DeviceModel) {
        if (device.isShared) {
            Toast.makeText(context, "You don't have permission to modify this device", Toast.LENGTH_SHORT).show()
            return
        }
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Undang Pengguna")
        builder.setMessage("Ingin mengundang pengguna lain untuk melihat perangkat ini?")

        builder.setPositiveButton("Undang") { _, _ ->
            // Pindah ke Fragment/Activity undangan
            val intent = Intent(context, InviteActivity::class.java) // Ganti dengan activity yang sesuai
            intent.putExtra("device_id", device.id) // Kirim ID perangkat ke activity
            intent.putExtra("device_nama", device.name) // Kirim ID perangkat ke activity
            context.startActivity(intent)
        }

        builder.setNegativeButton("Batal") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }


    fun updateData(newDevices: List<DeviceModel>) {
        deviceList.clear()
        deviceList.addAll(newDevices)
        notifyDataSetChanged()
    }

    /*fun connect(){
        for (holder in viewHolders){
            holder.setupMqttClient()
        }
    }*/

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

    fun publish_Button(topic: String, deviceId: String, command: String){
        for (holder in viewHolders){
            holder.publichBtn(topic, deviceId, command)
        }
    }



    class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvDeviceName: TextView = itemView.findViewById(R.id.textViewDeviceName)
        private val btnRename: ImageButton = itemView.findViewById(R.id.btnRename)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        private val btnRefresh: ImageButton = itemView.findViewById(R.id.btnRefresh)
        val deviceStatus: TextView = itemView.findViewById(R.id.textStatus)
        private val tvShared: TextView = itemView.findViewById(R.id.tvShared)
        val ip: TextView = itemView.findViewById(R.id.textIP)
        lateinit var mqttClient: MqttClient


/*        init{
            setupMqttClient()
            publishMessage("sending_order_${deviceId}", deviceId, "refresh")
        }*/

        fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: (T) -> Unit) {
            val wrapper = object : Observer<T> {
                override fun onChanged(t: T) {
                    observer(t)
                    removeObserver(this)
                }
            }
            observe(lifecycleOwner, wrapper)
        }

        fun bind(device: DeviceModel, listener: DeviceActionListener, position: Int, lifecycleOwner: LifecycleOwner) {
            // 🔄 Reset tampilan agar tidak mewarisi status lama
            deviceStatus.text = "Checking... Please press refresh button"
            deviceStatus.setTextColor(Color.DKGRAY)
            ip.text = device.ipAddress.ifEmpty { "0.0.0.0" }

//            DeviceSharingService.getStatus(device.id)
//
//            DeviceSharingService.status.observeOnce(lifecycleOwner) { status ->
//                if (!status.isNullOrEmpty()) {
//                    deviceStatus.text = status
//                    when (status.lowercase()) {
//                        "online" -> deviceStatus.setTextColor(Color.parseColor("#4CAF50")) // hijau
//                        "offline" -> deviceStatus.setTextColor(Color.parseColor("#F44336")) // merah
//                        else -> deviceStatus.setTextColor(Color.DKGRAY)
//                    }
//                }
//            }

            // Set nama dan ID
            tvDeviceName.text = "${device.name} - ID: ${device.id}"

            // Setup MQTT dan refresh status
            setupMqttClient(device)
            publishMessage("sending_order_${device.id}", device.id, "refresh")

            // Tombol
            btnRename.setOnClickListener { listener.onRenameDevice(device, position) }
            btnDelete.setOnClickListener {
                if (!device.isShared) {
                    listener.onDeleteDevice(device, position)
                }
            }
//            btnRefresh.setOnClickListener {
//                DeviceSharingService.getStatus(device.id)
////                Log.d("MQTT", "🔄 Manual Refresh untuk ${device.id}")
////                publishMessage("sending_order_${device.id}", device.id, "refresh")
//            }
//
//            // Check if status is available
//            if (!DeviceSharingService.status.value.isNullOrEmpty()) {
//                val status = DeviceSharingService.status.value
//                Log.d("CheckStatus", "Status2 sekarang: $status")
//                val currentStatus = DeviceSharingService.status.value
//                deviceStatus.text = currentStatus
//            }

    btnRefresh.setOnClickListener {
        DeviceSharingService.getStatus(device.id)

        Handler(Looper.getMainLooper()).postDelayed({
            val status = DeviceSharingService.status.value
            if (!status.isNullOrEmpty()) {
                Log.d("CheckStatus", "Status2 sekarang: $status")
                deviceStatus.text = status

                // Atur warna berdasarkan status
                when (status.lowercase()) {
                    "online" -> deviceStatus.setTextColor(Color.parseColor("#4CAF50")) // hijau
                    "offline" -> deviceStatus.setTextColor(Color.parseColor("#F44336")) // merah
                    else -> deviceStatus.setTextColor(Color.DKGRAY) // warna default
                }
            }
        }, 1000) // Delay 1 detik untuk memastikan status sempat diperbarui
    }




    // Shared status
            tvShared.visibility = if (device.isShared) View.VISIBLE else View.GONE
            btnDelete.visibility = if (device.isShared) View.GONE else View.VISIBLE
            btnRename.visibility = if (device.isShared) View.GONE else View.VISIBLE
        }


        fun setupMqttClient(device: DeviceModel) {
            val brokerUrl = "tcp://taryem.my.id:1883"
            val clientId = "client_${device.id}"
            val persistence = MemoryPersistence()
            val device_id = device.id
            try {
                mqttClient = MqttClient(brokerUrl, clientId, persistence)
                val options = MqttConnectOptions()
                options.isAutomaticReconnect = true
                options.isCleanSession = true
                options.connectionTimeout = 10
                options.keepAliveInterval = 120

                mqttClient.connect(options)

                Log.d("MQTT", "Connecting......")
                Logger.log("MQTT", "Connecting....")
                Log.d("MQTT", "✅ Connected client $clientId for device $device_id")
                Logger.log("MQTT", "✅ Connected client $clientId for device $device_id")
                mqttClient.setCallback(object : MqttCallback {
                    override fun connectionLost(cause: Throwable?) {}

                    override fun messageArrived(topic: String?, message: MqttMessage?) {
                        itemView.post {
                            val payload = message?.payload?.toString(Charsets.UTF_8) ?: ""
                            Log.d("MQTT", "📩 Pesan Diterima dari $topic: $payload")

                            try {
                                val json = JSONObject(payload)
                                val deviceIp = json.optString("ip", "")
                                val incomingDeviceId = json.optString("device_id", "")
                                Log.d("MQTT", "📩 $topic - Received: $payload - CurrentHolder: ${device_id}")

                                // ✅ Validasi: hanya update UI jika ID cocok
                                if (incomingDeviceId == device.id) {
                                    val sharedPreferences = itemView.context.getSharedPreferences("DevicePrefs", Context.MODE_PRIVATE)
                                    sharedPreferences.edit().putString("device_ip", deviceIp).apply()

                                    deviceStatus.text = "Online"
                                    deviceStatus.setTextColor(Color.GREEN)
                                    ip.text = deviceIp

                                    Log.d("MQTT", "✅ Status Online untuk device ID: ${device.id}")
                                } else {
                                    Log.d("MQTT", "❌ ID tidak cocok. Diabaikan. (expected: ${device.id}, got: $incomingDeviceId)")
                                }
                            } catch (e: Exception) {
                                println("❌ Gagal parsing JSON: ${e.message}")
                            }
                        }
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) {}
                })

                mqttClient.subscribe("${device_id}/switch1/status")
                mqttClient.subscribe("${device_id}/switch2/status")
                mqttClient.subscribe("sending_telemetri_${device_id}")


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
/*        fun publishMessage(topic: String, message: String) {
            if (::mqttClient.isInitialized && mqttClient.isConnected) {
                try {
                    val mqttMessage = MqttMessage()
                    mqttMessage.payload = message.toByteArray()
                    mqttClient.publish(topic, mqttMessage) // Kirim pesan ke topik MQTT
                } catch (e: MqttException) {
                    e.printStackTrace()
                }
            }
        }*/
        //WITH BUTON
        fun publichBtn(topic: String, deviceId: String, command: String) {
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
                    Toast.makeText(
                        itemView.context,
                        "Command $command sent",
                        Toast.LENGTH_SHORT
                    ).show()
                   // Log.d("MQTT", topic)
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


