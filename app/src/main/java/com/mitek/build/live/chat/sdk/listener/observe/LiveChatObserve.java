package com.mitek.build.live.chat.sdk.listener.observe;


import com.mitek.build.live.chat.sdk.model.chat.LCMessage;

public interface LiveChatObserve {
    void onReceiveMessage(LCMessage lcMessage);
}
