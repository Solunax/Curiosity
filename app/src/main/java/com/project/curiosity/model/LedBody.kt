package com.project.curiosity.model

import com.google.gson.annotations.SerializedName

data class LedBody(
    @SerializedName("statusCode") val statusCode : Int,
    @SerializedName("body") val body:String
)