package com.mitek.build.live.chat.sdk.core

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.mitek.build.live.chat.sdk.core.model.LCAccount
import com.mitek.build.live.chat.sdk.listener.publisher.LiveChatListener
import com.mitek.build.live.chat.sdk.model.chat.LCConversation
import com.mitek.build.live.chat.sdk.model.chat.LCMessage
import com.mitek.build.live.chat.sdk.util.LCLog
import com.mitek.build.live.chat.sdk.util.SocketConstant
import io.github.cdimascio.dotenv.dotenv
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONObject
import java.net.URISyntaxException


object LiveChatSDK {
    private var isInitialized = false;
    private var isAvailable = false
    private var listeners: ArrayList<LiveChatListener>? = null
    private var socket: Socket? = null
    private var socketClient: Socket? = null
    private var dotenv = dotenv {
        directory = "./res/raw"
        filename = "env"
    }
    private var currLCAccount: LCAccount? = null

    private fun isValid(): Boolean{
        if(!(isInitialized && isAvailable)){
            LCLog.logE("LiveChatSDK is not ready")
            return false
        }
        return true
    }

    @JvmStatic
    fun observingMessage(lcMessage: LCMessage) {
        if (listeners == null) return
        for (listener in listeners!!) {
            listener.onReceiveMessage(lcMessage)
        }
    }

    @JvmStatic
    fun observingResultConversation(conversations: ArrayList<LCConversation>) {
        if (listeners == null) return
        for (listener in listeners!!) {
            listener.onGotConversation(conversations)
        }
    }

    @JvmStatic
    fun observingAuthorize(success: Boolean,message:String){
        if (listeners == null) return
        for (listener in listeners!!) {
            listener.onAuthStateChanged(success,message)
        }
    }

    fun addEventListener(listener: LiveChatListener){
        if(listeners == null) listeners = ArrayList()
        listeners!!.add(listener)
    }

    fun sendMessage(lcMessage: LCMessage) {
        if(isValid()) socket!!.emit(SocketConstant.SEND_MESSAGE,lcMessage)
    }

    fun getConversation() {
        LCLog.logI("KKK ${isValid()}")
        if(isValid()) {
            socket!!.emit(SocketConstant.FETCH_CONVERSATION, currLCAccount!!.groupId)
        }
    }

    fun getDetailConversation(conversationId: Int) {
        if(isValid()) socket!!.emit(SocketConstant.FETCH_DETAIL_CONVERSATION,conversationId)
    }

    fun authorize(apiKey: String){
        socket!!.emit(SocketConstant.AUTHENTICATION, apiKey)
    }

    fun initialize(context: Context) {
        val isNotificationGranted = NotificationManagerCompat.from(context).areNotificationsEnabled()
        if (!isNotificationGranted) {
            LCLog.logE("LiveChatSDK require post notification permission to use!")
            return
        }
        try {
            socket = IO.socket(dotenv["BASE_URL_SOCKET"])
            socket!!.on(SocketConstant.CONFIRM_CONNECT) { data ->
                LCLog.logI("connected: ${socket!!.connected()}")
            }
            socket!!.connect()
        } catch (e: Exception) {
            LCLog.logE(e.toString())
        }
        socket!!.on(SocketConstant.RESULT_AUTHENTICATION) { data ->
            val jsonObject = data[0] as JSONObject
            val success = jsonObject.getBoolean("status")
            LCLog.logI("DATA_AUTHENTICATION: $jsonObject")
            isAvailable = success
            if(!success){
                //Observe failed
                observingAuthorize(false,"Un-authorized")
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
//                socketClient = IO.socket(SocketConstant.CLIENT_URL_SOCKET)
//                socketClient!!.on(SocketConstant.RESULT_FETCH_DETAIL_CONVERSATION) { data ->
//
//                }
//                socketClient!!.on(SocketConstant.RESULT_FETCH_CONVERSATION) { data ->
//
//                }
//                socketClient!!.connect()

                socket!!.on(SocketConstant.RESULT_FETCH_DETAIL_CONVERSATION) { data ->
                }
                socket!!.on(SocketConstant.RESULT_FETCH_CONVERSATION) { data ->
                    LCLog.logI("RESULT_FETCH_CONVERSATION: "+data[0].toString())
                    var dataResp = data[0] as JSONObject
                    var jsonArray = dataResp.getJSONArray("data")
                    var conversations:ArrayList<LCConversation> = ArrayList()
                    for (i in 0..<jsonArray.length()){
                        var jsonObject = jsonArray.getJSONObject(i)
                        conversations.add(
                            LCConversation(
                                jsonObject.getInt("request_id"),
                                jsonObject.getInt("contact_id"),
                                jsonObject.getString("session_id"),
                                jsonObject.getString("fullname"),
                                jsonObject.getString("email"),
                                jsonObject.getString("avatar"),
                                jsonObject.getString("os"),
                                jsonObject.getString("browser"),
                                jsonObject.getString("browser_lang"),
                            )
                        )
                    }
                    observingResultConversation(conversations)
                }
                observingAuthorize(true,"Authorization successful")
            } catch (ignored: URISyntaxException) {
            }
        }
        listeners = ArrayList()
        isInitialized = true
    }
}
