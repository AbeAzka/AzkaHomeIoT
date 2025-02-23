package com.indodevstudio.azka_home_iot.Model

import Event
import EventReminderWorker
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indodevstudio.azka_home_iot.R
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider

import java.util.Calendar
import androidx.work.*
import java.util.concurrent.TimeUnit


class EventViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = EventRepository()
    val events = MutableLiveData<List<Event>?>()

    init {
        createNotificationChannel()
        scheduleDailyWork()
    }

    private fun scheduleDailyWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<EventReminderWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .setInitialDelay(1, TimeUnit.MINUTES) // Untuk tes, nanti ganti ke jam tertentu
            .build()

        WorkManager.getInstance(getApplication()).enqueueUniquePeriodicWork(
            "EventReminder",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
    fun fetchEvents() {
        repository.getEvents { eventList ->
            events.postValue(eventList)
            eventList?.let { checkAndSendNotification(it) }
        }
    }

    fun addEvent(event: Event) {
        viewModelScope.launch {
            repository.addEvent(event) { success ->
                if (success) fetchEvents()
            }
        }
    }

    fun deleteEvent(id: Int) {
        viewModelScope.launch {
            repository.deleteEvent(id) { success ->
                if (success) fetchEvents()
            }
        }
    }

    private fun checkAndSendNotification(eventList: List<Event>?) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Calendar.getInstance()
        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }


        eventList?.forEach { event ->
            val eventDate = Calendar.getInstance().apply {
                time = sdf.parse(event.date) ?: return@forEach
            }

            val yesterday = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -1)
            }

            if (sdf.format(eventDate.time) == sdf.format(today.time)) {
                sendNotification(event.name, "Hari ini ada agenda: ${event.name}")
            } else if (sdf.format(eventDate.time) == sdf.format(tomorrow.time)) {
                sendNotification(event.name, "Besok ada agenda: ${event.name}")
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Event Reminder"
            val descriptionText = "Notifikasi untuk mengingatkan event"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("EVENT_CHANNEL", name, importance).apply {
                description = descriptionText
            }
            val notificationManager =
                getApplication<Application>().getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(eventName: String, message: String) {
        val context = getApplication<Application>().applicationContext
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(context, "EVENT_CHANNEL")
            .setSmallIcon(R.drawable.ic_event)
            .setContentTitle("Reminder Event")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(Random().nextInt(), builder.build())
    }
}
