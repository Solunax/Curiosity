package com.project.curiosity.model

import com.google.gson.annotations.SerializedName

data class Data2 (
    @SerializedName("statusCode") val statusCode : Int,
    @SerializedName("length") val length : Int,
    @SerializedName("body") val body : ArrayList<Body2>
)