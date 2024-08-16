package com.mitek.build.live.chat.sdk.model.chat

data class LCMessageSend (
    val content: String
) {
    override fun toString(): String {
        return "LCMessageSend(content='$content')"
    }
}