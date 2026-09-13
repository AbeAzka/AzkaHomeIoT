package com.indodevstudio.azka_home_iot.tracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.indodevstudio.azka_home_iot.R
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MapBottomSheet(private val latitude: Double, private val longitude: Double) : BottomSheetDialogFragment() {

    private lateinit var mapView: MapView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Konfigurasi wajib osmdroid agar tile peta dapat dimuat
        Configuration.getInstance().load(
            requireContext(),
            PreferenceManager.getDefaultSharedPreferences(requireContext())
        )

        val view = inflater.inflate(R.layout.dialog_map, container, false)
        mapView = view.findViewById(R.id.mapView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mengatur sumber peta standar OpenStreetMap
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true) // Mengizinkan zoom dengan dua jari

        val mapController = mapView.controller
        mapController.setZoom(18.0) // Mengatur kedekatan zoom

        // Titik koordinat berdasarkan parameter yang dikirim dari Fragment
        val titikLokasi = GeoPoint(latitude, longitude)
        mapController.setCenter(titikLokasi)

        // Menambahkan marker (pin) di peta
        val marker = Marker(mapView)
        marker.position = titikLokasi
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = "Posisi Tracker"
        mapView.overlays.add(marker)
    }

    // Wajib lifecycle osmdroid agar peta tidak freeze / error saat dialog di-pause/resume
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}