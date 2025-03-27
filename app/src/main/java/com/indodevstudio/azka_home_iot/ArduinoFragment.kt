package com.indodevstudio.azka_home_iot

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.*
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import okhttp3.*
import java.io.IOException

class ArduinoFragment : Fragment() {

    private lateinit var statusTextView: TextView
    private lateinit var editTextSSID: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonSubmit: Button

    private val wemosSSID = "Wemos_Setup" // Wemos AP SSID
    private val wemosPassword = "" // Jika ada password, masukkan di sini
    private lateinit var wifiManager: WifiManager
    private lateinit var wifiReceiver: BroadcastReceiver

    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_arduino, container, false)

        statusTextView = view.findViewById(R.id.statusTextView)
        editTextSSID = view.findViewById(R.id.editTextSSID)
        editTextPassword = view.findViewById(R.id.editTextPassword)
        buttonSubmit = view.findViewById(R.id.buttonSubmit)

        wifiManager = requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        buttonSubmit.setOnClickListener {
            submitNewWiFiCredentials()
        }



        return view
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
        startWiFiScan()
    }

    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(wifiReceiver)
    }

    private fun startWiFiScan() {

        wifiReceiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context?, intent: Intent?) {
                if (ActivityCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // Minta izin lokasi ke pengguna
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                    return
                }

                val results =
                    wifiManager.scanResults
                for (result in results) {
                    if (result.SSID == wemosSSID) {
                        connectToWemos()
                        break
                    }
                }
            }
        }
        requireActivity().registerReceiver(wifiReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        wifiManager.startScan()
    }


    private fun connectToWemos() {
        val wifiConfig = WifiConfiguration().apply {
            SSID = String.format("\"%s\"", wemosSSID)
            preSharedKey = if (wemosPassword.isNotEmpty()) String.format("\"%s\"", wemosPassword) else null
        }

        val netId = wifiManager.addNetwork(wifiConfig)
        wifiManager.disconnect()
        wifiManager.enableNetwork(netId, true)
        wifiManager.reconnect()

        statusTextView.text = "Connecting to Wemos..."

        Thread.sleep(5000) // Beri waktu untuk koneksi

        if (wifiManager.connectionInfo.ssid == "\"$wemosSSID\"") {
            statusTextView.text = "Connected to Wemos"
            buttonSubmit.visibility = View.VISIBLE
        } else {
            statusTextView.text = "Failed to connect"
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

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(activity, "Failed to send credentials", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        activity?.runOnUiThread {
                            Toast.makeText(activity, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        activity?.runOnUiThread {
                            Toast.makeText(activity, "Credentials sent successfully", Toast.LENGTH_SHORT).show()
                            statusTextView.text = "New WiFi credentials sent. Restarting Wemos..."
                        }
                    }
                }
            }
        })
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
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
}
