package com.indodevstudio.azka_home_iot.tracker

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.indodevstudio.azka_home_iot.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class AdminMapBottomSheet : BottomSheetDialogFragment() {

    private lateinit var mapView: MapView
    private lateinit var btnZoomIn: Button
    private lateinit var btnZoomOut: Button
    private lateinit var btnStandard: Button
    private lateinit var btnSatellite: Button
    private lateinit var tvInfoUser: TextView
    private lateinit var btnSalinKoordinat: Button
    private lateinit var containerCheckbox: LinearLayout

    private var selectedLatitude: Double = 0.0
    private var selectedLongitude: Double = 0.0
    private var selectedUserId: String = "-"

    // Menyimpan pemetaan marker berdasarkan userId
    private val userMarkersMap = mutableMapOf<String, Marker>()
    // Menyimpan status checkbox aktif/tidaknya tiap user agar tidak reset saat refresh
    private val userCheckboxStateMap = mutableMapOf<String, Boolean>()

    // Handler untuk auto-refresh data secara berkala
    private val refreshHandler = Handler(Looper.getMainLooper())
    private lateinit var refreshRunnable: Runnable
    private var isFirstLoad = true

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener { dlg ->
            val bottomSheetDialog = dlg as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?

            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                val layoutParams = it.layoutParams
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                it.layoutParams = layoutParams

                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_map_bottom_sheet, container, false)

        Configuration.getInstance().load(requireContext(), requireActivity().getPreferences(Context.MODE_PRIVATE))

        mapView = view.findViewById(R.id.mapAdminView)
        btnZoomIn = view.findViewById(R.id.btnZoomInAdmin)
        btnZoomOut = view.findViewById(R.id.btnZoomOutAdmin)
        btnStandard = view.findViewById(R.id.btnStandardAdmin)
        btnSatellite = view.findViewById(R.id.btnSatelliteAdmin)
        tvInfoUser = view.findViewById(R.id.tvInfoUserAdmin)
        btnSalinKoordinat = view.findViewById(R.id.btnSalinKoordinatAdmin)
        containerCheckbox = view.findViewById(R.id.containerCheckbox)

        setupMap()
        setupActions()
        startPeriodicRefresh()

        return view
    }

    private fun setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(16.0)

        mapView.setOnTouchListener { v, _ ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }
    }

    private fun startPeriodicRefresh() {
        refreshRunnable = object : Runnable {
            override fun run() {
                loadAllUsersData(isFirstLoad)
                isFirstLoad = false
                // Refresh data setiap 5 detik secara otomatis
                refreshHandler.postDelayed(this, 5000)
            }
        }
        refreshHandler.post(refreshRunnable)
    }

    private fun loadAllUsersData(isInitial: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = TrackerApiClient.instance.ambilSemuaLokasi()
                if (response.isSuccessful && response.body()?.status == "success") {
                    val rawList = response.body()?.data ?: emptyList()

                    // FILTER UTAMA: Ambil HANYA 1 data terbaru per user_id (mencegah marker duplikat)
                    val listUser = rawList.distinctBy { it.user_id }

                    withContext(Dispatchers.Main) {
                        if (listUser.isNotEmpty()) {
                            val geoPoints = ArrayList<GeoPoint>()

                            // Jika ini load pertama, set info user pertama
                            if (selectedUserId == "-" && listUser.isNotEmpty()) {
                                selectedUserId = listUser[0].user_id
                                selectedLatitude = listUser[0].latitude
                                selectedLongitude = listUser[0].longitude
                                tvInfoUser.text = "User: $selectedUserId\nLat: $selectedLatitude, Lon: $selectedLongitude"
                            }

                            // Dapatkan daftar user_id aktif saat ini dari server
                            val activeUserIds = listUser.map { it.user_id }.toSet()

                            // Bersihkan marker untuk user yang sudah tidak aktif di server
                            val iterator = userMarkersMap.entries.iterator()
                            while (iterator.hasNext()) {
                                val entry = iterator.next()
                                if (!activeUserIds.contains(entry.key)) {
                                    mapView.overlays.remove(entry.value)
                                    iterator.remove()
                                    userCheckboxStateMap.remove(entry.key)
                                }
                            }

                            if (isInitial) {
                                containerCheckbox.removeAllViews()
                            }

                            for (user in listUser) {
                                val point = GeoPoint(user.latitude, user.longitude)
                                geoPoints.add(point)

                                val isVisible = userCheckboxStateMap[user.user_id] ?: true

                                if (userMarkersMap.containsKey(user.user_id)) {
                                    // UPDATE POSISI MARKER YANG SUDAH ADA (REAL-TIME UPDATE)
                                    val existingMarker = userMarkersMap[user.user_id]!!
                                    existingMarker.position = point
                                    existingMarker.snippet = "Waktu: ${user.waktu}"

                                    // Jika marker yang sedang dipilih posisinya berubah, update teks panel bawah
                                    if (selectedUserId == user.user_id) {
                                        selectedLatitude = user.latitude
                                        selectedLongitude = user.longitude
                                        tvInfoUser.text = "User: $selectedUserId\nLat: $selectedLatitude, Lon: $selectedLongitude"
                                    }
                                } else {
                                    // BUAT MARKER BARU JIKA ADA USER BARU
                                    val marker = Marker(mapView).apply {
                                        position = point
                                        title = user.user_id
                                        snippet = "Waktu: ${user.waktu}"
                                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                        isEnabled = isVisible
                                        setAlpha(if (isVisible) 1.0f else 0.0f)

                                        setOnMarkerClickListener { clickedMarker, _ ->
                                            selectedUserId = user.user_id
                                            selectedLatitude = user.latitude
                                            selectedLongitude = user.longitude
                                            tvInfoUser.text = "User: $selectedUserId\nLat: $selectedLatitude, Lon: $selectedLongitude"
                                            clickedMarker.showInfoWindow()
                                            true
                                        }
                                    }

                                    userMarkersMap[user.user_id] = marker
                                    mapView.overlays.add(marker)

                                    // BUAT CHECKBOX FILTER HANYA SEKALI SAAT AWAL LOAD
                                    if (isInitial) {
                                        val checkBox = CheckBox(requireContext()).apply {
                                            text = user.user_id
                                            isChecked = isVisible
                                            val typedValue = TypedValue()
                                            context.theme.resolveAttribute(R.attr.textContent, typedValue, true)
                                            val color = ContextCompat.getColor(context, typedValue.resourceId)
                                            setTextColor(color)
                                            setPadding(10, 0, 20, 0)

                                            setOnCheckedChangeListener { _, checked ->
                                                userCheckboxStateMap[user.user_id] = checked
                                                userMarkersMap[user.user_id]?.let { m ->
                                                    m.isEnabled = checked
                                                    m.setAlpha(if (checked) 1.0f else 0.0f)
                                                    mapView.invalidate()
                                                }
                                            }
                                        }
                                        containerCheckbox.addView(checkBox)
                                    }
                                }
                            }
                            mapView.invalidate()

                            // Auto-zoom hanya pada saat pertama kali dibuka agar tidak mengganggu interaksi admin saat geser peta
                            if (isInitial && geoPoints.isNotEmpty()) {
                                val boundingBox = BoundingBox.fromGeoPoints(geoPoints)
                                mapView.post {
                                    mapView.zoomToBoundingBox(boundingBox, true, 100)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore error saat background refresh agar tidak mengganggu UI
            }
        }
    }

    private fun setupActions() {
        btnZoomIn.setOnClickListener { mapView.controller.zoomIn() }
        btnZoomOut.setOnClickListener { mapView.controller.zoomOut() }

        btnStandard.setOnClickListener {
            mapView.setTileSource(TileSourceFactory.MAPNIK)
            mapView.invalidate()
            Toast.makeText(requireContext(), "Peta Standar Aktif", Toast.LENGTH_SHORT).show()
        }

        btnSatellite.setOnClickListener {
            mapView.setTileSource(TileSourceFactory.USGS_SAT)
            mapView.invalidate()
            Toast.makeText(requireContext(), "Peta Satelit Aktif", Toast.LENGTH_SHORT).show()
        }

        btnSalinKoordinat.setOnClickListener {
            val text = "$selectedLatitude, $selectedLongitude"
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Koordinat $selectedUserId", text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "Koordinat $selectedUserId disalin!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Hentikan proses refresh background ketika BottomSheet ditutup untuk mencegah memory leak
        refreshHandler.removeCallbacks(refreshRunnable)
    }

    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { super.onPause(); mapView.onPause() }
}