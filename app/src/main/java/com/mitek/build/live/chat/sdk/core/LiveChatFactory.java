package com.mitek.build.live.chat.sdk.core;

import android.content.Context;

import com.mitek.build.live.chat.sdk.model.chat.LCMessage;

import java.util.List;
import java.util.Objects;

public class LiveChatFactory {
    public static void initialize(String apiKey, Context context){
        LiveChatSDK.initialize(apiKey,context);
    }
    public static List<Objects> fetchConversation(){
        return LiveChatSDK.fetchConversation();
    }
    public static List<LCMessage> fetchDetailConversation(int conversationId){
        return LiveChatSDK.fetchDetailConversation(conversationId);
    }
    public static boolean isOnline(){
        return true;
    }
}
