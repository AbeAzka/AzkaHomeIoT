package com.indodevstudio.azka_home_iot

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.indodevstudio.azka_home_iot.Adapter.DeviceAdapter
import com.indodevstudio.azka_home_iot.Model.DeviceModel
import androidx.fragment.app.setFragmentResultListener

class DeviceListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var deviceAdapter: DeviceAdapter
    private lateinit var tvNoDevices: TextView
    private val deviceList = mutableListOf<DeviceModel>() // Gunakan DeviceModel, bukan String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_device_list, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        tvNoDevices = view.findViewById(R.id.tvNoDevices)
        val fabAddDevice: FloatingActionButton = view.findViewById(R.id.fabAddDevice)

        // Inisialisasi adapter
        deviceAdapter = DeviceAdapter(deviceList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = deviceAdapter

        // Cek apakah ada device atau tidak
        updateUI()

        // Tombol tambah device
        fabAddDevice.setOnClickListener {
            val setupFragment = SetupWemosFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, setupFragment)
                .addToBackStack(null)
                .commit()
        }

        setFragmentResultListener("ADD_DEVICE") { _, bundle ->
            val newDevice = bundle.getString("DEVICE_NAME")
            if (!newDevice.isNullOrEmpty()) {
                addDevice(newDevice)
            }
        }

        return view
    }

    fun addDevice(deviceName: String) {
        deviceList.add(DeviceModel(deviceName)) // Simpan sebagai DeviceModel
        deviceAdapter.notifyDataSetChanged()
        updateUI()
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

