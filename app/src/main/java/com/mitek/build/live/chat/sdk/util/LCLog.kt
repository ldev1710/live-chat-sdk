package com.mitek.build.live.chat.sdk.util

import android.util.Log
import com.mitek.build.live.chat.sdk.core.LiveChatSDK

object LCLog {
    var TAG: String = "Live-Chat-SDK-Log"
    fun logI(message: String?) {
        if(LiveChatSDK.isDebugging()) Log.i(TAG, message!!)
    }

    fun logE(message: String?) {
        if(LiveChatSDK.isDebugging()) Log.e(TAG, message!!)
    }
}
