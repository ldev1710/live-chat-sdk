package com.mitek.build.live.chat.sdk.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mitek.build.live.chat.sdk.core.LiveChatSDK.observingMessage
import com.mitek.build.live.chat.sdk.model.chat.LCMessage
import com.mitek.build.live.chat.sdk.model.chat.LCSender
import com.mitek.build.live.chat.sdk.util.LCLog
import org.json.JSONObject

open class LCService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        LCLog.logI("data fcm received: $data")
        val from = JSONObject(data["sender"] as String)
        val lcMessage = LCMessage(
            data["id"]!!.toInt(),
            data["content"]!!,
            LCSender(from.getString("id"),from.getString("name")),
            data["created_at"]!!,
        )
        observingMessage(lcMessage)
    }
}
