package com.project.curiosity.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Device::class], version = 1)
abstract class AppDataBase : RoomDatabase(){
    abstract fun DeviceDAO() : DeviceDAO

    companion object{
        private var instance:AppDataBase? = null

        fun getInstance(context: Context):AppDataBase?{
            if(instance == null){
                synchronized(AppDataBase::class){
                    instance = Room.databaseBuilder(context, AppDataBase::class.java, "device").build()
                }
            }
            return instance
        }
//        Instance 초기화 코드
//        fun deleteInstance(){
//            instance = null
//        }
    }
}