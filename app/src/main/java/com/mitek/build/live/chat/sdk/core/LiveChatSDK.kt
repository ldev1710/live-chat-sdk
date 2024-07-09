package com.mitek.build.live.chat.sdk.core

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.mitek.build.live.chat.sdk.listener.publisher.LiveChatListener
import com.mitek.build.live.chat.sdk.model.chat.LCMessage
import com.mitek.build.live.chat.sdk.util.LCLog
import java.util.Objects

object LiveChatSDK {
    private var isAvailable = false
    private var listeners: List<LiveChatListener>? = null

    @JvmStatic
    fun observingMessage(lcMessage: LCMessage?) {
        if (listeners == null) return
        for (listener in listeners!!) {
            listener.onReceiveMessage(lcMessage)
        }
    }

    fun fetchConversation(): List<Objects> {
        return ArrayList()
    }

    fun fetchDetailConversation(conversationId: Int): List<LCMessage> {
        return ArrayList()
    }

    fun initialize(apiKey: String?, context: Context?) {
        val isNotificationGranted = NotificationManagerCompat.from(context!!).areNotificationsEnabled()
        if (!isNotificationGranted) {
            LCLog.logE("LiveChatSDK require post notification permission to use!")
            return
        }
        listeners = ArrayList()
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task: Task<String> ->
                if (!task.isSuccessful) {
                    LCLog.logE("Fetching FCM registration token failed: " + task.exception)
                    return@addOnCompleteListener
                }
                val token = task.result
                LCLog.logI("Token: $token")
            }
        isAvailable = true
    }
}
