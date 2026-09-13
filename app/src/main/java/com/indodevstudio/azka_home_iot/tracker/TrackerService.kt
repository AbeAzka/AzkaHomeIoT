package com.indodevstudio.azka_home_iot.tracker

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.indodevstudio.azka_home_iot.R // Sesuaikan jika resource R berbeda
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TrackerService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var notificationManager: NotificationManager
    private val CHANNEL_ID = "TrackerChannel"
    private val NOTIF_ID = 1

    private val TAG = "TrackerServiceError"

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        notificationManager = getSystemService(NotificationManager::class.java)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Buat notifikasi awal saat service pertama kali dinyalakan
        val initialNotification = buildNotification("Menunggu sinyal GPS...")

        // Memulai foreground service dengan notifikasi yang terkunci (ongoing)
        startForeground(NOTIF_ID, initialNotification)

        startTracking()
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startTracking() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000)
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            for (location in result.locations) {
                val koordinatBaru = Koordinat(location.latitude, location.longitude)

                // 1. Update ke Fragment secara real-time
                LocationData.locationLiveData.postValue(koordinatBaru)

                // 2. UBAH ISI KONTEN NOTIFIKASI dengan koordinat terbaru
                updateNotificationContent("Lat: ${location.latitude}, Lon: ${location.longitude}")

                // 3. Kirim ke Database (PHP) via Coroutine
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = TrackerApiClient.instance.kirimLokasi(koordinatBaru)

                        if (response.isSuccessful) {
                            Log.d(TAG, "Berhasil kirim lokasi ke server: ${location.latitude}, ${location.longitude}")
                        } else {
                            Log.e(TAG, "Server merespons error code: ${response.code()} - ${response.errorBody()?.string()}")
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "Gagal mengirim data via Retrofit: ${e.localizedMessage}", e)
                    }
                }
            }
        }
    }

    /**
     * Fungsi pembantu untuk merakit Builder Notifikasi (Dikunci agar tidak bisa di-swipe)
     */
    private fun buildNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GPS Tracker Aktif")
            .setContentText(contentText) // Isi konten notifikasi yang bisa diubah-ubah
            .setSmallIcon(R.drawable.azkahiot) // Bisa diganti ikon aplikasi kamu (misal: R.drawable.ic_notification)
            .setOngoing(true) // <--- KUNCI UTAMA: Membuat notifikasi tidak bisa di-swipe/dihapus
            .setPriority(NotificationCompat.PRIORITY_LOW) // Agar tenang (tidak bunyi/getar terus menerus)
            .setAutoCancel(false)
            .build()
    }

    /**
     * Fungsi untuk memperbarui tampilan isi notifikasi secara dinamis di status bar
     */
    private fun updateNotificationContent(newText: String) {
        val updatedNotification = buildNotification(newText)
        notificationManager.notify(NOTIF_ID, updatedNotification)
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Tracker Service Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel untuk background tracker GPS"
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}