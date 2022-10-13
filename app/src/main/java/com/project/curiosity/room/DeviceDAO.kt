package com.project.curiosity.room

import androidx.room.*
import androidx.room.OnConflictStrategy.IGNORE
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDAO {
    @Insert(onConflict = IGNORE)
    suspend fun insertDeviceData(device: Device)

    @Query("SELECT * FROM device")
    fun getDeviceData(): Flow<List<Device>>

    @Delete
    suspend fun deleteDeviceData(device: Device)
}