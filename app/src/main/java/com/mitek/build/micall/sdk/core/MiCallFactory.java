package com.mitek.build.micall.sdk.core;

import android.content.Context;

import com.mitek.build.micall.sdk.listener.publisher.MiCallStateListener;
import com.mitek.build.micall.sdk.model.account.MiCallAccount;

public class MiCallFactory {
    public static void init(String apiKey){
        MiCallSDK.init(apiKey);
    }
    public static void register(MiCallAccount account){
        MiCallSDK.register(account);
    }

    public static void addMiCallListener(MiCallStateListener listener){
        MiCallSDK.addMiCallListener(listener);
    }

    public static void toggleSpeaker(Context context,boolean isEnable){
        MiCallSDK.toggleSpeaker(context,isEnable);
    }

    public static void hold(){
        MiCallSDK.hold();
    }

    public static void unHold(){
        MiCallSDK.unHold();
    }

    public static void toggleMute(boolean isMute){
        MiCallSDK.toggleMute(isMute);
    }

    public static void makeCall(String phone){
        MiCallSDK.makeCall(phone);
    }

    public static void hangup(){
        MiCallSDK.hangup();
    }
    public static void answer(){
        MiCallSDK.answer();
    }
}
