package com.mitek.build.live.chat.sdk.listener.publisher

import com.mitek.build.live.chat.sdk.listener.observe.LiveChatObserve
import com.mitek.build.live.chat.sdk.model.chat.LCConversation
import com.mitek.build.live.chat.sdk.model.chat.LCMessage

open class LiveChatListener : LiveChatObserve {
    override fun onReceiveMessage(lcMessage: LCMessage) {
    }

    override fun onAuthStateChanged(success: Boolean, message: String) {
    }

    override fun onGotConversation(conversations: ArrayList<LCConversation>) {
    }

    override fun onGotDetailConversation(messages: ArrayList<LCMessage>) {
    }
}
