package com.indodevstudio.azka_home_iot.tracker

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.indodevstudio.azka_home_iot.R

class TrackerFragment : Fragment() {

    private lateinit var tvKoordinat: TextView
    private lateinit var btnMulai: Button
    private lateinit var btnBukaPeta: Button

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

        // 1. CEK KONDISI NYATA: Apakah Service sedang berjalan di background?
        isTrackingActive = isServiceRunning(TrackerService::class.java)

        // 2. Sesuaikan UI tombol berdasarkan hasil pengecekan tersebut
        updateTrackerUIState(isTrackingActive)

        // Jika service sudah aktif sebelumnya, beri teks informasi di TextView
        if (isTrackingActive) {
            tvKoordinat.text = "Tracker sedang berjalan di background..."
        }

        // Dengarkan perubahan data dari Service secara realtime
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

        btnBukaPeta.setOnClickListener {
            bukaPetaBottomSheet()
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
        updateTrackerUIState(isTracking = true)
    }

    private fun stopBackgroundTracker() {
        val serviceIntent = Intent(requireContext(), TrackerService::class.java)
        requireActivity().stopService(serviceIntent)
        tvKoordinat.text = "Tracker Dihentikan"

        isTrackingActive = false
        updateTrackerUIState(isTracking = false)
    }

    private fun updateTrackerUIState(isTracking: Boolean) {
        if (isTracking) {
            btnMulai.text = "Matikan Tracker"
            btnMulai.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F44336")) // Merah
        } else {
            btnMulai.text = "Mulai Tracker"
            btnMulai.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50")) // Hijau
        }
    }

    /**
     * Fungsi Helper untuk mendeteksi apakah sebuah Service sedang aktif berjalan di sistem
     */
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