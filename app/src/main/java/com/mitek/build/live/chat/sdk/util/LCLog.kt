package com.mitek.build.live.chat.sdk.util

import android.util.Log

object LCLog {
    var TAG: String = "Live-Chat-SDK-Log"
    fun logI(message: String?) {
        Log.i(TAG, message!!)
    }

    fun logE(message: String?) {
        Log.e(TAG, message!!)
    }
}
