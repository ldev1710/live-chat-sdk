package com.mitek.build.live.chat.sdk.model.chat

import com.google.gson.annotations.SerializedName

data class LCContent(
    @SerializedName("content-type")
    val contentType: String,  // 'attachment' or 'text'
    @SerializedName("content-message")
    var contentMessage: Any // String or ArrayList<LCAttachment>
) {
    override fun toString(): String {
        return "LCContent(contentType='$contentType', contentMessage=$contentMessage)"
    }
}