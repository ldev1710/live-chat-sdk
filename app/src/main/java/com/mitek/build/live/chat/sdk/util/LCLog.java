package com.mitek.build.live.chat.sdk.util;

import android.util.Log;

public class LCLog {
    static String TAG = "Live-Chat-SDK-Log";
    public static void logI(String message){
        Log.i(TAG, message);
    }
    public static void logE(String message){
        Log.e(TAG, message);
    }
}
