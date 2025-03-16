package com.indodevstudio.azka_home_iot.Model

import Event
import Event2
import Event3
import EventReminderWorker
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
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
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider

import java.util.Calendar
import androidx.work.*
import com.indodevstudio.azka_home_iot.API.EventService
import com.indodevstudio.azka_home_iot.API.RetrofitClient
import okhttp3.ResponseBody
import java.util.concurrent.TimeUnit
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class EventViewModel : ViewModel() {
    private val repository = EventRepository()
    //private val repository = EventRepository()
    //val events = MutableLiveData<ArrayList<Event2>?>()
    val selectedDate = MutableLiveData<String>()
    val filteredEvents = MutableLiveData<List<Event>>()

    val eventsLiveData = MutableLiveData<List<Event2>?>()
    private val _events = MutableLiveData<List<Event2>>()
    val events: LiveData<List<Event2>> get() = _events

    fun fetchEvents(email: String) {
        repository.getEvents(email).observeForever { eventList ->
            Log.d("fetchEvents", "Data event diterima: ${eventList.size} items")
            _events.postValue(eventList)
        }
    }

    fun addEvent(event: Event3, email: String) {
        viewModelScope.launch {
            repository.addEvent(event) { success ->
                if (success) {
                    Log.d("EventViewModel", "Fetch ulang data setelah tambah event")
                    fetchEvents(email) // Ambil ulang data
                } else {
                    Log.e("EventViewModel", "Tambah event gagal")
                }
            }
        }
    }

    fun deleteEvent(id: Int, email: String) {
        repository.deleteEvent(id) { success ->
            if (success) {
                fetchEvents(email) // Ambil ulang event berdasarkan email setelah penghapusan berhasil
            }
        }
    }


    private fun getTodayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    /*fun sendNotification(userId: Int, title: String, body: String) {
        RetrofitClient.instance.sendNotification(userId, title, body)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Log.d("FCM", "Notifikasi berhasil dikirim")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("FCM", "Gagal mengirim notifikasi: ${t.message}")
                }
            })
    }*/







}
