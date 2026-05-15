package com.indodevstudio.azka_home_iot

import EventWorker
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MyApp: Application() {

    override fun onCreate() {
        super.onCreate()
        //WorkManager.initialize(this, Configuration.Builder().build())
        setupWorkManager()
    }

    private fun setupWorkManager() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiresStorageNotLow(true)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<EventWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .addTag("EventReminder")
            .setInitialDelay(10, TimeUnit.SECONDS) // Tunggu 10 detik sebelum pertama kali dijalankan
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "EventReminder",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        val workManager = WorkManager.getInstance(this)
        workManager.getWorkInfosForUniqueWork("EventWorker")
            .get() // Dijalankan secara synchronous
            .forEach { workInfo ->
                Log.d("WorkerStatus", "Status: ${workInfo.state}")
            }


        Log.d("WorkManagerSetup", "WorkManager berhasil dijadwalkan")
    }
}