package com.project.curiosity.repository

import com.project.curiosity.api.ApiInterface
import com.project.curiosity.model.Data
import com.project.curiosity.model.Request
import com.project.curiosity.room.Device
import com.project.curiosity.room.DeviceDAO
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)
class Repository @Inject constructor(private val api : ApiInterface, private val dataBase : DeviceDAO) {
    val nameData : Flow<List<Device>> = dataBase.getDeviceData()

    suspend fun insertDeviceData(device : Device){
        dataBase.insertDeviceData(device)
    }

    suspend fun deleteDeviceData(device: Device){
        dataBase.deleteDeviceData(device)
    }

    suspend fun getData(request: Request): Response<Data> {
        return api.getData(request)
    }
}