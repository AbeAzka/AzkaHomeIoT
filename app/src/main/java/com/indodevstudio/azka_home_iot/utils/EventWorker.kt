import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.indodevstudio.azka_home_iot.Model.EventRepository
import com.indodevstudio.azka_home_iot.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        Log.d("EventWorker", "WorkManager berjalan!") // 🔥 Cek di Logcat
        // ✅ Set Worker sebagai Foreground Service agar tetap berjalan
        setForegroundAsync(createForegroundInfo())

        // ✅ Jalankan pengecekan event
        checkAndSendNotification()
        return Result.success()
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val notification = createNotification()
        return ForegroundInfo(1, notification)
    }

    private fun createNotification(): Notification {
        val channelId = "event_reminder_channel"
        val channelName = "Event Reminder"

        // ✅ Buat Notification Channel untuk Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_LOW
            )
            val manager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Memeriksa Event...")
            .setContentText("Aplikasi sedang berjalan di latar belakang")
            .setSmallIcon(R.drawable.ic_event) // Pastikan ada icon di drawable
            .build()
    }

    private fun checkAndSendNotification() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val repository = EventRepository()
        val eventsToday = repository.getEventsForDate(today) // 🔥 Ambil event dari database

        if (eventsToday.isNotEmpty()) {
            sendNotification("Ada ${eventsToday.size} acara hari ini!", "Jangan lupa untuk mengecek jadwal.")
            Log.d("EventWorker", "Notifikasi dikirim!") // 🔥 Debugging
        } else {
            Log.d("EventWorker", "Tidak ada event hari ini.") // 🔍 Pastikan ini tidak terjadi
        }
    }

    private fun sendNotification(title: String, message: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationBuilder = NotificationCompat.Builder(applicationContext, "event_channel")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_event)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(1, notificationBuilder.build())
    }
}
