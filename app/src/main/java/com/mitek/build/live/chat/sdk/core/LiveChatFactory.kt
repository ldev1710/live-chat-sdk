package com.mitek.build.live.chat.sdk.core

import android.content.Context
import com.mitek.build.live.chat.sdk.listener.publisher.LiveChatListener
import com.mitek.build.live.chat.sdk.model.chat.LCMessageSend
import com.mitek.build.live.chat.sdk.model.internal.LCMessageReceiveSource
import com.mitek.build.live.chat.sdk.model.internal.LCSupportType
import com.mitek.build.live.chat.sdk.model.user.LCSession
import com.mitek.build.live.chat.sdk.model.user.LCUser

object LiveChatFactory {
    fun initializeSDK(context: Context) {
        LiveChatSDK.initialize(context)
    }

    fun sendFileMessage(paths: ArrayList<String>,contentType: String){
        LiveChatSDK.sendFileMessage(paths,contentType)
    }

    fun openChatView(from: Context){
        LiveChatSDK.openChatView(from)
    }

    fun initializeSession(user: LCUser,supportType: LCSupportType) {
        LiveChatSDK.initializeSession(user,supportType)
    }

    fun authorize(apiKey: String){
        LiveChatSDK.authorize(apiKey)
    }

    fun removeEventListener(listener: LiveChatListener){
        LiveChatSDK.removeEventListener(listener)
    }

    fun addEventListener(listener: LiveChatListener){
        LiveChatSDK.addEventListener(listener)
    }

    fun sendMessage(message: LCMessageSend){
        LiveChatSDK.sendMessage(message)
    }

    fun getMessages(offset: Int = 0,limit: Int = 5) {
        LiveChatSDK.getMessages(offset,limit)
    }

    fun setMessageReceiveSource(sources: ArrayList<LCMessageReceiveSource>){
        LiveChatSDK.setMessageReceiveSource(sources)
    }

    fun setUserSession(lcSession: LCSession, lcUser: LCUser){
        LiveChatSDK.setUserSession(lcSession,lcUser)
    }

    fun enableDebug(isEnable: Boolean){
        LiveChatSDK.enableDebug(isEnable)
    }
}
