package com.indodevstudio.azka_home_iot.tracker

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.indodevstudio.azka_home_iot.R
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

class MapBottomSheet(
    private val latitude: Double,
    private val longitude: Double
) : BottomSheetDialogFragment() {

    private lateinit var mapView: MapView
    private lateinit var btnZoomIn: Button
    private lateinit var btnZoomOut: Button
    private lateinit var btnSatellite: Button
    private lateinit var btnStandard: Button

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
        val view = inflater.inflate(R.layout.dialog_map, container, false)

        Configuration.getInstance().load(requireContext(), requireActivity().getPreferences(android.content.Context.MODE_PRIVATE))

        mapView = view.findViewById(R.id.mapView)
        btnZoomIn = view.findViewById(R.id.btnZoomIn)
        btnZoomOut = view.findViewById(R.id.btnZoomOut)
        btnSatellite = view.findViewById(R.id.btnSatellite)
        btnStandard = view.findViewById(R.id.btnStandard)

        setupMap()
        setupActions()

        return view
    }

    private fun setupMap() {
        // Set User Agent agar Osmdroid diizinkan mengunduh gambar peta
        Configuration.getInstance().setUserAgentValue(requireContext().packageName)

        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        val mapController = mapView.controller
        mapController.setZoom(18.0)

        // Titik koordinat target
        val startPoint = GeoPoint(latitude, longitude)
        mapController.setCenter(startPoint)

        // ==========================================
        // MENAMBAHKAN PIN / MARKER DI TITIK LOKASI
        // ==========================================
        val marker = org.osmdroid.views.overlay.Marker(mapView)
        marker.position = startPoint
        marker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM)
        marker.title = "Lokasi Tracker Saat Ini"
        marker.snippet = "Lat: $latitude, Lon: $longitude"

        // Masukkan marker ke dalam peta
        mapView.overlays.add(marker)
        mapView.invalidate() // Refresh peta agar marker langsung muncul

        // Mencegah BottomSheet ikut terseret saat peta digeser
        mapView.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }
    }

    private fun setupActions() {
        btnZoomIn.setOnClickListener {
            mapView.controller.zoomIn()
        }

        btnZoomOut.setOnClickListener {
            mapView.controller.zoomOut()
        }

        btnStandard.setOnClickListener {
            mapView.setTileSource(TileSourceFactory.MAPNIK)
            Toast.makeText(requireContext(), "Mode Peta Standar", Toast.LENGTH_SHORT).show()
        }

        btnSatellite.setOnClickListener {
            mapView.setTileSource(TileSourceFactory.USGS_SAT)
            Toast.makeText(requireContext(), "Mode Peta Satelit", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}