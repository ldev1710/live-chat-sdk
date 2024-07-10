package com.mitek.build.live.chat.sdk.listener.observe

import com.mitek.build.live.chat.sdk.model.chat.LCConversation
import com.mitek.build.live.chat.sdk.model.chat.LCMessage


interface LiveChatObserve {
    fun onReceiveMessage(lcMessage: LCMessage)
    fun onAuthStateChanged(success: Boolean,message: String)
    fun onGotConversation(conversations: ArrayList<LCConversation>)
    fun onGotDetailConversation(messages: ArrayList<LCMessage>)
}
