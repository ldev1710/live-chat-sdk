package com.mitek.build.live.chat.sdk.core.network

import com.mitek.build.live.chat.sdk.core.model.ResponseUploadFile
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("upload")
    fun uploadFile(
        @Part body: ArrayList<MultipartBody.Part>,
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