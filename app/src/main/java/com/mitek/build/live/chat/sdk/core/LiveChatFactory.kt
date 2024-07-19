package com.mitek.build.live.chat.sdk.core

import android.content.Context
import com.mitek.build.live.chat.sdk.core.model.LCSupportType
import com.mitek.build.live.chat.sdk.listener.publisher.LiveChatListener
import com.mitek.build.live.chat.sdk.model.chat.LCMessageSend
import com.mitek.build.live.chat.sdk.model.user.LCSession
import com.mitek.build.live.chat.sdk.model.user.LCUser

object LiveChatFactory {
    fun initializeSDK(context: Context) {
        LiveChatSDK.initialize(context)
    }

    fun sendFileMessage(paths: ArrayList<String>,lcUser: LCUser,lcSession: LCSession){
        LiveChatSDK.sendFileMessage(paths, lcUser, lcSession)
    }

    fun initializeSession(user: LCUser,supportType: LCSupportType) {
        LiveChatSDK.initializeSession(user,supportType)
    }

    fun authorize(apiKey: String){
        LiveChatSDK.authorize(apiKey)
    }

    fun addEventListener(listener: LiveChatListener){
        LiveChatSDK.addEventListener(listener)
    }

    fun sendMessage(lcUser: LCUser, message: LCMessageSend){
        LiveChatSDK.sendMessage(lcUser, message)
    }

    fun getMessages(sessionId: String,offset: Int = 0,limit: Int = 5) {
        LiveChatSDK.getMessages(sessionId,offset,limit)
    }

    val isOnline: Boolean
        get() = true
}
