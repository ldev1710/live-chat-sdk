package com.mitek.build.live.chat.sdk.listener.observe

import com.mitek.build.live.chat.sdk.core.model.LCAccount
import com.mitek.build.live.chat.sdk.model.chat.LCMessage
import com.mitek.build.live.chat.sdk.model.chat.LCSendMessageEnum


interface LiveChatObserve {
    fun onReceiveMessage(lcMessage: LCMessage)
    fun onAuthStateChanged(success: Boolean,message: String,lcAccount: LCAccount?)
    fun onInitialSessionStateChanged(success: Boolean,sessionId:String)
    fun onGotDetailConversation(messages: ArrayList<LCMessage>)
    fun onSendMessageStateChange(state: LCSendMessageEnum, message: LCMessage?)
}
