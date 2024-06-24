package com.mitek.build.micall.sdk.util;

import android.util.Log;

public class MiCallLog {
    static String TAG = "MiCall SDK Log";
    public static void logI(String message){
        Log.i(TAG, message);
    }
    public static void logE(String message){
        Log.e(TAG, message);
    }
}
