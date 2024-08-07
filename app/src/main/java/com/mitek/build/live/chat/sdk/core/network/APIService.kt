package com.mitek.build.live.chat.sdk.core.network

import com.mitek.build.live.chat.sdk.model.internal.ResponseUploadFile
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("uploadSDK")
    fun uploadFile(
        @Header("Authorization") token: String,
        @Part body: ArrayList<MultipartBody.Part>,
        @Part("mapping_id") mappingId: String,
        @Part("add_message_archive") messageArchive: String,
        @Part("groupid") groupId: Int,
        @Part("reply") reply: Int,
        @Part("type") type: String,
        @Part("from") from: String,
        @Part("name") name: String,
        @Part("session_id") sessionId: String,
        @Part("host_name") hostName: String,
        @Part("visitor_jid") jid: String,
        @Part("is_file") isFile: Int,
    ): Call<ResponseUploadFile>
}