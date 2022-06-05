package com.project.curiosity.room

import androidx.room.*
import androidx.room.OnConflictStrategy.IGNORE

@Dao
interface DeviceDAO {
    @Insert(onConflict = IGNORE)
    suspend fun insertDeviceData(device: Device)

    @Query("SELECT * FROM device")
    suspend fun getDeviceData():List<Device>

    @Delete
    suspend fun deleteDeviceData(device: Device)
}