package com.project.curiosity.api

import com.project.curiosity.model.Data
import com.project.curiosity.model.Request
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiInterface {
    @POST("GET_From_DB")
    suspend fun getData(@Body body:Request):Response<Data>
}