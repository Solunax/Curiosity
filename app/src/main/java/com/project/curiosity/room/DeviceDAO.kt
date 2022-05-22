package com.project.curiosity.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.Query

@Dao
interface DeviceDAO {
    @Insert(onConflict = IGNORE)
    suspend fun insertDeviceData(device: Device)

    @Query("SELECT * FROM device")
    suspend fun getDeviceData():List<Device>
}