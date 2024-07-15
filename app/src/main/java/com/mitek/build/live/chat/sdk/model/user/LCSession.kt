package com.mitek.build.live.chat.sdk.model.user

data class LCSession(val sessionId: String, val visitorJid: String) {
    override fun toString(): String {
        return "LCSession(sessionId='$sessionId', visitorJid='$visitorJid')"
    }
}