package com.indodevstudio.azka_home_iot

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.indodevstudio.azka_home_iot.Adapter.DeviceAdapter
import com.indodevstudio.azka_home_iot.Model.DeviceModel
import com.indodevstudio.azka_home_iot.Model.DeviceViewModel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class DeviceListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var deviceAdapter: DeviceAdapter
    private lateinit var tvNoDevices: TextView
    private val deviceList = mutableListOf<DeviceModel>()
    private val deviceViewModel: DeviceViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_device_list, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        tvNoDevices = view.findViewById(R.id.tvNoDevices)
        val fabAddDevice: FloatingActionButton = view.findViewById(R.id.fabAddDevice)
        loadDeviceList() // 🔹 Muat daftar perangkat saat fragment dibuka
        // Inisialisasi adapter dengan listener
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
        })

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
                deviceViewModel.updateDeviceName(position, newName) // Perbarui nama di ViewModel
                deviceAdapter.notifyItemChanged(position)
                Toast.makeText(requireContext(), "Device renamed to $newName", Toast.LENGTH_SHORT).show()
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
            deviceAdapter.publish("sending_order2", "arduino_1", "delete")
            Toast.makeText(requireContext(), "Device deleted", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }


    fun resetDeviceWiFi(deviceIp: String) {
        val client = OkHttpClient()
        val requestBody = "".toRequestBody(null) // Request body kosong untuk POST

        val request = Request.Builder()
            .url("http://$deviceIp/reset_wifi")
            .post(requestBody) // Pastikan pakai POST
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("WiFi Reset", "Failed: ${e.message}")
                Logger.log("Wifi Reset", "Failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("WiFi Reset", "Success: ${response.body?.string()}")
                    Logger.log("WiFi Reset", "Success: ${response.body?.string()}")
                } else {
                    Log.e("WiFi Reset", "Failed: ${response.code}")
                    Logger.log("WiFi Reset", "Failed: ${response.code}")
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

                            // 🔄 Tunggu 5 detik sebelum pindah ke Fragment Setup WiFi
                            Handler(Looper.getMainLooper()).postDelayed({
                                val fragment = SetupWiFiFragment()
                                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                                transaction.replace(R.id.fragment_container, fragment)
                                transaction.addToBackStack(null)
                                transaction.commit()
                            }, 5000)
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





    fun addDevice(name: String, ip: String) {
        val newDevice = DeviceModel(name, ip) // Tambahkan IP Address
        deviceViewModel.addDevice(newDevice)
        saveDeviceList()
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
