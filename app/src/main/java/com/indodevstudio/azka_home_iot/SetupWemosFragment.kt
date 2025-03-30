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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.firebase.auth.FirebaseAuth
import com.indodevstudio.azka_home_iot.API.DeviceSharingService
import com.indodevstudio.azka_home_iot.Model.DeviceModel
import com.indodevstudio.azka_home_iot.Model.DeviceViewModel
import okhttp3.OkHttpClient
import okhttp3.*
import java.io.IOException

class SetupWemosFragment : Fragment() {

    private lateinit var statusTextView: TextView
    private lateinit var buttonConnect: Button
    private lateinit var editTextSSID: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonSubmitWiFi: Button
    private lateinit var editTextDeviceName: EditText
    private lateinit var buttonSubmitDevice: Button
    private lateinit var textInfo :TextView
    var IPA = ""
    private lateinit var wifiManager: WifiManager
    private var isReceiverRegistered = false
    private val wemosSSID = "Wemos_Setup"
    private val wemosPassword = ""
    private var email: String? = null

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

        textInfo = view.findViewById(R.id.textInfo)
        statusTextView = view.findViewById(R.id.statusTextView)
        buttonConnect = view.findViewById(R.id.buttonConnect)
        editTextSSID = view.findViewById(R.id.editTextSSID)
        editTextPassword = view.findViewById(R.id.editTextPassword)
        buttonSubmitWiFi = view.findViewById(R.id.buttonSubmitWiFi)
        editTextDeviceName = view.findViewById(R.id.editTextDeviceName)
        buttonSubmitDevice = view.findViewById(R.id.buttonSubmitDevice)

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if(firebaseUser != null){
            email = firebaseUser.email
        }
        val userData = getUserData()
        val prefs = requireContext().getSharedPreferences("my_prefs", AppCompatActivity.MODE_PRIVATE)
        val authToken = prefs.getString("auth_token", null)
        if(authToken != null) {
            email = userData["email"]
        }

        wifiManager = requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        buttonConnect.setOnClickListener {
            startWiFiScan()
        }

        buttonSubmitWiFi.setOnClickListener {
            submitNewWiFiCredentials()
        }

        val deviceViewModel: DeviceViewModel by activityViewModels()

        buttonSubmitDevice.setOnClickListener {
//            getLatestDeviceIp()
            val ownerEmail = email // Ambil dari session/login
            val deviceName = editTextDeviceName.text.toString()
            val deviceIp = getCurrentIpAddress() // Ambil IP Wemos
            val sharedPreferences = requireContext().getSharedPreferences("DevicePrefs", Context.MODE_PRIVATE)
            val deviceId = sharedPreferences.getString("device_id", "UNKNOWN_ID") ?: "UNKNOWN_ID"
            saveDeviceName(requireContext(), deviceName)
            saveDeviceIP(requireContext(), deviceIp)
            if (deviceName.isNotEmpty() && deviceIp.isNotEmpty()) {
                val newDevice = DeviceModel(deviceId,deviceName, deviceIp) // Simpan dengan IP
                deviceViewModel.addDevice(newDevice)
                if (ownerEmail != null) {
                    DeviceSharingService.sendDevice(ownerEmail , deviceName, deviceId, getCurrentIpAddress())
                }

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, DeviceListFragment())
                    .commit()

                Logger.log("SetupWemosFragment", "Device Disimpan: Name = $deviceName, IP = $deviceIp")
            } else {
                Toast.makeText(requireContext(), "Enter device name & ensure connection", Toast.LENGTH_SHORT).show()
            }
        }


        (activity as? AppCompatActivity)?.supportActionBar?.hide()

        val toolbar = view.findViewById<Toolbar>(R.id.toolbarSetup)
        toolbar.setNavigationOnClickListener(View.OnClickListener {
            //What to do on back clicked
            onBackPressed()
        })

        return view
    }

    private fun getUserData(): Map<String, String?> {
        val prefs = requireContext().getSharedPreferences("my_prefs", AppCompatActivity.MODE_PRIVATE)
        return mapOf(
            "token" to prefs.getString("auth_token", null),
            "username" to prefs.getString("username", null),
            "email" to prefs.getString("email", null),
            "avatar" to prefs.getString("avatar", null),
            "isVerified" to prefs.getString("isVerified", null)
        )
    }

     fun onBackPressed() {

        // Menggunakan parentFragmentManager untuk mengganti fragmen di dalam aktivitas
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, DeviceListFragment()) // Gantilah dengan fragmen yang sesuai
            .commit()
    }

    private fun saveDeviceName(context: Context, deviceName: String) {
        val sharedPreferences = context.getSharedPreferences("DevicePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("DEVICE_NAME", deviceName)
        editor.apply()
    }

    private fun saveDeviceIP(context: Context, deviceName: String) {
        val sharedPreferences = context.getSharedPreferences("DevicePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("DEVICE_IP", deviceName)
        editor.apply()
    }



    private fun getCurrentIpAddress(): String {
        val dhcpInfo = wifiManager.dhcpInfo
        val ip = dhcpInfo.gateway // IP Gateway biasanya IP Wemos

        return String.format(
            "%d.%d.%d.%d",
            (ip and 0xFF),
            (ip shr 8 and 0xFF),
            (ip shr 16 and 0xFF),
            (ip shr 24 and 0xFF)
        )
    }

    fun getLatestDeviceIp() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://ahi.abeazka.my.id/api/get_latest_ip.php") // Endpoint untuk mendapatkan IP
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("IP Fetch", "Error fetching IP: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // Mengambil IP dari body response
                    val ip = response.body?.string()?.trim()  // Pastikan response.body tidak null dan ambil IP
                    if (ip != null) {
                        IPA = ip
                        Logger.log("IP", "${ip}")
                    } else {
                        Log.e("IP Fetch", "Received empty IP")
                    }
                } else {
                    Log.e("IP Fetch", "Error: ${response.code}")
                }
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? AppCompatActivity)?.supportActionBar?.show()
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
            val currentIp = getCurrentIpAddress()
            if (wifiManager.connectionInfo.ssid == "\"$wemosSSID\"" && currentIp.isNotEmpty()) {
                statusTextView.text = "Connected to Wemos"
                Logger.log("SetupWemosFragment", "Terhubung ke Wemos: $currentIp")
                textInfo.visibility = View.GONE
                buttonConnect.visibility = View.GONE
                editTextSSID.visibility = View.VISIBLE
                editTextPassword.visibility = View.VISIBLE
                buttonSubmitWiFi.visibility = View.VISIBLE
            } else {
                statusTextView.text = "Failed to connect"
                Logger.log("SetupWemosFragment", "Gagal terhubung ke Wemos")
            }
        }, 5000)

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

                            // Cek apakah IP Wemos berubah setelah koneksi sukses
                            val newIp = getCurrentIpAddress()
                            Logger.log("SetupWemosFragment", "Wemos IP setelah setup: $newIp")

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
