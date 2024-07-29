package com.mitek.build.live.chat.sdk.core

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import com.mitek.build.live.chat.sdk.model.internal.LCSupportType
import com.mitek.build.live.chat.sdk.listener.publisher.LiveChatListener
import com.mitek.build.live.chat.sdk.model.chat.LCMessageSend
import com.mitek.build.live.chat.sdk.model.user.LCSession
import com.mitek.build.live.chat.sdk.model.user.LCUser
import com.mitek.build.live.chat.sdk.view.LCChatActivity

object LiveChatFactory {
    fun initializeSDK(context: Context) {
        LiveChatSDK.initialize(context)
    }

    fun sendFileMessage(paths: ArrayList<String>){
        LiveChatSDK.sendFileMessage(paths)
    }

    fun openChatView(from: Context,lcSession: LCSession){
        val activity = LCChatActivity(lcSession)
        val intent = Intent(from, activity.javaClass)
        from.startActivity(intent)
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

    fun getMessages(sessionId: String,offset: Int = 0,limit: Int = 5) {
        LiveChatSDK.getMessages(sessionId,offset,limit)
    }

    fun setUserSession(lcSession: LCSession, lcUser: LCUser){
        LiveChatSDK.setUserSession(lcSession,lcUser)
    }

    fun enableDebug(isEnable: Boolean){
        LiveChatSDK.enableDebug(isEnable)
    }
}
