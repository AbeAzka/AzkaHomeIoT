package com.indodevstudio.azka_home_iot.tracker

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.indodevstudio.azka_home_iot.R
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
        // Cek apakah ada perintah pembaruan status notifikasi atau pengaturan dari SettingsBottomSheet
        if (intent?.action == "ACTION_UPDATE_NOTIFICATION") {
            val isNotifActive = intent.getBooleanExtra("notif_aktif", true)

            val notification = buildNotification(if (isNotifActive) "GPS Tracker Aktif" else "Berjalan di background (Notifikasi disembunyikan)", isNotifActive)
            startForeground(NOTIF_ID, notification)

            return START_STICKY
        }

        // Cek jika ada perintah memperbarui interval GPS secara real-time
        if (intent?.action == "ACTION_UPDATE_INTERVAL") {
            // Restart request update lokasi dengan interval baru dari SharedPreferences
            restartTracking()
            return START_STICKY
        }

        // Cek status SharedPreferences saat service pertama kali dinyalakan
        val sharedPrefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val isNotifActive = sharedPrefs.getBoolean("notif_aktif", true)

        val initialNotification = buildNotification("Menunggu sinyal GPS...", isNotifActive)
        startForeground(NOTIF_ID, initialNotification)

        startTracking()
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startTracking() {
        // AMBIL PENGATURAN INTERVAL DARI SharedPreferences (Default 5000ms / 5 detik)
        val sharedPrefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val intervalMillis = sharedPrefs.getLong("interval_millis", 5000L)
        val minIntervalMillis = intervalMillis / 2 // Set minimum setengah dari interval utama

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMillis)
            .setMinUpdateIntervalMillis(minIntervalMillis)
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    // Fungsi untuk memperbarui interval GPS tanpa mematikan service
    @SuppressLint("MissingPermission")
    private fun restartTracking() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        startTracking()
        Log.d(TAG, "Interval tracking GPS berhasil diperbarui sesuai pengaturan.")
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            for (location in result.locations) {
                val userData = getUserData()
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                var userId = ""
                if(firebaseUser != null){
                    userId = firebaseUser.email.toString()
                }else{
                    userId = userData["email"].toString()
                }

                val koordinatBaru = Koordinat(userId.toString(), location.latitude, location.longitude)

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

    private fun buildNotification(contentText: String, isActive: Boolean): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GPS Tracker")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.azkahiot)
            .setOngoing(true)
            .setAutoCancel(false)

        if (!isActive) {
            builder.setPriority(NotificationCompat.PRIORITY_MIN)
                .setContentTitle("Tracker Berjalan")
                .setContentText("Layanan aktif di latar belakang")
        } else {
            builder.setPriority(NotificationCompat.PRIORITY_LOW)
        }

        return builder.build()
    }

    private fun updateNotificationContent(newText: String) {
        val sharedPrefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val isNotifActive = sharedPrefs.getBoolean("notif_aktif", true)

        if (isNotifActive) {
            val updatedNotification = buildNotification(newText, true)
            notificationManager.notify(NOTIF_ID, updatedNotification)
        }
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

    private fun getUserData(): Map<String, String?> {
        val prefs = getSharedPreferences("my_prefs", MODE_PRIVATE)
        return mapOf(
            "token" to prefs.getString("auth_token", null),
            "username" to prefs.getString("username", null),
            "email" to prefs.getString("email", null),
            "avatar" to prefs.getString("avatar", null),
            "isVerified" to prefs.getString("isVerified", null)
        )
    }
}