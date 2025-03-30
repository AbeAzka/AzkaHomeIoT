package com.indodevstudio.azka_home_iot.Model

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.indodevstudio.azka_home_iot.Logger

class DeviceViewModel(application: Application) : AndroidViewModel(application) {

    val deviceList = MutableLiveData<MutableList<DeviceModel>>()
    private val sharedPreferences = application.getSharedPreferences("DevicePrefs", Context.MODE_PRIVATE)

    init {
        loadDeviceList()
    }

    fun getDeviceList(): LiveData<MutableList<DeviceModel>> = deviceList

    fun addDevice(device: DeviceModel) {
        val list = deviceList.value ?: mutableListOf()
        list.add(device)
        deviceList.value = list

        saveDeviceList()
    }

    fun deleteDevice(device: DeviceModel) {
        val list = deviceList.value ?: mutableListOf()
        list.remove(device)
        deviceList.value = list
        saveDeviceList()
    }

    private fun saveDeviceList() {
        val gson = com.google.gson.Gson()
        val json = gson.toJson(deviceList.value)

        sharedPreferences.edit().putString("device_list", json).apply()
        Logger.log("DeviceViewModel", "Device list saved: $json")
    }

    fun updateDeviceList(newList: List<DeviceModel>) {
        deviceList.value = newList.toMutableList()
    }

    fun updateDeviceName(position: Int, newName: String, newIP : String) {
        val currentList = deviceList.value ?: return
        if (position in currentList.indices) {
            currentList[position] = DeviceModel(currentList[position].id, newName, newIP)
            deviceList.value = currentList
        }
    }

    private fun loadDeviceList() {
        val json = sharedPreferences.getString("device_list", null)
        if (json != null) {
            val gson = com.google.gson.Gson()
            val type = object : com.google.gson.reflect.TypeToken<MutableList<DeviceModel>>() {}.type
            val list: MutableList<DeviceModel> = gson.fromJson(json, type)
            deviceList.value = list
            Logger.log("DeviceViewModel", "Loaded device list: $json")
        } else {
            deviceList.value = mutableListOf()
        }
    }
}
