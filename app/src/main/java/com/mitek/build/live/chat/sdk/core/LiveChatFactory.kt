package com.mitek.build.live.chat.sdk.core

import android.content.Context
import com.mitek.build.live.chat.sdk.listener.publisher.LiveChatListener
import com.mitek.build.live.chat.sdk.model.chat.LCMessageSend
import com.mitek.build.live.chat.sdk.model.user.LCUser

object LiveChatFactory {
    fun initializeSDK(context: Context) {
        LiveChatSDK.initialize(context)
    }

    fun initializeSession(user: LCUser) {
        LiveChatSDK.initializeSession(user)
    }

    fun authorize(apiKey: String){
        LiveChatSDK.authorize(apiKey)
    }

    fun addEventListener(listener: LiveChatListener){
        LiveChatSDK.addEventListener(listener)
    }

    fun sendMessage(message: LCMessageSend){
        LiveChatSDK.sendMessage(message)
    }

    fun getDetailConversation(conversationId: Int) {
        LiveChatSDK.getDetailConversation(conversationId)
    }

    val isOnline: Boolean
        get() = true
}
