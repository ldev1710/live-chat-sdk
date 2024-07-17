package com.mitek.build.live.chat.sdk.core.model

import com.google.gson.annotations.SerializedName
import com.mitek.build.live.chat.sdk.model.chat.LCMessage

data class ResponseUploadFile(
    @SerializedName("status")
    val status: Boolean,
    val message: String?,
    val data: LCMessage
) {
    override fun toString(): String {
        return "ResponseUploadFile(status=$status, message=$message, data=$data)"
    }
}