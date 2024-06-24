package com.mitek.build.micall.sdk.core;

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
}
