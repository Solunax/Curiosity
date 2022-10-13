package com.project.curiosity.repository

import com.project.curiosity.api.ApiInterface
import com.project.curiosity.model.Data
import com.project.curiosity.model.Request
import com.project.curiosity.room.AppDataBase
import com.project.curiosity.room.Device
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class Repository(private val api : ApiInterface, private val db : AppDataBase?) {
    val nameData : Flow<List<Device>> = db!!.DeviceDAO().getDeviceData()

    suspend fun insertDeviceData(device : Device){
        db!!.DeviceDAO().insertDeviceData(device)
    }

    suspend fun deleteDeviceData(device: Device){
        db!!.DeviceDAO().deleteDeviceData(device)
    }

    suspend fun getData(request: Request): Response<Data> {
        return api.getData(request)
    }
}