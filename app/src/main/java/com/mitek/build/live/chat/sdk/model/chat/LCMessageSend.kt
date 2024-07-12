package com.mitek.build.live.chat.sdk.model.chat

data class LCMessageSend (
    val content: String,
    val sessionId: String,
) {
    override fun toString(): String {
        return "LCMessageSend(content='$content', sessionId='$sessionId')"
    }
}