package com.project.curiosity.yongapi

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.project.curiosity.api.ApiInterface
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient1 {
    private val BASE_URL:String = "https://hwghawcyc9.execute-api.ap-northeast-2.amazonaws.com/default/"

    private fun getRetrofit(): Retrofit {
        val gson: Gson = GsonBuilder().setLenient().create()
        val httpLoggingInterceptor: HttpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client: OkHttpClient = OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor).build()

        return Retrofit.Builder().baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
            .build()
    }

    fun getApiClient1(): Apiinterface {
        return getRetrofit().create(Apiinterface::class.java)
    }
}