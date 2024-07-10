package com.mitek.build.live.chat.sdk.core

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.mitek.build.live.chat.sdk.core.model.LCAccount
import com.mitek.build.live.chat.sdk.core.model.LCSupportType
import com.mitek.build.live.chat.sdk.listener.publisher.LiveChatListener
import com.mitek.build.live.chat.sdk.model.chat.LCMessage
import com.mitek.build.live.chat.sdk.model.chat.LCMessageSend
import com.mitek.build.live.chat.sdk.model.chat.LCSendMessageEnum
import com.mitek.build.live.chat.sdk.model.user.LCUser
import com.mitek.build.live.chat.sdk.util.LCLog
import com.mitek.build.live.chat.sdk.util.PrefUtil
import com.mitek.build.live.chat.sdk.util.SocketConstant
import io.github.cdimascio.dotenv.dotenv
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


@SuppressLint("StaticFieldLeak")
object LiveChatSDK {
    private var isInitialized = false;
    private var isAvailable = false
    private var listeners: ArrayList<LiveChatListener>? = null
    private var context: Context? = null
    private var socket: Socket? = null
    private var socketClient: Socket? = null
    private var dotenv = dotenv {
        directory = "./res/raw"
        filename = "env"
    }
    private var currLCAccount: LCAccount? = null

    private fun isValid(): Boolean {
        if (!(isInitialized && isAvailable)) {
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
    fun observingAuthorize(success: Boolean, message: String,lcAccount: LCAccount?) {
        if (listeners == null) return
        for (listener in listeners!!) {
            listener.onAuthStateChanged(success, message, lcAccount)
        }
    }

    @JvmStatic
    fun observingInitialSession(success: Boolean, sessionId: String) {
        if (listeners == null) return
        for (listener in listeners!!) {
            listener.onInitialSessionStateChanged(success, sessionId)
        }
    }

    @JvmStatic
    fun observingSendMessage(state: LCSendMessageEnum, message: LCMessage?) {
        if (listeners == null) return
        for (listener in listeners!!) {
            listener.onSendMessageStateChange(state, message)
        }
    }


    @OptIn(ExperimentalEncodingApi::class)
    private fun base64(rawString: String): String {
        return Base64.encode(rawString.toByteArray())
    }

    fun initializeSession(user: LCUser) {
        if (isValid()) {
            val body = JSONObject()
            body.put(base64("groupid"),currLCAccount!!.groupId.toString())
            body.put(base64("host_name"),currLCAccount!!.hostName)
            body.put(base64("visitor_name"),user.fullName)
            body.put(base64("visitor_email"),user.email)
            body.put(base64("visitor_phone"),user.phone)
            body.put(base64("url_visit"),user.deviceName)
            socketClient!!.emit(SocketConstant.INITIALIZE_SESSION, body)
        }
    }

    fun addEventListener(listener: LiveChatListener) {
        if (listeners == null) listeners = ArrayList()
        listeners!!.add(listener)
    }

    fun sendMessage(lcMessage: LCMessageSend) {
        if (isValid()) {
            val jsonObject = JSONObject()
            jsonObject.put(base64("body"),lcMessage.content)
            jsonObject.put(base64("add_message_archive"),"")
            jsonObject.put(base64("groupid"), currLCAccount!!.groupId)
            jsonObject.put(base64("session_id"), PrefUtil.getString("currSessionId"))
            jsonObject.put(base64("host_name"), currLCAccount!!.hostName)
            LCLog.logI("Send message with: $jsonObject")
            socketClient!!.emit(SocketConstant.SEND_MESSAGE, jsonObject)
            observingSendMessage(LCSendMessageEnum.SENDING,null)
        }
    }

    fun getDetailConversation(conversationId: Int) {
        if (isValid()) socket!!.emit(SocketConstant.FETCH_DETAIL_CONVERSATION, conversationId)
    }

    fun authorize(apiKey: String) {
        socket!!.emit(SocketConstant.AUTHENTICATION, apiKey)
    }

    fun initialize(context: Context) {
        val isNotificationGranted = NotificationManagerCompat.from(context).areNotificationsEnabled()
        if (!isNotificationGranted) {
            LCLog.logE("LiveChatSDK require post notification permission to use!")
            return
        }
        this.context = context
        PrefUtil.init(context)
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
            isAvailable = success
            if (!success) {
                //Observe failed
                observingAuthorize(false, "Un-authorized",null)
                return@on
            }
            val dataResp = jsonObject.getJSONObject("data")
            SocketConstant.CLIENT_URL_SOCKET = dataResp.getString("domain_socket")
            val supportTypesRaw = dataResp.getJSONArray("support_type")
            var supportTypes: ArrayList<LCSupportType> = ArrayList()
            for(i in 0..<supportTypesRaw.length()){
                var jsonObject: JSONObject = supportTypesRaw.getJSONObject(i)
                supportTypes.add(
                    LCSupportType(
                        jsonObject.getString("id"),
                        jsonObject.getString("name")
                    )
                )
            }
            currLCAccount = LCAccount(
                dataResp.getInt("id"),
                dataResp.getInt("groupid"),
                dataResp.getString("group_name"),
                dataResp.getString("domain_socket"),
                dataResp.getString("for_domain"),
                supportTypes
            )
            try {
                socketClient = IO.socket(SocketConstant.CLIENT_URL_SOCKET)
                socketClient!!.on(SocketConstant.RESULT_FETCH_DETAIL_CONVERSATION) { data ->
                    LCLog.logI("RESULT_FETCH_DETAIL_CONVERSATION: ${data[0]}")

                }
                socketClient!!.on(SocketConstant.RESULT_SEND_MESSAGE) { data ->
                    LCLog.logI("RESULT_SEND_MESSAGE: ${data[0]}")
                    val jsonObject = data[0] as JSONObject
                    val success = jsonObject.getBoolean("status")
                    val messageRaw = jsonObject.getJSONObject("data")
                    val lcMessage = LCMessage(
                        messageRaw.getInt("id"),
                        messageRaw.getString("content"),
                        messageRaw.getString("created_at")
                    )

                    observingSendMessage(if (success) LCSendMessageEnum.SENT_SUCCESS else LCSendMessageEnum.SENT_FAILED,if (success) lcMessage else null)
                }
                socketClient!!.on(SocketConstant.RESULT_INITIALIZE_SESSION) { data ->
                    val jsonObject = data[0] as JSONObject
                    val success: Boolean = jsonObject.getBoolean("status")
                    val sessionId: String = jsonObject.getString("session_id")
                    PrefUtil.setString("currSessionId",sessionId)
                    observingInitialSession(success,sessionId)
                }
                socketClient!!.connect()
                observingAuthorize(true, "Authorization successful", currLCAccount)
            } catch (ignored: URISyntaxException) {
            }
        }
        listeners = ArrayList()
        isInitialized = true
    }
}
