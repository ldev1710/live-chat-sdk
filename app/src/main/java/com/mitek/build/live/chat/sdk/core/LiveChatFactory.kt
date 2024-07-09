package com.mitek.build.live.chat.sdk.core

import android.content.Context
import com.mitek.build.live.chat.sdk.model.chat.LCMessage
import java.util.Objects

object LiveChatFactory {
    fun initialize(apiKey: String?, context: Context?) {
        LiveChatSDK.initialize(apiKey, context)
    }

    fun fetchConversation(): List<Objects> {
        return LiveChatSDK.fetchConversation()
    }

    fun fetchDetailConversation(conversationId: Int): List<LCMessage> {
        return LiveChatSDK.fetchDetailConversation(conversationId)
    }

    val isOnline: Boolean
        get() = true
}
