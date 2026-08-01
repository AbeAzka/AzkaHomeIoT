package com.indodevstudio.azka_home_iot.Model

import Event2
import Event3
import Server2
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// 4. Repository
class EventRepository {

    private val _events = MutableLiveData<List<Event2>?>()
    /*fun getEvents(email: String): LiveData<ArrayList<Event2>> {
        val eventsLiveData = MutableLiveData<ArrayList<Event2>>() // Pakai ArrayList

        Server2.instance.getAllEvents(email).enqueue(object : Callback<ArrayList<Event2>> {
            override fun onResponse(call: Call<ArrayList<Event2>>, response: Response<ArrayList<Event2>>) {
                if (response.isSuccessful) {

                    eventsLiveData.postValue(response.body() ?: arrayListOf()) // Gunakan ArrayList
                } else {
                    eventsLiveData.postValue(arrayListOf()) // Jika error, kirim list kosong
                }
            }

            override fun onFailure(call: Call<ArrayList<Event2>>, t: Throwable) {
                eventsLiveData.postValue(arrayListOf()) // Jika gagal, kirim list kosong
                Log.e("EventRepository", "Gagal mengambil event: ${t.message}")
            }
        })
        return eventsLiveData
    }*/

    /*fun getEvents(email: String, onResult: (List<Event2>?) -> Unit) {
        Server2.instance.getAllEvents(email).enqueue(object : Callback<ArrayList<Event2>> {
            override fun onResponse(call: Call<ArrayList<Event2>>, response: Response<ArrayList<Event2>>) {
                if (response.isSuccessful) {
                    onResult(response.body())
                } else {
                    onResult(null)
                }
            }
            override fun onFailure(call: Call<ArrayList<Event2>>, t: Throwable) {
                onResult(null)
            }
        })
    }*/
    fun getEvents(email: String): LiveData<List<Event2>> {
        val eventsLiveData = MutableLiveData<List<Event2>>()

        Server2.instance.getAllEvents(email).enqueue(object : Callback<ArrayList<Event2>> {
            override fun onResponse(call: Call<ArrayList<Event2>>, response: Response<ArrayList<Event2>>) {
                if (response.isSuccessful) {
                    Log.d("EventRepository", "Data event: ${response.body()}")
                    eventsLiveData.postValue(response.body() ?: emptyList())
                } else {
                    Log.e("EventRepository", "Gagal mengambil event, response code: ${response.code()}")
                    eventsLiveData.postValue(emptyList())
                }
            }

            override fun onFailure(call: Call<ArrayList<Event2>>, t: Throwable) {
                Log.e("EventRepository", "Gagal mengambil event: ${t.message}")
                eventsLiveData.postValue(emptyList())
            }
        })

        return eventsLiveData
    }





    fun addEvent(event: Event3, callback: (Boolean) -> Unit) {
        Server2.instance.addEvent(event).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d("EventRepository", "Event berhasil ditambahkan: ${response.body()?.string()}")
                    callback(true)
                } else {
                    Log.e("EventRepository", "Gagal: ${response.errorBody()?.string()}")
                    callback(false)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("EventRepository", "Retrofit Error: ${t.message}")
                callback(false)
            }
        })
    }


    fun deleteEvent(id: Int, callback: (Boolean) -> Unit) {
        Server2.instance.deleteEvent(id).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                callback(response.isSuccessful)



            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                callback(false)
            }
        })
    }

    fun updateEventStatus(eventId: Int, status: Int, callback: (Boolean) -> Unit) {
        // Implementasi API call untuk update status event
        Server2.instance.updateEventStatus(eventId, status).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                callback(response.isSuccessful)
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                callback(false)
            }
        })
    }

    fun getEventsByNamesAndDate(names: List<String>, date: String, email: String): List<Event2> {
        // Ini adalah contoh implementasi sederhana
        // Dalam implementasi nyata, Anda mungkin perlu melakukan API call
        return emptyList() // Ganti dengan implementasi sebenarnya
    }
}
