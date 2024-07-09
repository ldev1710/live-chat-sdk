package com.mitek.build.live.chat.sdk.core

import android.content.Context
import com.mitek.build.live.chat.sdk.model.chat.LCMessage
import java.util.Objects

object LiveChatFactory {
    fun initialize(apiKey: String, context: Context) {
        LiveChatSDK.initialize(apiKey, context)
    }

    fun sendMessage(message: LCMessage){
        LiveChatSDK.sendMessage(message)
    }

    fun getConversation() {
        return LiveChatSDK.getConversation()
    }

    fun getDetailConversation(conversationId: Int) {
        LiveChatSDK.getDetailConversation(conversationId)
    }

    val isOnline: Boolean
        get() = true
}
