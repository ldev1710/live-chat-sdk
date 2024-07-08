package com.mitek.build.live.chat.sdk.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mitek.build.live.chat.sdk.core.LiveChatSDK;
import com.mitek.build.live.chat.sdk.model.chat.LCMessage;

public class LCService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        LCMessage lcMessage = new LCMessage();
        LiveChatSDK.observingMessage(lcMessage);
    }
}
