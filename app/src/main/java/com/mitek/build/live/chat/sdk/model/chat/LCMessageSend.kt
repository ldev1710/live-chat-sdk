package com.mitek.build.live.chat.sdk.model.chat

import com.mitek.build.live.chat.sdk.model.user.LCSession

data class LCMessageSend (
    val content: String
) {
    override fun toString(): String {
        return "LCMessageSend(content='$content')"
    }
}