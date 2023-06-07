package com.example.mifonelibproj.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import org.linphone.core.tools.Log;

public class HeadsetReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (isInitialStickyBroadcast()) {
            Log.i("[Headset] Received broadcast from sticky cache, ignoring...");
            return;
        }

        String action = intent.getAction();
        if (action.equals(AudioManager.ACTION_HEADSET_PLUG)) {
            int state = intent.getIntExtra("state", 0);
            String name = intent.getStringExtra("name");
            int hasMicrophone = intent.getIntExtra("microphone", 0);

            if (state == 0) {
                Log.i("[Headset] Headset disconnected:" + name);
            } else if (state == 1) {
                Log.i("[Headset] Headset connected:" + name);
                if (hasMicrophone == 1) {
                    Log.i("[Headset] Headset " + name + " has a microphone");
                }
            } else {
                Log.w("[Headset] Unknown headset plugged state: " + state);
            }

//            MifoneManager.getAudioManager().routeAudioToEarPiece();
//            MifoneManager.getCallManager().refreshInCallActions();
        } else if (action.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            // This happens when the user disconnect a headset, so we shouldn't play audio loudly
            Log.i("[Headset] Noisy state detected, most probably a headset has been disconnected");
//            MifoneManager.getAudioManager().routeAudioToEarPiece();
//            MifoneManager.getCallManager().refreshInCallActions();
        } else {
            Log.w("[Headset] Unknown action: " + action);
        }
    }
}