package com.mitek.build.live.chat.sdk.model.chat

data class LCContent(
    val contentType: String,  // 'attachment' or 'text'
    var contentMessage: Any // String or ArrayList<LCAttachment>
) {
    override fun toString(): String {
        return "LCContent(contentType='$contentType', contentMessage=$contentMessage)"
    }
}