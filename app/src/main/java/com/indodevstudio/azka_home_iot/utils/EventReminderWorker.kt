import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.indodevstudio.azka_home_iot.MainActivity
import com.indodevstudio.azka_home_iot.R
import java.text.SimpleDateFormat
import java.util.*

class EventReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val eventList = fetchEventsFromServer() // Ambil event dari server
        checkAndSendNotification(eventList)
        return Result.success()
    }

    private fun fetchEventsFromServer(): List<Event> {
        // Panggil API (pakai Retrofit Synchronous Call atau simpan di lokal)
        return listOf()
    }

    private fun checkAndSendNotification(eventList: List<Event>) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }
        eventList.forEach { event ->
            val eventDate = Calendar.getInstance().apply { time = sdf.parse(event.date) ?: return@forEach }

            if (sdf.format(eventDate.time) == sdf.format(today.time)) {
                sendNotification(event.name, "Hari ini ada event: ${event.name}")
            } else if (sdf.format(eventDate.time) == sdf.format(tomorrow.time)) {
                sendNotification(event.name, "Besok ada event: ${event.name}")
            }
        }
    }

    private fun sendNotification(eventName: String, message: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent untuk membuka EventFragment di MainActivity
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            putExtra("FRAGMENT", "EventFragment")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(applicationContext, "EVENT_CHANNEL")
            .setSmallIcon(R.drawable.ic_event)
            .setContentTitle("Reminder Event")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(Random().nextInt(), builder.build())
    }
}
