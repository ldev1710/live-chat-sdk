package com.mitek.build.live.chat.sdk.core.network

import com.mitek.build.live.chat.sdk.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object LiveChatAPI {
    private const val BASE_URL = BuildConfig.BASE_URL_SOCKET

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

object ApiClient {
    val apiService: ApiService by lazy {
        LiveChatAPI.retrofit.create(ApiService::class.java)
    }
}