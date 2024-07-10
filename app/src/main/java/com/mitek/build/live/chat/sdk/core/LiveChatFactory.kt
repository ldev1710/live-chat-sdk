package com.mitek.build.live.chat.sdk.core

import android.content.Context
import com.mitek.build.live.chat.sdk.listener.publisher.LiveChatListener
import com.mitek.build.live.chat.sdk.model.chat.LCMessage
import java.util.Objects

object LiveChatFactory {
    fun initialize(context: Context) {
        LiveChatSDK.initialize(context)
    }

    fun authorize(apiKey: String){
        LiveChatSDK.authorize(apiKey)
    }

    fun addEventListener(listener: LiveChatListener){
        LiveChatSDK.addEventListener(listener)
    }

    fun sendMessage(message: LCMessage){
        LiveChatSDK.sendMessage(message)
    }

    fun getConversation() {
        LiveChatSDK.getConversation()
    }

    fun getDetailConversation(conversationId: Int) {
        LiveChatSDK.getDetailConversation(conversationId)
    }

    val isOnline: Boolean
        get() = true
}
