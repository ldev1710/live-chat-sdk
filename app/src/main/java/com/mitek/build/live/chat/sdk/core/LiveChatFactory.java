package com.mitek.build.live.chat.sdk.core;

public class LiveChatFactory {
    public static void initialize(String apiKey){
        LiveChatSDK.initialize(apiKey);
    }
    public static void fetchConversation(){

    }

    public static boolean isOnline(){
        return true;
    }
}
