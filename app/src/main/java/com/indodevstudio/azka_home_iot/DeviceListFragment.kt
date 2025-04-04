package com.indodevstudio.azka_home_iot

import android.adservices.topics.Topic
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.indodevstudio.azka_home_iot.API.DeviceSharingService
import com.indodevstudio.azka_home_iot.Adapter.DeviceAdapter
import com.indodevstudio.azka_home_iot.Model.DeviceModel
import com.indodevstudio.azka_home_iot.Model.DeviceViewModel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException

class DeviceListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var deviceAdapter: DeviceAdapter
    private lateinit var tvNoDevices: TextView
    private val deviceList = mutableListOf<DeviceModel>()
    private val deviceViewModel: DeviceViewModel by activityViewModels()
    private var email = ""
    var deviceId = ""
    private var ipAddress = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_device_list, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        tvNoDevices = view.findViewById(R.id.tvNoDevices)
        val sharedPreferences = requireContext().getSharedPreferences("DevicePrefs", Context.MODE_PRIVATE)
        deviceId = sharedPreferences.getString("device_id", null).toString()
        ipAddress = sharedPreferences.getString("device_ip", "0.0.0.0").toString()
        Log.d("MQTT", deviceId)
        //FOR FIREBASE LOGIN (AKA. GOOGLE)
        val fabAddDevice: FloatingActionButton = view.findViewById(R.id.fabAddDevice)
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            email = firebaseUser.email.toString()
        }
        /*deviceAdapter.connect()*/


        //FOR INDODEVSTUDIO LOGIN
        val userData = getUserData()
        val prefs = requireContext().getSharedPreferences("my_prefs", AppCompatActivity.MODE_PRIVATE)
        val authToken = prefs.getString("auth_token", null)
        if (authToken != null) {
            email = userData["email"].toString()
        }

        // 🔹 Muat daftar perangkat lokal sebelum cek shared devices
        loadDeviceList()

        // Jika ada email, baru cek shared devices (opsional)
        val userEmail = email
        fetchDevices(userEmail) {
            fetchSharedDevices(userEmail)
        }

        deviceAdapter = DeviceAdapter(deviceList, object : DeviceAdapter.DeviceActionListener {
            override fun onRenameDevice(device: DeviceModel, position: Int) {
                renameDevice(device, position)
            }

            override fun onDeleteDevice(device: DeviceModel, position: Int) {
                deleteDevice(device, position)
            }

            override fun onResetWiFi(device: DeviceModel) {
                resetDeviceWiFi(device)
            }

            override fun onPublish(device: String) {
                deviceAdapter.publish("sending_order_$deviceId", deviceId, "refresh")
            }

        })
        deviceAdapter.publish("sending_order_$deviceId", deviceId, "refresh")

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = deviceAdapter


        // Observasi ViewModel untuk update data
        deviceViewModel.getDeviceList().observe(viewLifecycleOwner) { devices ->
            deviceList.clear()
            deviceList.addAll(devices)
            deviceAdapter.notifyDataSetChanged()
            updateUI()
        }

        // Tombol tambah perangkat
        fabAddDevice.setOnClickListener {
            val setupFragment = SetupWemosFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, setupFragment)
                .addToBackStack(null)
                .commit()
        }

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


    private fun fetchSharedDevices(userEmail: String) {
        val request = Request.Builder()
            .url("http://ahi.abeazka.my.id/api/arduino/get_shared_devices.php?shared_email=$userEmail")
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Gagal terhubung ke server", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { jsonString ->
                    Log.d("fetchSharedDevices", "Shared devices JSON: $jsonString")
                    Logger.log("fetchSharedDevices", "Shared devices JSON: $jsonString")

                    if (jsonString.isNullOrEmpty() || jsonString == "[]") {
                        requireActivity().runOnUiThread {
                            //fetchDevices(userEmail)
                            Toast.makeText(requireContext(), "Tidak ada perangkat yang dibagikan", Toast.LENGTH_SHORT).show()
                        }
                        return
                    }

                    val sharedDevices = parseDevicesJson(jsonString).map { device ->
                        device.copy(isShared = true) // 🔹 Tandai sebagai perangkat shared
                    }

                    requireActivity().runOnUiThread {
                        val uniqueDevices = (deviceList + sharedDevices).distinctBy { it.isShared  }
                        deviceList.clear()
                        deviceList.addAll(uniqueDevices)
                        deviceViewModel.updateDeviceList(uniqueDevices)
                        updateUI()
                    }
                }
            }
        })
    }

    private fun fetchDevices(userEmail: String, onComplete: () -> Unit) {
        val url = "http://ahi.abeazka.my.id/api/arduino/get_devices.php?owner_email=$userEmail"
        val requestBody = FormBody.Builder()
            .add("owner_email", userEmail)
            .build()
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Gagal mengambil daftar perangkat", Toast.LENGTH_SHORT).show()
                }
                onComplete() // Lanjutkan ke fetchSharedDevices meskipun gagal
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { jsonString ->
                    Logger.log("fetchDevices", "Response: $jsonString") // Tambahkan log ini
                    Log.d("fetchDevices", "Response: $jsonString") // Tambahkan log ini
                    if (!jsonString.isNullOrEmpty() && jsonString != "[]") {
                        val ownedDevices = parseDevicesJson(jsonString).map { device ->
                            if (device.isShared) {
                                device.copy(isShared = true) // 🔹 Jika perangkat dibagikan
                            } else {
                                device.copy(isShared = false) // 🔹 Jika perangkat milik user sendiri
                            }
                        }




                        //val ownedDevices = parseDevicesJson(jsonString)
                        requireActivity().runOnUiThread {

                            val normalDevices = deviceList.map { it.copy(isShared = false) }

                            val uniqueDevices = (normalDevices + ownedDevices).distinctBy { it.isShared }
                            deviceList.clear()
                            deviceList.addAll(uniqueDevices)
                            deviceViewModel.updateDeviceList(uniqueDevices)
                            updateUI()
                        }
                    }
                }
                onComplete() // Lanjutkan ke fetchSharedDevices setelah fetchDevices selesai
            }
        })
    }



    private fun loadLocalDevices(): List<DeviceModel> {
        val sharedPreferences = requireContext().getSharedPreferences("DevicePrefs", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("device_list", null)

        return if (json != null) {
            val gson = com.google.gson.Gson()
            val type = object : com.google.gson.reflect.TypeToken<List<DeviceModel>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }



    private fun parseDevicesJson(jsonString: String?): List<DeviceModel> {
        val devices = mutableListOf<DeviceModel>()

        if (jsonString.isNullOrEmpty()) {
            Logger.log("DeviceListFragment", "No shared devices found.")
            return devices
        }

        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val deviceId = obj.optString("device_id", "")
                val deviceName = getSavedDeviceName(requireContext())
                val deviceIp = getSavedDeviceIP(requireContext())

                val newDevice = DeviceModel(deviceId, deviceName, deviceIp)
                devices.add(newDevice)
            }
        } catch (e: JSONException) {
            Logger.log("DeviceListFragment", "JSON Parsing Error: ${e.message}")
            e.printStackTrace()
        }

        return devices
    }


    private fun getSavedDeviceName(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("DevicePrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("DEVICE_NAME", "No Device") ?: "No Device"
    }

    private fun getSavedDeviceIP(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("DevicePrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("DEVICE_IP", "0.0.0.0") ?: "0.0.0.0"
    }




    private fun saveDeviceName(context: Context, deviceName: String) {
        val sharedPreferences = context.getSharedPreferences("DevicePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("DEVICE_NAME", deviceName)
        editor.apply()
    }
    // 🔹 Fungsi untuk menyimpan daftar perangkat ke SharedPreferences
    private fun saveDeviceList() {
        val sharedPreferences = requireContext().getSharedPreferences("DevicePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val gson = com.google.gson.Gson()
        val json = gson.toJson(deviceList) // 🔹 Konversi list ke JSON
        editor.putString("device_list", json)
        editor.apply()

        Logger.log("DeviceListFragment", "Device list saved: $json")
    }

    // 🔹 Fungsi untuk memuat daftar perangkat dari SharedPreferences
    private fun loadDeviceList() {
        val sharedPreferences = requireContext().getSharedPreferences("DevicePrefs", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("device_list", null)

        if (json != null) {
            val gson = com.google.gson.Gson()
            val type = object : com.google.gson.reflect.TypeToken<List<DeviceModel>>() {}.type
            deviceList.clear()
            deviceList.addAll(gson.fromJson(json, type))

            Logger.log("DeviceListFragment", "Loaded device list: $json")
        }
    }

    // Rename device
    private fun renameDevice(device: DeviceModel, position: Int) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Rename Device")

        val input = android.widget.EditText(requireContext())
        input.setText(device.name)
        builder.setView(input)

        builder.setPositiveButton("Rename") { _, _ ->
            val newName = input.text.toString()
            if (newName.isNotEmpty()) {
                // 🔹 Update nama perangkat tanpa menghilangkan status "isShared"
                val updatedDevice = device.copy(name = newName, isShared = device.isShared)

                // 🔹 Perbarui data di ViewModel & Adapter
                deviceViewModel.updateDeviceName(position, newName, ipAddress)
                deviceList[position] = updatedDevice
                deviceAdapter.notifyItemChanged(position)

                // 🔹 Simpan perubahan
                saveDeviceName(requireContext(), newName)
                updateDev(device.id, newName, device.ipAddress)

                Toast.makeText(requireContext(), "Device renamed to $newName", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(requireContext(), "Rename form must be filled", Toast.LENGTH_SHORT).show()

            }
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }




    // Delete device
    private fun deleteDevice(device: DeviceModel, position: Int) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Device")
        builder.setMessage("Are you sure you want to remove ${device.name}?")

        builder.setPositiveButton("Delete") { _, _ ->
            deviceViewModel.deleteDevice(device)
            //resetDeviceWiFi(device) // 🔹 Reset WiFi setelah delete
            email?.let { deleteDevice(device.id, it) }
            deviceAdapter.publish("sending_order_$deviceId", deviceId, "delete")
            Toast.makeText(requireContext(), "Device deleted", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun deleteDevice(deviceId: String, ownerEmail: String) {
        val requestBody = FormBody.Builder()
            .add("device_id", deviceId)
            .add("owner_email", ownerEmail)
            .build()

        val request = Request.Builder()
            .url("http://ahi.abeazka.my.id/api/arduino/delete_device.php")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Gagal menghapus perangkat", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { jsonString ->
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), jsonString, Toast.LENGTH_SHORT).show()
                        email?.let { fetchDevices(it){

                        } } // 🔹 Refresh daftar perangkat setelah dihapus
                    }
                }
            }
        })
    }

    private fun updateDev(deviceId: String, deviceName: String, deviceIP: String) {
        val userEmail = email// 🔹 Ambil email pengguna saat ini

        val requestBody = FormBody.Builder()
            .add("device_id", deviceId)
            .add("device_name", deviceName)
            .add("device_ip", deviceIP)
            .build()


        val request = Request.Builder()
            .url("http://ahi.abeazka.my.id/api/arduino/update_devices.php")
            .post(requestBody)
            .build()



        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Gagal rename perangkat", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { jsonString ->
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), jsonString, Toast.LENGTH_SHORT).show()
//                        fetchDevices() // 🔹 Refresh daftar perangkat setelah dihapus
                        if (userEmail != null) {
                            fetchSharedDevices(userEmail)
                        }
                    }
                }
            }
        })

    }




    // Reset WiFi
    private fun resetDeviceWiFi(device: DeviceModel) {
        val url = "http://${device.ipAddress}/reset_wifi"
        val requestBody = "".toRequestBody(null) // Body kosong untuk POST
        val request = okhttp3.Request.Builder().url(url).post(requestBody).build()
        val client = okhttp3.OkHttpClient()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to reset WiFi", Toast.LENGTH_SHORT).show()
                    Logger.log("[ERROR RESET WIFI] ", "${e.message}")
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                    requireActivity().runOnUiThread {
                        if (!response.isSuccessful) {
                            Toast.makeText(requireContext(), "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "WiFi Reset Successful", Toast.LENGTH_SHORT).show()

//                            // 🔄 Tunggu 5 detik sebelum pindah ke Fragment Setup WiFi
//                            Handler(Looper.getMainLooper()).postDelayed({
//                                val fragment = SetupWiFiFragment()
//                                val transaction = requireActivity().supportFragmentManager.beginTransaction()
//                                transaction.replace(R.id.fragment_container, fragment)
//                                transaction.addToBackStack(null)
//                                transaction.commit()
//                            }, 5000)
                        }
                    }
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()

        deviceAdapter.disconnectAllMqttClients() // Memutus semua koneksi MQTT
    }







    fun checkDeviceStatus(device: DeviceModel, callback: (Boolean) -> Unit) {
        val url = "http://${device.ipAddress}/status" // Sesuaikan dengan endpoint di Arduino

        val request = okhttp3.Request.Builder()
            .url(url)
            .get()
            .build()

        val client = okhttp3.OkHttpClient()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                callback(false) // Perangkat dianggap offline jika gagal terhubung
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                    callback(response.isSuccessful) // Jika respon sukses, berarti online
                }
            }
        })
    }




    private fun updateUI() {
        if (deviceList.isEmpty()) {
            tvNoDevices.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvNoDevices.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}
