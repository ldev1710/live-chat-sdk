package com.test.excute.core;

import com.test.excute.listener.publisher.MiCallStateListener;
import com.test.excute.model.account.MiCallAccount;

public class MiCallFactory {
    static void init(String apiKey){
        MiCallSDK.init(apiKey);
    }
    static void register(MiCallAccount account){
        MiCallSDK.register(account);
    }

    static void addMiCallListener(MiCallStateListener listener){
        MiCallSDK.addMiCallListener(listener);
    }
}
