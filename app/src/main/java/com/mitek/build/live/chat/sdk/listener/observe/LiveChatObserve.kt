package com.mitek.build.live.chat.sdk.listener.observe

import com.mitek.build.live.chat.sdk.model.chat.LCMessage


interface LiveChatObserve {
    fun onReceiveMessage(lcMessage: LCMessage)
    fun onAuthStateChanged(success: Boolean,message: String)
    fun onGotConversation(conversations: List<LCMessage>)
    fun onGotDetailConversation(messages: List<LCMessage>)
}
