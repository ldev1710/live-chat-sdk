package com.mitek.build.live.chat.sdk.listener.observe

import com.mitek.build.live.chat.sdk.core.model.InitialEnum
import com.mitek.build.live.chat.sdk.core.model.LCAccount
import com.mitek.build.live.chat.sdk.model.chat.LCMessage
import com.mitek.build.live.chat.sdk.model.chat.LCSendMessageEnum
import com.mitek.build.live.chat.sdk.model.user.LCSession


interface LiveChatObserve {
    fun onReceiveMessage(lcMessage: LCMessage)
    fun onInitSDKStateChanged(state: InitialEnum,message: String)
    fun onAuthStateChanged(success: Boolean,message: String,lcAccount: LCAccount?)
    fun onInitialSessionStateChanged(success: Boolean,lcSession: LCSession)
    fun onGotDetailConversation(messages: ArrayList<LCMessage>)
    fun onSendMessageStateChange(state: LCSendMessageEnum, message: LCMessage?)
}
