package com.project.curiosity.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.*
import com.project.curiosity.repository.RoomRepository
import com.project.curiosity.room.AppDataBase
import com.project.curiosity.room.Device
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RoomViewModel(application : Application) : AndroidViewModel(application) {
    val data : LiveData<List<Device>>
    private val repository : RoomRepository

    init{
        repository = RoomRepository(AppDataBase.getInstance(application))
        data = repository.data.asLiveData()
    }

    fun insertDeviceData(device: Device){
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertDeviceData(device)
        }
    }

    fun deleteDeviceData(device : Device){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDeviceData(device)
        }
    }
}