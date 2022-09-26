package com.project.curiosity.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.*
import com.project.curiosity.api.ApiClient
import com.project.curiosity.model.Body
import com.project.curiosity.model.Request
import com.project.curiosity.repository.Repository
import com.project.curiosity.room.AppDataBase
import com.project.curiosity.room.Device
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewModel(application : Application) : AndroidViewModel(application) {
    val nameData : LiveData<List<Device>>
    val roverData get() = _roverData
    private val _roverData = MutableLiveData<Body>()
    private val repository : Repository

    init{
        repository = Repository(ApiClient.getApiClient(), AppDataBase.getInstance(application))
        nameData = repository.nameData.asLiveData()
    }

    // 내부 DB 에서 로버를 새로 추가하는 함수
    fun insertDeviceData(device: Device){
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertDeviceData(device)
        }
    }

    // 내부 DB 에서 로버를 지우는 메소드
    fun deleteDeviceData(device : Device){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDeviceData(device)
        }
    }

    // 서버에서 데이터를 가져오는 메소드
    fun getData(body : Request) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.getData(body)
            if(response.isSuccessful && response.body()!!.statusCode == 200){
                val recentData = response.body()!!.body[0]
                _roverData.postValue(recentData)
            }
        }
    }
}