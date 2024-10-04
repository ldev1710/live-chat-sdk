package com.mitek.build.live.chat.sdk.core.network

import com.mitek.build.live.chat.sdk.BuildConfig
import com.mitek.build.live.chat.sdk.util.SocketConstant
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object LiveChatAPI {
    var retrofit: Retrofit? = null

    fun setUrl(url: String){
        retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

object ApiClient {
    val apiService: ApiService by lazy {
        LiveChatAPI.retrofit!!.create(ApiService::class.java)
    }
}