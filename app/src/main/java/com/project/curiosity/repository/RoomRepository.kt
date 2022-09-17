package com.project.curiosity.repository

import com.project.curiosity.room.AppDataBase
import com.project.curiosity.room.Device
import kotlinx.coroutines.flow.Flow

class RoomRepository(private val db : AppDataBase?) {
    val data : Flow<List<Device>> = db!!.DeviceDAO().getDeviceData()

    suspend fun insertDeviceData(device : Device){
        db!!.DeviceDAO().insertDeviceData(device)
    }

    suspend fun deleteDeviceData(device: Device){
        db!!.DeviceDAO().deleteDeviceData(device)
    }
}