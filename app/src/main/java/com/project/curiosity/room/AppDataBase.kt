package com.project.curiosity.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Device::class], version = 1)
abstract class AppDataBase : RoomDatabase(){
    abstract fun DeviceDAO() : DeviceDAO
}