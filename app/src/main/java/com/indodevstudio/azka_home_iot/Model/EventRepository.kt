package com.indodevstudio.azka_home_iot.Model

import ApiService
import ApiService2
import Event
import Server2
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// 4. Repository
class EventRepository {

     fun getEventsForDate(date: String): ArrayList<Event> {
        val apiService = Server.instance.create(ApiService2::class.java)
        return apiService.getEventsByDate(date)
    }

    fun getEvents(callback: (ArrayList<Event>?) -> Unit) {
        Server2.instance.getEvents().enqueue(object : Callback<ArrayList<Event>> {
            override fun onResponse(call: Call<ArrayList<Event>>, response: Response<ArrayList<Event>>) {
                if (response.isSuccessful) {
                    callback(response.body())
                } else {
                    callback(null)
                }
            }
            override fun onFailure(call: Call<ArrayList<Event>>, t: Throwable) {
                callback(null)
            }
        })
    }

    fun addEvent(event: Event, callback: (Boolean) -> Unit) {
        Server2.instance.addEvent(event).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                callback(response.isSuccessful)
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
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
}
