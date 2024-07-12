package com.mitek.build.live.chat.sdk.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mitek.build.live.chat.sdk.core.LiveChatSDK.observingMessage
import com.mitek.build.live.chat.sdk.model.chat.LCMessage
import com.mitek.build.live.chat.sdk.model.chat.LCSender

class LCService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        val lcMessage = LCMessage(
            data["id"]!!.toInt(),
            data["content"]!!,
            LCSender("0",""),
            data["created_at"]!!,
        )
        observingMessage(lcMessage)
    }
}
