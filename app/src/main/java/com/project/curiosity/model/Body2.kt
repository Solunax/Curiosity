package com.project.curiosity.model

import com.google.gson.annotations.SerializedName

data class Body2 (
    @SerializedName("deviceId") val deviceId : String,
    @SerializedName("timestamp") val timestamp : String,
    @SerializedName("temperature") val temperature : Int,
    @SerializedName("humidity") val humidity : Int,
    @SerializedName("latitude") val latitude : Double,
    @SerializedName("longitude") val longitude : Double
)