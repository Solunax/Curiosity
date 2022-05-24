package com.project.curiosity.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device")
data class Device(@PrimaryKey val deviceID:String) {

}