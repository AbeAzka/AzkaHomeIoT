import android.app.Application
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager

import java.util.concurrent.TimeUnit

class MyApplication  : Application() {
    override fun onCreate() {
        super.onCreate()
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
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "EventReminder",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )

        Log.d("WorkManagerSetup", "WorkManager berhasil dijadwalkan")
    }
}


