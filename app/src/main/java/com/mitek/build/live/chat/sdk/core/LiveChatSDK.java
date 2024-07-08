package com.mitek.build.live.chat.sdk.core;

import static android.provider.Settings.System.getString;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mitek.build.live.chat.sdk.listener.publisher.LiveChatListener;
import com.mitek.build.live.chat.sdk.model.chat.LCMessage;
import com.mitek.build.live.chat.sdk.util.LCLog;

import java.util.ArrayList;
import java.util.List;

public class LiveChatSDK {
    private static boolean isAvailable = false;
    private static List<LiveChatListener> listeners;

    private static boolean isValid(){
        return
    }

    public static void observingMessage(LCMessage lcMessage){
        if(listeners == null) return;
        for(LiveChatListener listener : listeners){
            listener.onReceiveMessage(lcMessage);
        }
    }

    public static void initialize(String apiKey){
        listeners = new ArrayList<>();
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        LCLog.logI("Fetching FCM registration token failed: "+task.getException());
                        return;
                    }
                    String token = task.getResult();
                    LCLog.logI("Token: "+token);
                });
        isAvailable = true;
    }
}
