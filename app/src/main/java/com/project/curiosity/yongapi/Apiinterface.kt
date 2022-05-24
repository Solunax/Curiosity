package com.project.curiosity.yongapi

import com.project.curiosity.model.Data
import com.project.curiosity.model.Request
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface Apiinterface {
    @POST("getDataTest2")
    suspend fun getData1(@Body body:Request):Response<Data>
}