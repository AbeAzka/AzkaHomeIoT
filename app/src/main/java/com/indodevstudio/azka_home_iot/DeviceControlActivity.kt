package com.indodevstudio.azka_home_iot

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.indodevstudio.azka_home_iot.API.DeviceSharingService
import com.indodevstudio.azka_home_iot.Adapter.DeviceAdapter
import com.indodevstudio.azka_home_iot.Model.DeviceModel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.io.IOException


class DeviceControlActivity : AppCompatActivity() {

    private lateinit var textViewStatus: TextView
/*    private lateinit var buttonOn: Button
    private lateinit var buttonOff: Button
    private lateinit var buttonOn2: Button
    private lateinit var buttonOff2: Button*/
    private val deviceList = mutableListOf<DeviceModel>()
    private lateinit var textSwitch1: TextView
    private lateinit var textSwitch2: TextView

    private lateinit var switch1Layout: LinearLayout
    private lateinit var switch2Layout: LinearLayout
    private lateinit var indicator1: View
    private lateinit var indicator2: View
    lateinit var deviceAdapter : DeviceAdapter
    var isSwitch1On = false
    var isSwitch2On = false


    private val client = OkHttpClient()
    private lateinit var baseUrl: String

    lateinit var mqttClient: MqttClient

    var device_id = ""

    var sharedPreferences: SharedPreferences? = null

    private lateinit var category: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_control)
        setupMqttClient() //START MQTTTT

        textViewStatus = findViewById(R.id.textViewStatus)
        val sharedPrefs = this.getSharedPreferences("device_category", AppCompatActivity.MODE_PRIVATE)

        category = intent.getStringExtra("category") ?: "Unknown"
        when (category) {
            "Lamp" -> showLampUI()
            "Sensor" -> showSensorUI()
            "Custom" -> showCustomUI()
            else -> showDefaultUI()
        }


        switch1Layout = findViewById(R.id.switch1Layout)
        switch2Layout = findViewById(R.id.switch2Layout)

        indicator1 = findViewById(R.id.indicator1)
        indicator2 = findViewById(R.id.indicator2)

        textSwitch2 = findViewById(R.id.textSwitch2)
        textSwitch1 = findViewById(R.id.textSwitch1)

        val sharedPreferences2 = getSharedPreferences("Bagogo", Context.MODE_PRIVATE)
        device_id = sharedPreferences2?.getString("device_id", null).toString()
        sharedPreferences = getSharedPreferences("MyPrefs_$device_id", 0)

        // 🔹 Set teks yang tersimpan
        textSwitch1.text = sharedPreferences?.getString("switch1", "Switch 1")
        textSwitch2.text = sharedPreferences?.getString("switch2", "Switch 2")
        textSwitch1.setOnClickListener { v: View? ->
            showEditDialog(
                textSwitch1, "switch1"
            )
        }
        textSwitch2.setOnClickListener { v: View? ->
            showEditDialog(
                textSwitch2, "switch2"
            )
        }

        val deviceName = intent.getStringExtra("deviceName") ?: "Unknown Device"
        baseUrl = "http://taryem.my.id/Lab01/ahi.php"
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()?.setDisplayShowHomeEnabled(true);
        supportActionBar?.title = "Control $deviceName"


        switch1Layout.setOnClickListener {
            isSwitch1On = !isSwitch1On
            val command = if (isSwitch1On) "ON" else "OFF"
            publishCommand("switch1", command)
            //sendCommand(command, device_id, "switch1")
            updateSwitchUI(switch1Layout, indicator1, isSwitch1On)
        }


        switch2Layout.setOnClickListener {
            isSwitch2On = !isSwitch2On
            val command = if (isSwitch2On) "ON" else "OFF"
            publishCommand("switch2", command)
            //sendCommand(command, device_id, "switch2")
            updateSwitchUI(switch2Layout, indicator2, isSwitch2On)

        }

        toolbar.setNavigationOnClickListener(View.OnClickListener {
            //What to do on back clicked
            onBackPressed()
        })

        val shar = getSharedPreferences("STAT", Context.MODE_PRIVATE)

        DeviceSharingService.getDeviceStatus(device_id)
        if (!DeviceSharingService.deviceStatus.value.isNullOrEmpty()) {
            val status = DeviceSharingService.deviceStatus.value
            val status2 = DeviceSharingService.deviceStatus2.value
            Log.d("CheckStatus", "Status sekarang: $status")
            Log.d("CheckStatus", "Status sekarang: $status2")
        }

        val currentStatus =  DeviceSharingService.deviceStatus.value//shar.getString("STATUS1_$device_id", null).toString()
//DeviceSharingService.deviceStatus.value
        val currentStatus2 = DeviceSharingService.deviceStatus2.value//shar.getString("STATUS2_$device_id", null).toString()
        //DeviceSharingService.deviceStatus2.value

        DeviceSharingService.deviceStatus.observe(this) { status ->
            isSwitch1On = status.equals("ON", true)
            updateSwitchUI(switch1Layout, indicator1, isSwitch1On)
        }

        DeviceSharingService.deviceStatus2.observe(this) { status2 ->
            isSwitch2On = status2.equals("ON", true)
            updateSwitchUI(switch2Layout, indicator2, isSwitch2On)
        }



    }

    override fun onResume() {
        super.onResume()
        DeviceSharingService.startPollingDeviceStatus(device_id)

    }

    override fun onPause() {
        super.onPause()
        DeviceSharingService.stopPolling()

    }

    private fun showLampUI() {
        Toast.makeText(this, "Menampilkan UI Lampu", Toast.LENGTH_SHORT).show()
        val customBuilderLayout = findViewById<LinearLayout>(R.id.customBuilderLayout)
        customBuilderLayout.visibility = View.GONE
        val switchScrollView = findViewById<ScrollView>(R.id.switchScrollView)
        switchScrollView.visibility = View.VISIBLE
    }

    private fun showSensorUI() {
        Toast.makeText(this, "Menampilkan UI Sensor", Toast.LENGTH_SHORT).show()
        val customBuilderLayout = findViewById<LinearLayout>(R.id.customBuilderLayout)
        customBuilderLayout.visibility = View.GONE
        val switchScrollView = findViewById<ScrollView>(R.id.switchScrollView)
        switchScrollView.visibility = View.VISIBLE
    }


    private fun showCustomUI() {
        val switchScrollView = findViewById<ScrollView>(R.id.switchScrollView)
        switchScrollView.visibility = View.GONE
        Toast.makeText(this, "Menampilkan UI Custom", Toast.LENGTH_SHORT).show()
        val customBuilderLayout = findViewById<LinearLayout>(R.id.customBuilderLayout)
        customBuilderLayout.visibility = View.VISIBLE
        setupPalette()
        setupDropArea()
    }

    private fun showDefaultUI() {
        Toast.makeText(this, "Kategori tidak dikenali", Toast.LENGTH_SHORT).show()
    }

    private fun setupPalette() {
        val komponen = listOf("Switch", "Slider", "Button")
        val palette = findViewById<LinearLayout>(R.id.componentPanel) // ID diperbaiki

        komponen.forEach { nama ->
            val btn = Button(this) // Gunakan requireContext() jika dalam Fragment
            btn.text = nama
            btn.setPadding(16, 8, 16, 8)
            btn.setOnLongClickListener {
                val data = ClipData.newPlainText("komponen", nama)
                val shadow = View.DragShadowBuilder(it)
                it.startDragAndDrop(data, shadow, null, 0)
                true
            }
            palette.addView(btn)
        }
    }

    private fun setupDropArea() {
        val dropArea = findViewById<FrameLayout>(R.id.customArea) // ID diperbaiki
        dropArea.setOnDragListener { v, event ->
            when (event.action) {
                DragEvent.ACTION_DROP -> {
                    if (event.clipData.itemCount > 0) {
                        val jenis = event.clipData.getItemAt(0).text.toString()
                        val newView = when (jenis) {
                            "Switch" -> {
                                val inflater = LayoutInflater.from(this)
                                val switchView = inflater.inflate(R.layout.item_switcth, null)
                                val params = LinearLayout.LayoutParams(
                                    TypedValue.applyDimension(
                                        TypedValue.COMPLEX_UNIT_DIP,
                                        200f, // ganti ke ukuran lebar yang kamu mau
                                        resources.displayMetrics
                                    ).toInt(),
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                                switchView.layoutParams = params
                                val layout = switchView.findViewById<LinearLayout>(R.id.switchLayout)
                                val indicator = switchView.findViewById<View>(R.id.indicator)
                                val text = switchView.findViewById<TextView>(R.id.textSwitch)

                                // Toggle logika ON/OFF saat diklik
                                var isOn = false
                                layout.setOnClickListener {
                                    isOn = !isOn
                                    if (isOn) {
                                        layout.setBackgroundResource(R.drawable.bg_switch_on)
                                        indicator.setBackgroundResource(R.drawable.indicator_on)
                                        publishCommand("custom_switch", "ON")
                                    } else {
                                        layout.setBackgroundResource(R.drawable.bg_switch_off)
                                        indicator.setBackgroundResource(R.drawable.indicator_off)
                                        publishCommand("custom_switch", "OFF")
                                    }
                                }
                                switchView
                            }
                            "Slider" -> SeekBar(this).apply {
                                max = 100
                                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                                        publishCommand("custom_slider", progress.toString())
                                    }

                                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                                })
                            }
                            "Button" -> Button(this).apply {
                                text = "Custom Button"
                                setOnClickListener {
                                    publishCommand("custom_button", "PRESSED")
                                    Toast.makeText(this@DeviceControlActivity, "Button pressed", Toast.LENGTH_SHORT).show()
                                }
                            }
                            else -> null
                        }

                        newView?.let {
                            val params = FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.WRAP_CONTENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT
                            )
                            params.leftMargin = event.x.toInt()
                            params.topMargin = event.y.toInt()
                            (v as FrameLayout).addView(it, params)
                            makeViewDraggable(it)
                        }
                    }
                }
            }
            true
        }
    }

    private fun makeViewDraggable(view: View) {
        var dX = 0f
        var dY = 0f
        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = v.x - event.rawX
                    dY = v.y - event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    v.animate()
                        .x(event.rawX + dX)
                        .y(event.rawY + dY)
                        .setDuration(0)
                        .start()
                }
            }
            true
        }
    }





    private fun showEditDialog(textView: TextView, key: String) {
        val textInputLayout = TextInputLayout(this).apply {
            setPadding(50, 40, 50, 0)
            hint = "Masukkan nama"
        }

        val editText = TextInputEditText(textInputLayout.context).apply {
            setText(textView.text.toString())
            setTextSize(16f)
        }

        textInputLayout.addView(editText)

        MaterialAlertDialogBuilder(this)
            .setTitle("Edit Nama")
            .setView(textInputLayout)
            .setPositiveButton("Simpan") { _, _ ->
                val newText = editText.text.toString()
                textView.text = newText
                sharedPreferences?.edit()?.putString(key, newText)?.apply()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }



    override fun onBackPressed() {
        super.onBackPressed()
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fragment_container, manual_book_fragment()).commit()
        onBackPressedDispatcher.onBackPressed()
    }

    fun publishCommand(switch: String, command: String) {
        try {
            val topic = "$device_id/$switch/set"
            mqttClient.publish(topic, MqttMessage(command.toByteArray()))
            runOnUiThread {
                textViewStatus.text = "Last Command: $command"
                Toast.makeText(
                    this@DeviceControlActivity,
                    "Command $command sent",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "MQTT Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun sendCommand(command: String, deviceID: String, switch: String) {
        //deviceAdapter.publish_Button("${deviceID}/${switch}/set", deviceID, command)

        val url = "$baseUrl?type=$command&device_id=$deviceID"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@DeviceControlActivity,
                        "Failed to send command",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(
                                this@DeviceControlActivity,
                                "Error: ${response.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        runOnUiThread {
                            textViewStatus.text = "Last Command: $command"
                            Toast.makeText(
                                this@DeviceControlActivity,
                                "Command $command sent",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        })
    }

    private fun sendCommand(command: String, deviceID: String) {
        val url = "$baseUrl?type=$command&device_id=$deviceID"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@DeviceControlActivity,
                        "Failed to send command",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(
                                this@DeviceControlActivity,
                                "Error: ${response.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        runOnUiThread {
                            textViewStatus.text = "Last Command: $command"
                            Toast.makeText(
                                this@DeviceControlActivity,
                                "Command $command sent",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        })
    }

/*    fun updateSwitchUI(layout: LinearLayout, indicator: View, isOn: Boolean) {
        if (isOn) {
            layout.setBackgroundResource(R.drawable.bg_switch_on)
            indicator.setBackgroundResource(R.drawable.indicator_on)
        } else {
            layout.setBackgroundResource(R.drawable.bg_switch_off)
            indicator.setBackgroundResource(R.drawable.indicator_off)
        }
    }*/

    fun updateSwitchUI(layout: LinearLayout, indicator: View, isOn: Boolean) {
        layout.setBackgroundResource(if (isOn) R.drawable.bg_switch_on else R.drawable.bg_switch_off)
        indicator.setBackgroundResource(if (isOn) R.drawable.indicator_on else R.drawable.indicator_off)
    }



    fun setupMqttClient() {
        val brokerUrl = "tcp://taryem.my.id:1883"
        val clientId = "android-${System.currentTimeMillis()}" // biar unik tiap koneksi
        val persistence = MemoryPersistence()

        try {
            mqttClient = MqttClient(brokerUrl, clientId, persistence)
            val options = MqttConnectOptions()
            options.isAutomaticReconnect = true
            options.isCleanSession = true
            options.connectionTimeout = 10
            options.keepAliveInterval = 60

            mqttClient.connect(options)
            mqttClient.subscribe("${device_id}/switch1/status")
            mqttClient.subscribe("${device_id}/switch2/status")

            mqttClient.setCallback(object : MqttCallback {
                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    val payload = message?.payload?.toString(Charsets.UTF_8) ?: ""

                        when (topic) {
                            "${device_id}/switch1/status" -> {
                                isSwitch1On = !isSwitch1On
                                isSwitch1On = payload.equals("ON", ignoreCase = true)
                                updateSwitchUI(switch1Layout, indicator1, isSwitch1On)
                            }
                            "${device_id}/switch2/status" -> {
                                isSwitch2On = !isSwitch2On
                                isSwitch2On = payload.equals("ON", ignoreCase = true)
                                updateSwitchUI(switch2Layout, indicator2, isSwitch2On)
                            }

                    }
                }

                override fun connectionLost(cause: Throwable?) {
                    Log.e("MQTT", "Connection lost: ${cause?.message}")
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {}
            })



        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun disconnectMqtt() {
        if (::mqttClient.isInitialized && mqttClient.isConnected) {
            try {
                mqttClient.disconnect()
                mqttClient.close()
                Log.d("MQTT", "Disconnect2")
                Logger.log("MQTT", "Disconnect2")
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectMqtt()
    }

}
