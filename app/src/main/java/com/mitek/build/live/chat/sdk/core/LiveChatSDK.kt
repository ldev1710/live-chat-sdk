package com.mitek.build.live.chat.sdk.core

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.mitek.build.live.chat.sdk.core.model.LCAccount
import com.mitek.build.live.chat.sdk.listener.publisher.LiveChatListener
import com.mitek.build.live.chat.sdk.model.chat.LCMessage
import com.mitek.build.live.chat.sdk.util.LCLog
import com.mitek.build.live.chat.sdk.util.SocketConstant
import io.github.cdimascio.dotenv.dotenv
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException


object LiveChatSDK {
    private var isAvailable = false
    private var listeners: List<LiveChatListener>? = null
    private var socket: Socket? = null
    private var socketClient: Socket? = null
    private var dotenv = dotenv {
        directory = "./res/raw"
        filename = "env"
    }
    private var currLCAccount: LCAccount? = null

    @JvmStatic
    fun observingMessage(lcMessage: LCMessage) {
        if (listeners == null) return
        for (listener in listeners!!) {
            listener.onReceiveMessage(lcMessage)
        }
    }

    fun sendMessage(lcMessage: LCMessage) {
        socketClient!!.emit(SocketConstant.SEND_MESSAGE,lcMessage)
    }

    fun getConversation() {
        socketClient!!.emit(SocketConstant.FETCH_CONVERSATION)
    }

    fun getDetailConversation(conversationId: Int) {
        socketClient!!.emit(SocketConstant.FETCH_DETAIL_CONVERSATION,conversationId)
    }

    fun initialize(apiKey: String, context: Context) {
        val isNotificationGranted = NotificationManagerCompat.from(context).areNotificationsEnabled()
        if (!isNotificationGranted) {
            LCLog.logE("LiveChatSDK require post notification permission to use!")
            return
        }

        try {
            socket = IO.socket(dotenv["BASE_URL_SOCKET"])
            socket!!.on(SocketConstant.CONFIRM_CONNECT) { data ->
                LCLog.logI("connected: ${socket!!.connected()}")
                socket!!.emit(SocketConstant.AUTHENTICATION, apiKey)
            }
            socket!!.connect()
        } catch (e: Exception) {
            LCLog.logE(e.toString())
        }
        socket!!.on(SocketConstant.RESULT_AUTHENTICATION) { data ->
            val jsonObject = data[0] as JSONObject
            val success = jsonObject.getBoolean("status")
            LCLog.logI("RESULT_AUTHENTICATION: $jsonObject")
            if(!success){
                //Observe failed
                return@on
            }
            val dataResp = jsonObject.getJSONObject("data")
            SocketConstant.CLIENT_URL_SOCKET = dataResp.getString("domain_socket")
            currLCAccount = LCAccount(
                dataResp.getInt("id"),
                dataResp.getInt("groupid"),
                dataResp.getString("group_name"),
                dataResp.getString("domain_socket")
            )
            try {
                socketClient = IO.socket(SocketConstant.CLIENT_URL_SOCKET)
                socketClient!!.on(SocketConstant.RESULT_FETCH_DETAIL_CONVERSATION) { data ->

                }
                socketClient!!.on(SocketConstant.RESULT_FETCH_CONVERSATION) { data ->

                }
                socketClient!!.connect()
            } catch (ignored: URISyntaxException) {
            }
        }
        listeners = ArrayList()
        isAvailable = true
    }
}
