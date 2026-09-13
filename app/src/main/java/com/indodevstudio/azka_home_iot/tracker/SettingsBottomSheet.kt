package com.indodevstudio.azka_home_iot.tracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.indodevstudio.azka_home_iot.R

class SettingsBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings_bottom_sheet, container, false)

        val switchNotif = view.findViewById<Switch>(R.id.switchNotifikasi)
        val switchAutoStart = view.findViewById<Switch>(R.id.switchAutoStart) // <--- Baru
        val spinnerInterval = view.findViewById<Spinner>(R.id.spinnerInterval)
        val sharedPrefs = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        // 1. Pengaturan Notifikasi
        switchNotif.isChecked = sharedPrefs.getBoolean("notif_aktif", true)
        switchNotif.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("notif_aktif", isChecked).apply()
            val statusTeks = if (isChecked) "Notifikasi Diaktifkan" else "Notifikasi Dimatikan"
            Toast.makeText(requireContext(), statusTeks, Toast.LENGTH_SHORT).show()

            val intent = Intent(requireContext(), TrackerService::class.java).apply {
                action = "ACTION_UPDATE_NOTIFICATION"
                putExtra("notif_aktif", isChecked)
            }
            requireContext().startService(intent)
        }

        // 2. Pengaturan Tracker: Auto-Start saat aplikasi dibuka
        switchAutoStart.isChecked = sharedPrefs.getBoolean("auto_start_tracker", false)
        switchAutoStart.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("auto_start_tracker", isChecked).apply()
            val msg = if (isChecked) "Auto-Start Tracker diaktifkan" else "Auto-Start Tracker dimatikan"
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        // 3. Pengaturan Interval Kirim Lokasi (Dropdown/Spinner)
        val options = arrayOf("Cepat (3 Detik)", "Normal (5 Detik)", "Hemat Baterai (15 Detik)")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, options)
        spinnerInterval.adapter = adapter

        val savedIntervalIndex = sharedPrefs.getInt("interval_index", 1)
        spinnerInterval.setSelection(savedIntervalIndex)

        spinnerInterval.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                sharedPrefs.edit().putInt("interval_index", position).apply()
                val intervalMillis = when (position) {
                    0 -> 3000L
                    1 -> 5000L
                    2 -> 15000L
                    else -> 5000L
                }
                sharedPrefs.edit().putLong("interval_millis", intervalMillis).apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        return view
    }
}