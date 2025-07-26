package com.indodevstudio.azka_home_iot

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.indodevstudio.azka_home_iot.API.DeviceSharingService
import com.indodevstudio.azka_home_iot.Adapter.DeviceAdapter
import com.indodevstudio.azka_home_iot.Model.DeviceModel
import com.indodevstudio.azka_home_iot.Model.DeviceViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException

class DeviceListFragment : Fragment() {
    private lateinit var wifiManager: WifiManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var deviceAdapter: DeviceAdapter
    private lateinit var tvNoDevices: TextView
    private lateinit var tvListDvc: TextView
    private lateinit var categorySpinner: Spinner
    private lateinit var emptyTextView: TextView
    private lateinit var qrBtn: LinearLayout
    private lateinit var wifibtn: LinearLayout
    private var allDevices: List<DeviceModel> = listOf() // simpan semua hasil fetch
    private val deviceList = mutableListOf<DeviceModel>()
    private val deviceViewModel: DeviceViewModel by activityViewModels()
    private var email = ""
    var deviceId = ""
    var selectedCategory = ""
    private var ipAddress = ""

    lateinit var shimmerLayout : ShimmerFrameLayout
    lateinit var swipeRefresh: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_device_list, container, false)
        wifiManager = requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        shimmerLayout = view.findViewById<ShimmerFrameLayout>(R.id.shimmerLayout)
        swipeRefresh = view.findViewById(R.id.swipeRefreshLayout)
        recyclerView = view.findViewById(R.id.recyclerView)
        tvNoDevices = view.findViewById(R.id.tvNoDevices)
        tvListDvc = view.findViewById(R.id.tvTotalDevice)
        categorySpinner = view.findViewById(R.id.categorySpinner)
        emptyTextView = view.findViewById(R.id.emptyTextView)



        val categories = listOf("All", "Lamp", "Sensor", "Custom")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        categorySpinner.adapter = adapter


        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = categories[position]
                filterDevicesByCategory(selectedCategory)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

//        val sharedPreferences_tutorial = requireContext().getSharedPreferences("AppPrefs",
//            Context.MODE_PRIVATE
//        )
//        val isFirstTime = sharedPreferences_tutorial.getBoolean("isFirstTimes", true)
//
//        if (isFirstTime) {
//            TapTargetSequence(requireActivity())
//                .targets(
//                    TapTarget.forView(view.findViewById(R.id.fabAddDevice), "Add Device", "Klik tombol ini untuk menambahkan perangkat Arduino.")
//                        .cancelable(false)
//                        .transparentTarget(true),
//
//                )
//                .listener(object : TapTargetSequence.Listener {
//                    override fun onSequenceFinish() {
//                        // Tandai bahwa user sudah pernah lihat tutorial
//                        sharedPreferences_tutorial.edit().putBoolean("isFirstTimes", false).apply()
//                    }
//
//                    override fun onSequenceStep(lastTarget: TapTarget, targetClicked: Boolean) {}
//                    override fun onSequenceCanceled(lastTarget: TapTarget) {}
//                }).start()
//        }

        val sharedPreferences2 = context?.getSharedPreferences("Bagogo", Context.MODE_PRIVATE)
        val deviceIdd = sharedPreferences2?.getString("device_id", null)
        val deviceIpp = sharedPreferences2?.getString("device_ip", null)
        if (deviceIdd != null && deviceIpp != null) {
            deviceId = deviceIdd
            ipAddress = deviceIpp
            Log.d("SharedPrefs", "Loaded device_id: $deviceIdd")
            // Lanjut pakai deviceId sesuai kebutuhan
        } else {
            Log.d("SharedPrefs", "No device_id found")
        }

        val defaultDevice = deviceViewModel.getDeviceList().value?.firstOrNull()
        if (defaultDevice != null) {
            deviceId = defaultDevice.id
            ipAddress = defaultDevice.ipAddress
            Log.d("Fallback", "Pakai default deviceId dari ViewModel: $deviceId")
        } else {
            Log.d("Fallback", "Tidak ada device yang bisa dijadikan default.")
        }

        //val sharedPreferences = requireContext().getSharedPreferences("DevicePrefs2", Context.MODE_PRIVATE)
        //ipAddress = sharedPreferences.getString("device_ip", "0.0.0.0").toString()
        Log.d("MQTT", deviceId)
        //FOR FIREBASE LOGIN (AKA. GOOGLE)
        val fabAddDevice: FloatingActionButton = view.findViewById(R.id.fabAddDevice)
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            email = firebaseUser.email.toString()
        }
        /*deviceAdapter.connect()*/
        deviceViewModel.getDeviceList().observe(viewLifecycleOwner) { list ->
            deviceAdapter.updateData(list)
            updateUI()
        }


        //FOR INDODEVSTUDIO LOGIN
        val userData = getUserData()
        val prefs = requireContext().getSharedPreferences("my_prefs", AppCompatActivity.MODE_PRIVATE)
        val authToken = prefs.getString("auth_token", null)
        if (authToken != null) {
            email = userData["email"].toString()
        }
        deviceAdapter = DeviceAdapter(deviceList, object : DeviceAdapter.DeviceActionListener {
            override fun onRenameDevice(device: DeviceModel, position: Int) {
                renameDevice(device, position)
            }

            override fun onDeleteDevice(device: DeviceModel, position: Int) {
                //deleteDevice(device, position)
                deleteDevices(device, device.id, email)
            }

            override fun onResetWiFi(device: DeviceModel) {
                resetDeviceWiFi(device)
            }

            override fun onPublish(device: String) {
                deviceAdapter.publish("sending_order_$deviceId", deviceId, "refresh")
            }
        }, viewLifecycleOwner)

        // 🔹 Muat daftar perangkat lokal sebelum cek shared devices
//        loadDeviceList()
        if (recyclerView.adapter == null) {
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = deviceAdapter
        }


        // Jika ada email, baru cek shared devices (opsional)
        shimmerLayout.startShimmer()
        shimmerLayout.visibility = View.VISIBLE

        loadData{
            updateUI()

        }
        with (swipeRefresh) {
            swipeRefresh?.setOnRefreshListener {
                setRefreshing(true)
                shimmerLayout.startShimmer()
                shimmerLayout.visibility = View.VISIBLE

                loadData {
                    setRefreshing(false) // Hanya setelah data selesai dimuat
                    updateUI()
                }
            }

        }

        // Tombol tambah perangkat
        fabAddDevice.setOnClickListener {
//            val setupFragment = SetupWemosFragment()
//            parentFragmentManager.beginTransaction()
//                .replace(R.id.fragment_container, setupFragment)
//                .addToBackStack(null)
//                .commit()

            // create a new bottom sheet dialog
            val dialog = BottomSheetDialog(requireContext())

            // inflate the layout file of bottom sheet
            val view = layoutInflater.inflate(R.layout.bottom_sheet_dialog, null)
            qrBtn = view.findViewById(R.id.addviaqr)
            wifibtn = view.findViewById(R.id.addviawifi)
            // initialize variable for dismiss button
            //dismissButton = view.findViewById(R.id.dismissButton)
            wifibtn.setOnClickListener{
                val setupFragment = SetupWemosFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, setupFragment)
                    .addToBackStack(null)
                    .commit()
                dialog.dismiss()
            }

//            qrBtn.setOnClickListener{
//
//            }
            // on click event for dismiss button
//            dismissButton.setOnClickListener {
//                // call dismiss method to close the dialog
//                dialog.dismiss()
//            }
            // set cancelable to avoid closing of dialog box when clicking on the screen.
            dialog.setCancelable(true)
            // set content view to our view.
            dialog.setContentView(view)
            // call a show method to display a dialog
            dialog.show()
        }

        return view
    }

    private fun loadData(onFinished: (() -> Unit)? = null) {

        recyclerView.visibility = View.GONE
        tvNoDevices.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            val userEmail = email

            val ownedDevices = async { fetchDevicesSuspend(userEmail) }
            val sharedDevices = async { fetchSharedDevicesSuspend(userEmail) }

            val combinedDevices = (ownedDevices.await() + sharedDevices.await())
                .distinctBy { it.id + it.isShared.toString() }

            allDevices = combinedDevices // biar filter category tetap jalan

            withContext(Dispatchers.Main) {
                // Simpan ke ViewModel supaya adapter dapat
                deviceViewModel.updateDeviceList(combinedDevices)

                filterDevicesByCategory(categorySpinner.selectedItem.toString())

                shimmerLayout.stopShimmer()
                shimmerLayout.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE

                deviceViewModel.getDeviceList().observe(viewLifecycleOwner) { list ->
                    deviceAdapter.updateData(list)
                    updateUI()
                }


                updateUI()
                onFinished?.invoke()
            }
        }
    }


    private suspend fun fetchDevicesSuspend(userEmail: String): List<DeviceModel> = withContext(Dispatchers.IO) {
        val url = "https://www.indodevstudio.my.id/api/arduino/get_devices.php?owner_email=$userEmail"
        val request = Request.Builder().url(url).get().build()

        return@withContext try {
            val response = OkHttpClient().newCall(request).execute()
            val jsonString = response.body?.string().orEmpty()

            Logger.log("fetchDevicesSuspend", "Response: $jsonString")
            Log.d("fetchDevicesSuspend", "Response: $jsonString")

            if (jsonString.isNotEmpty() && jsonString != "[]") {
                val parsedDevices = parseDevicesJson(jsonString)

                val ownedDevices = parsedDevices.map { device ->
                    val safeCategory = device.category?.takeIf { it.isNotBlank() } ?: "Unknown"
                    device.copy(
                        isShared = false, // 💡 dipastikan milik sendiri
                        category = if (device.isShared) safeCategory else (device.category ?: "Unknown")
                    )
                }

                ownedDevices
            } else {
                emptyList()
            }
        } catch (e: IOException) {
            Log.e("fetchDevicesSuspend", "Gagal fetch: ${e.message}")
            emptyList()
        }
    }


    private suspend fun fetchSharedDevicesSuspend(userEmail: String): List<DeviceModel> = withContext(Dispatchers.IO) {
        val url = "https://www.indodevstudio.my.id/api/arduino/get_shared_devices.php?shared_email=$userEmail"
        val request = Request.Builder().url(url).build()

        return@withContext try {
            val response = OkHttpClient().newCall(request).execute()
            val jsonString = response.body?.string().orEmpty()

            Log.d("fetchSharedDevicesSuspend", "Shared devices JSON: $jsonString")
            Logger.log("fetchSharedDevicesSuspend", "Shared devices JSON: $jsonString")

            if (jsonString.isEmpty() || jsonString == "[]") {
                emptyList()
            } else {
                val sharedDevices = parseDevicesJson(jsonString).map { device ->
                    val safeCategory = device.category?.takeIf { it.isNotBlank() } ?: "Unknown"
                    device.copy(
                        isShared = true,
                        category = safeCategory
                    )
                }
                sharedDevices
            }
        } catch (e: IOException) {
            Log.e("fetchSharedDevicesSuspend", "Gagal fetch: ${e.message}")
            emptyList()
        }
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
            .url("https://www.indodevstudio.my.id/api/arduino/get_shared_devices.php?shared_email=$userEmail")
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
                            Toast.makeText(requireContext(), "Tidak ada perangkat yang dibagikan", Toast.LENGTH_SHORT).show()
                        }
                        return
                    }

                    val sharedDevices = parseDevicesJson(jsonString).map { device ->
                        val safeCategory = device.category?.takeIf { it.isNotBlank() } ?: "Unknown"
                        device.copy(
                            isShared = true,
                            category = safeCategory // 🔹 Pastikan kategori dari shared device digunakan
                        )
                    }

                    requireActivity().runOnUiThread {
                        val uniqueDevices = (sharedDevices + deviceList)
                            .distinctBy { it.id + it.isShared.toString() } // 🔹 Supaya tidak bentrok antara milik sendiri dan shared

                        deviceViewModel.updateDeviceList(uniqueDevices)
                        updateUI()
                    }
                }
            }
        })
    }


    private fun fetchDevices(userEmail: String, onComplete: () -> Unit) {
        val url = "https://www.indodevstudio.my.id/api/arduino/get_devices.php?owner_email=$userEmail"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Gagal mengambil daftar perangkat", Toast.LENGTH_SHORT).show()
                }
                onComplete()
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { jsonString ->
                    Logger.log("fetchDevices", "Response: $jsonString")
                    Log.d("fetchDevices", "Response: $jsonString")

                    if (jsonString.isNotEmpty() && jsonString != "[]") {
                        val parsedDevices = parseDevicesJson(jsonString)

                        val ownedDevices = parsedDevices.map { device ->
                            val safeCategory = device.category?.takeIf { it.isNotBlank() } ?: "Unknown"
                            device.copy(
                                category = if (device.isShared) safeCategory else (device.category ?: "Unknown")
                            )
                        }

                        requireActivity().runOnUiThread {
                            val normalDevices = deviceList.map { it.copy(isShared = false) }

                            val uniqueDevices = (normalDevices + ownedDevices)
                                .distinctBy { it.id + it.isShared.toString() }

                            allDevices = uniqueDevices
                            filterDevicesByCategory(categorySpinner.selectedItem.toString())
                            updateUI()
                        }
                    }
                }
                onComplete()
            }
        })
    }


    private fun filterDevicesByCategory(category: String) {
        val filtered = if (category == "All") {
            allDevices

        } else {
            allDevices.filter { it.category.equals(category, ignoreCase = true) }
        }

        deviceViewModel.updateDeviceList(filtered)
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
                val deviceName = obj.optString("device_name", "")
                val deviceIp = getSavedDeviceIP(requireContext(), deviceId)
                val category = obj.optString("category", "Unknown")
                val newDevice = DeviceModel(deviceId, deviceName, deviceIp,category)

                val sharedPrefs = requireContext().getSharedPreferences("device_category", AppCompatActivity.MODE_PRIVATE)

                selectedCategory = sharedPrefs.edit().putString("device_selected_category", "Custom").toString()

                devices.add(newDevice)
            }
        } catch (e: JSONException) {
            Logger.log("DeviceListFragment", "JSON Parsing Error: ${e.message}")
            e.printStackTrace()
        }

        return devices
    }


    private fun getSavedDeviceName(context: Context, deviceId: String): String {
        val sharedPreferences = context.getSharedPreferences("DevicePrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("DEVICE_NAME_$deviceId", "No Device") ?: "No Device"
    }

    private fun getSavedDeviceIP(context: Context, deviceId: String): String {
        val sharedPreferences = context.getSharedPreferences("DevicePrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("DEVICE_IP_$deviceId", "0.0.0.0") ?: "0.0.0.0"
    }

    private fun saveDeviceInfo(deviceId: String, deviceName: String, deviceIp: String) {
        val sharedPreferences = requireContext().getSharedPreferences("DevicePrefs", Context.MODE_PRIVATE)
        with (sharedPreferences.edit()) {
            putString("DEVICE_NAME_$deviceId", deviceName)
            putString("DEVICE_IP_$deviceId", deviceIp)
            apply()
        }
    }




    private fun saveDeviceName(context: Context, deviceName: String, deviceId: String) {
        val sharedPreferences = context.getSharedPreferences("DevicePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("DEVICE_NAME_$deviceId", deviceName)
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
    fun renameDevice( device: DeviceModel, position: Int) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Rename Device")

        val input = android.widget.EditText(requireContext())
        input.setText(device.name)
        builder.setView(input)

        val textInputLayout = TextInputLayout(requireContext()).apply {
            setPadding(50, 40, 50, 0)
            hint = "Masukkan nama"
        }

        val editText = TextInputEditText(textInputLayout.context).apply {
            setText(device.name)
            setTextSize(16f)
        }
        DeviceSharingService.getDeviceStatus(device.id)
        textInputLayout.addView(editText)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Nama")
            .setView(textInputLayout)
            .setPositiveButton("Simpan") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    // 🔹 Salin dan update nama device
                    val updatedDevice = device.copy(name = newName)

                    // 🔹 Update di ViewModel berdasarkan ID (biar aman walau ada device yang shared)
                    deviceViewModel.updateDeviceNameById(device.id, newName)

                    // 🔹 Update local list dan adapter
                    deviceList[position] = updatedDevice
                    deviceAdapter.notifyItemChanged(position)

                    // 🔹 Simpan perubahan ke SharedPreferences/server
                    saveDeviceName(requireContext(), newName, device.id)
                    saveDeviceInfo(device.id, newName, getCurrentIpAddress())
                    updateDev(device.id, newName, ipAddress)

                    Toast.makeText(requireContext(), "Device renamed to $newName", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Rename form must be filled", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }





    // Delete device
    private fun deleteDevice(device: DeviceModel, position: Int) {

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete device")
            .setMessage("Are you sure you want to remove ${device.name}?")
            .setPositiveButton("Yes") { _, _ ->
                deviceViewModel.deleteDevice(device)
                //resetDeviceWiFi(device) // 🔹 Reset WiFi setelah delete
                email.let { deleteDevices(device, device.id, it) }
                deviceAdapter.publish("sending_order_$deviceId", deviceId, "delete")
                Toast.makeText(requireContext(), "Device deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteDevices(device: DeviceModel, deviceId: String, ownerEmail: String) {
//        val requestBody = FormBody.Builder()
//            .add("device_id", deviceId)
//            .add("owner_email", ownerEmail)
//            .build()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete device")
            .setMessage("Are you sure you want to remove ${device.name}?")
            .setPositiveButton("Yes") { _, _ ->
                val json = """
                    {
                        "device_id": "$deviceId",
                        "owner_email": "$ownerEmail"
                    }
                """.trimIndent()

                // Buat RequestBody dengan tipe JSON
                val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())



                val request = Request.Builder()
                    .url("https://www.indodevstudio.my.id/api/arduino/delete_device.php")
                    .delete(requestBody)
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


                deviceViewModel.deleteDevice(device)
                //resetDeviceWiFi(device) // 🔹 Reset WiFi setelah delete
                deviceAdapter.publish("sending_order_$deviceId", deviceId, "delete")
                Toast.makeText(requireContext(), "Device deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()




    }



    private fun updateDev(deviceId: String, deviceName: String, deviceIP: String) {
        val userEmail = email// 🔹 Ambil email pengguna saat ini

//        val requestBody = FormBody.Builder()
//            .add("device_id", deviceId)
//            .add("device_name", deviceName)
//            .add("device_ip", deviceIP)
//            .build()

        // Buat JSON string payload
        val json = """
        {
            "device_id": "$deviceId",
            "device_name": "$deviceName",
            "device_ip": "$deviceIP",
            "owner_email": "$userEmail"
        }
    """.trimIndent()

        // Buat RequestBody dengan tipe JSON
        val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())


        val request = Request.Builder()
            .url("https://www.indodevstudio.my.id/api/arduino/update_devices.php")
            .put(requestBody)
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

    override fun onDestroyView() {
        super.onDestroyView()

        val sharedPreferences = requireContext().getSharedPreferences("Bagogo", Context.MODE_PRIVATE)
        sharedPreferences.edit().remove("device_id").apply()
        sharedPreferences.edit().remove("device_ip").apply()

        Log.d("SharedPrefs", "device_id & device_ip dihapus saat keluar fragment")
        deviceAdapter.disconnectAllMqttClients() // Memutus semua koneksi MQTT
    }







    fun checkDeviceStatus(device: DeviceModel, callback: (Boolean) -> Unit) {
        val url = "https://${device.ipAddress}/status" // Sesuaikan dengan endpoint di Arduino

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


    private fun updateUI() {
        val currentList = deviceViewModel.getDeviceList().value.orEmpty()

        if (currentList.isEmpty()) {
            tvNoDevices.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            tvListDvc.text = "Total Devices: 0"
        } else {
            tvNoDevices.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            tvListDvc.text = "Total Devices: ${currentList.size}"

            // ✅ Update ke adapter
            deviceAdapter.updateData(currentList)

            // 🔁 Refresh ke tiap device
            currentList.forEach { device ->
                Log.d("MQTT", "🔁 Refresh untuk ${device.id}")
                deviceAdapter.publish("sending_order_${device.id}", device.id, "refresh")
            }
        }
    }


}
