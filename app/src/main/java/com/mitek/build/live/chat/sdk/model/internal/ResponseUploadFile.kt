package com.mitek.build.live.chat.sdk.model.internal

import com.google.gson.annotations.SerializedName
import com.mitek.build.live.chat.sdk.model.chat.LCMessage

data class ResponseUploadFile(
    val error: Boolean,
    val message: String?,
    val data: LCMessage
) {
    override fun toString(): String {
        return "ResponseUploadFile(error=$error, message=$message, data=$data)"
    }
}