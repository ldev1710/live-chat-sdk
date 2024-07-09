package com.mitek.build.live.chat.sdk.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mitek.build.live.chat.sdk.core.LiveChatSDK.observingMessage
import com.mitek.build.live.chat.sdk.model.chat.LCMessage

class LCService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val lcMessage = LCMessage()
        observingMessage(lcMessage)
    }
}
