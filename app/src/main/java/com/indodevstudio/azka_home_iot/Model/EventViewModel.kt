package com.indodevstudio.azka_home_iot.Model

import Event2
import Event3
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class EventViewModel : ViewModel() {
    private val repository = EventRepository()
    private val _events = MutableLiveData<List<Event2>>()
    val events: LiveData<List<Event2>> get() = _events
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // Fungsi-fungsi yang sudah ada
    fun fetchEvents(email: String) {
        repository.getEvents(email).observeForever { eventList ->
            Log.d("EventViewModel", "Data event diterima: ${eventList.size} items")
            _events.postValue(eventList)
        }
    }

    fun addEvent(event: Event3, email: String) {
        viewModelScope.launch {
            repository.addEvent(event) { success ->
                if (success) {
                    fetchEvents(email)
                } else {
                    _errorMessage.postValue("Gagal menambahkan event")
                }
            }
        }
    }

    fun deleteEvent(id: Int, email: String) {
        repository.deleteEvent(id) { success ->
            if (success) {
                fetchEvents(email)
            } else {
                _errorMessage.postValue("Gagal menghapus event")
            }
        }
    }

    // Fungsi baru untuk menangani aksi dari notifikasi
    fun markEventsAsComplete(eventNames: List<String>, date: String, email: String) {
        viewModelScope.launch {
            try {
                val matchingEvents = _events.value?.filter { event ->
                    eventNames.contains(event.name) && event.date == date
                } ?: emptyList()

                val results = matchingEvents.map { event ->
                    async {
                        updateEventStatus(
                            event.id, 1,
                            email = email
                        )
                    }
                }.awaitAll()

                if (results.any { false }) {
                    _errorMessage.postValue("Gagal mengupdate status beberapa event")
                } else {
                    fetchEvents(email)
                    _errorMessage.postValue("Event berhasil ditandai sebagai selesai")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Terjadi kesalahan: ${e.message}")
            }
        }
    }




    // Fungsi untuk update status single event
    fun updateEventStatus(eventId: Int, status: Int, email: String) {
        repository.updateEventStatus(eventId, status) { success ->
            if (success) {
                fetchEvents(email)
            } else {
                _errorMessage.postValue("Gagal mengupdate status event")
            }
        }
    }
}