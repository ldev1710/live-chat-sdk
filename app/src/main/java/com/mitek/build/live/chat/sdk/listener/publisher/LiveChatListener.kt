package com.mitek.build.live.chat.sdk.listener.publisher

import com.mitek.build.live.chat.sdk.model.internal.InitialEnum
import com.mitek.build.live.chat.sdk.model.internal.LCAccount
import com.mitek.build.live.chat.sdk.listener.observe.LiveChatObserve
import com.mitek.build.live.chat.sdk.model.chat.LCMessage
import com.mitek.build.live.chat.sdk.model.chat.LCSendMessageEnum
import com.mitek.build.live.chat.sdk.model.internal.LCButtonAction
import com.mitek.build.live.chat.sdk.model.user.LCSession

open class LiveChatListener : LiveChatObserve {
    override fun onReceiveMessage(lcMessage: LCMessage) {
    }

    override fun onInitSDKStateChanged(state: InitialEnum, message: String) {
    }

    override fun onAuthStateChanged(success: Boolean, message: String,lcAccount: LCAccount?) {
    }

    override fun onInitialSessionStateChanged(success: Boolean,lcSession: LCSession) {
    }

    override fun onGotDetailConversation(messages: ArrayList<LCMessage>) {
    }

    override fun onSendMessageStateChange(state: LCSendMessageEnum, message: LCMessage?,errorMessage: String?,mappingId: String?) {
    }

    override fun onRestartScripting(buttonActions: ArrayList<LCButtonAction>) {
    }
}
