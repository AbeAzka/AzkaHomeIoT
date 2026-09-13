package com.indodevstudio.azka_home_iot.tracker

import android.Manifest
import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.indodevstudio.azka_home_iot.R

class TrackerFragment : Fragment() {

    private lateinit var tvKoordinat: TextView
    private lateinit var btnMulai: Button
    private lateinit var btnBukaPeta: Button
    private lateinit var btnSalin: Button      // <--- Tombol baru
    private lateinit var btnBagikan: Button    // <--- Tombol baru

    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    private var isTrackingActive: Boolean = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            startBackgroundTracker()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tracker, container, false)
        tvKoordinat = view.findViewById(R.id.tvKoordinat)
        btnMulai = view.findViewById(R.id.btnMulai)
        btnBukaPeta = view.findViewById(R.id.btnBukaPeta)
        btnSalin = view.findViewById(R.id.btnSalin)       // Inisialisasi
        btnBagikan = view.findViewById(R.id.btnBagikan)   // Inisialisasi

        isTrackingActive = isServiceRunning(TrackerService::class.java)
        updateTrackerUIState(isTrackingActive)

        if (isTrackingActive) {
            tvKoordinat.text = "Tracker sedang berjalan di background..."
        }

        LocationData.locationLiveData.observe(viewLifecycleOwner) { koordinat ->
            tvKoordinat.text = "Latitude: ${koordinat.latitude}\nLongitude: ${koordinat.longitude}"
            currentLatitude = koordinat.latitude
            currentLongitude = koordinat.longitude
        }

        btnMulai.setOnClickListener {
            if (isTrackingActive) {
                stopBackgroundTracker()
            } else {
                checkPermissionsAndStart()
            }
        }

        btnBukaPeta.setOnClickListener { bukaPetaBottomSheet() }

        // Fitur Tambahan 1: Salin Koordinat ke Clipboard
        btnSalin.setOnClickListener {
            val teksKoordinat = "$currentLatitude, $currentLongitude"
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Koordinat GPS", teksKoordinat)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "Koordinat berhasil disalin!", Toast.LENGTH_SHORT).show()
        }

        // Fitur Tambahan 2: Bagikan Lokasi via WhatsApp / Intent
        btnBagikan.setOnClickListener {
            if (currentLatitude == 0.0 && currentLongitude == 0.0) {
                //Toast.(requireContext(), "Lokasi belum didapatkan!", Toast.LENGTH_SHORT).show() // Perbaiki toast jika perlu
                Toast.makeText(requireContext(), "Lokasi belum didapatkan!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val gmmIntentUri = Uri.parse("geo:$currentLatitude,$currentLongitude?q=$currentLatitude,$currentLongitude(Lokasi Saya)")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.whatsapp") // Opsional: arahkan langsung ke WhatsApp atau biarkan umum

            try {
                startActivity(mapIntent)
            } catch (e: Exception) {
                // Jika WhatsApp tidak terinstal, buka opsi bagikan biasa
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Halo, ini posisi terkini saya: https://maps.google.com/?q=$currentLatitude,$currentLongitude")
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(shareIntent, "Bagikan Lokasi Via"))
            }
        }

        return view
    }

    private fun bukaPetaBottomSheet() {
        val bottomSheet = MapBottomSheet(currentLatitude, currentLongitude)
        bottomSheet.show(parentFragmentManager, "MapBottomSheetTag")
    }

    private fun checkPermissionsAndStart() {
        val hasLocationPerm = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasLocationPerm) {
            startBackgroundTracker()
        } else {
            val permissions = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            requestPermissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun startBackgroundTracker() {
        val serviceIntent = Intent(requireContext(), TrackerService::class.java)
        ContextCompat.startForegroundService(requireContext(), serviceIntent)
        isTrackingActive = true
        updateTrackerUIState(true)
    }

    private fun stopBackgroundTracker() {
        val serviceIntent = Intent(requireContext(), TrackerService::class.java)
        requireActivity().stopService(serviceIntent)
        tvKoordinat.text = "Tracker Dihentikan"
        isTrackingActive = false
        updateTrackerUIState(false)
    }

    private fun updateTrackerUIState(isTracking: Boolean) {
        if (isTracking) {
            btnMulai.text = "Matikan Tracker"
            btnMulai.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F44336"))
        } else {
            btnMulai.text = "Mulai Tracker"
            btnMulai.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
        }
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION")
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}