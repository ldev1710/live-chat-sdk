package com.mitek.build.live.chat.sdk.listener.publisher

import com.mitek.build.live.chat.sdk.listener.observe.LiveChatObserve
import com.mitek.build.live.chat.sdk.model.chat.LCMessage

class LiveChatListener : LiveChatObserve {
    override fun onReceiveMessage(lcMessage: LCMessage) {
    }

    override fun onAuthStateChanged(success: Boolean, message: String) {
    }

    override fun onGotConversation(conversations: List<LCMessage>) {
    }

    override fun onGotDetailConversation(messages: List<LCMessage>) {
    }
}
