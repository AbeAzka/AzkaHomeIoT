package com.indodevstudio.azka_home_iot

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.io.IOException

class SetupWemosFragment : Fragment() {

    private lateinit var statusTextView: TextView
    private lateinit var buttonConnect: Button
    private lateinit var editTextSSID: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonSubmitWiFi: Button
    private lateinit var editTextDeviceName: EditText
    private lateinit var buttonSubmitDevice: Button

    private lateinit var wifiManager: WifiManager
    private var isReceiverRegistered = false
    private val wemosSSID = "Wemos_Setup"
    private val wemosPassword = ""

    private val wifiReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            context?.let { ctx ->
                if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                    return
                }

                val results: List<ScanResult> = wifiManager.scanResults
                for (result in results) {
                    if (result.SSID == wemosSSID) {
                        Logger.log("SetupWemosFragment", "Wemos ditemukan: ${result.SSID}")
                        unregisterReceiverSafe()
                        connectToWemos()
                        return
                    }
                }
                Logger.log("SetupWemosFragment", "Wemos tidak ditemukan")
                statusTextView.text = "Wemos not found. Please try again."
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_setup_wemos, container, false)

        statusTextView = view.findViewById(R.id.statusTextView)
        buttonConnect = view.findViewById(R.id.buttonConnect)
        editTextSSID = view.findViewById(R.id.editTextSSID)
        editTextPassword = view.findViewById(R.id.editTextPassword)
        buttonSubmitWiFi = view.findViewById(R.id.buttonSubmitWiFi)
        editTextDeviceName = view.findViewById(R.id.editTextDeviceName)
        buttonSubmitDevice = view.findViewById(R.id.buttonSubmitDevice)

        wifiManager = requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        buttonConnect.setOnClickListener {
            startWiFiScan()
        }

        buttonSubmitWiFi.setOnClickListener {
            submitNewWiFiCredentials()
        }

        buttonSubmitDevice.setOnClickListener {
            try {
                val deviceName = editTextDeviceName.text.toString()
                if (deviceName.isNotEmpty()) {
                    val bundle = Bundle()
                    bundle.putString("deviceName", deviceName)

                    val deviceListFragment = DeviceListFragment()
                    deviceListFragment.arguments = bundle

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, deviceListFragment)
                        .commit()

                    Logger.log("SetupWemosFragment", "Navigasi ke DeviceListFragment dengan deviceName: $deviceName")
                } else {
                    Toast.makeText(requireContext(), "Please enter a device name", Toast.LENGTH_SHORT).show()
                    Logger.log("SetupWemosFragment", "User tidak memasukkan nama device")
                }
            } catch (e: Exception) {
                Logger.log("SetupWemosFragment", "Error saat navigasi: ${e.message}")
                e.printStackTrace()
            }
        }


        (activity as? AppCompatActivity)?.supportActionBar?.hide()

        return view
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
        startWiFiScan()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiverSafe()
    }

    private fun startWiFiScan() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        statusTextView.text = "Scanning for Wemos..."
        Logger.log("SetupWemosFragment", "Memulai scanning WiFi...")

        if (!isReceiverRegistered) {
            requireContext().registerReceiver(wifiReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
            isReceiverRegistered = true
            Logger.log("SetupWemosFragment", "Receiver WiFi didaftarkan")
        }

        wifiManager.startScan()
    }

    private fun unregisterReceiverSafe() {
        if (isReceiverRegistered) {
            try {
                requireContext().unregisterReceiver(wifiReceiver)
                Logger.log("SetupWemosFragment", "Receiver berhasil di-unregister")
            } catch (e: IllegalArgumentException) {
                Logger.log("SetupWemosFragment", "Receiver tidak terdaftar, tidak perlu di-unregister")
            }
            isReceiverRegistered = false
        }
    }

    private fun connectToWemos() {
        statusTextView.text = "Connecting to Wemos..."
        Logger.log("SetupWemosFragment", "Menghubungkan ke Wemos...")

        val wifiConfig = WifiConfiguration().apply {
            SSID = "\"$wemosSSID\""
            preSharedKey = if (wemosPassword.isNotEmpty()) "\"$wemosPassword\"" else null
        }

        val netId = wifiManager.addNetwork(wifiConfig)
        wifiManager.disconnect()
        wifiManager.enableNetwork(netId, true)
        wifiManager.reconnect()

        Handler().postDelayed({
            if (wifiManager.connectionInfo.ssid == "\"$wemosSSID\"") {
                statusTextView.text = "Connected to Wemos"
                Logger.log("SetupWemosFragment", "Berhasil terhubung ke Wemos")
                buttonConnect.visibility = View.GONE
                editTextSSID.visibility = View.VISIBLE
                editTextPassword.visibility = View.VISIBLE
                buttonSubmitWiFi.visibility = View.VISIBLE
            } else {
                statusTextView.text = "Failed to connect"
                Logger.log("SetupWemosFragment", "Gagal terhubung ke Wemos")
            }
        }, 5000) // Delay untuk memastikan koneksi stabil
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(activity, "Location permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, "Location permission required to scan WiFi networks", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun submitNewWiFiCredentials() {
        val newSSID = editTextSSID.text.toString()
        val newPassword = editTextPassword.text.toString()

        if (newSSID.isEmpty()) {
            Toast.makeText(activity, "Please enter a new SSID", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "http://192.168.4.1/action_page?ssidNew=$newSSID&passNew=$newPassword"

        val request = okhttp3.Request.Builder()
            .url(url)
            .get()
            .build()

        val client = okhttp3.OkHttpClient()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(activity, "Failed to send credentials", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                    if (!response.isSuccessful) {
                        activity?.runOnUiThread {
                            Toast.makeText(activity, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        activity?.runOnUiThread {
                            Toast.makeText(activity, "WiFi Setup Successful!", Toast.LENGTH_SHORT).show()
                            statusTextView.text = "Wemos Configured!"
                            editTextSSID.visibility = View.GONE
                            editTextPassword.visibility = View.GONE
                            buttonSubmitWiFi.visibility = View.GONE
                            editTextDeviceName.visibility = View.VISIBLE
                            buttonSubmitDevice.visibility = View.VISIBLE
                        }
                    }
                }
            }
        })
    }
}
