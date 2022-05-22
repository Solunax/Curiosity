package com.project.curiosity.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device")
data class Device(val deviceID:String) {
    @PrimaryKey(autoGenerate = true)
    var number:Int = 0
}