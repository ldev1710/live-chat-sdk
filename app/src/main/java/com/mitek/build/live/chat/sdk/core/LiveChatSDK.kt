package com.mitek.build.live.chat.sdk.core

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.mitek.build.live.chat.sdk.BuildConfig
import com.mitek.build.live.chat.sdk.core.model.LCAccount
import com.mitek.build.live.chat.sdk.core.model.LCSupportType
import com.mitek.build.live.chat.sdk.listener.publisher.LiveChatListener
import com.mitek.build.live.chat.sdk.model.chat.LCMessage
import com.mitek.build.live.chat.sdk.model.chat.LCMessageSend
import com.mitek.build.live.chat.sdk.model.chat.LCSendMessageEnum
import com.mitek.build.live.chat.sdk.model.chat.LCSender
import com.mitek.build.live.chat.sdk.model.user.LCSession
import com.mitek.build.live.chat.sdk.model.user.LCUser
import com.mitek.build.live.chat.sdk.util.LCLog
import com.mitek.build.live.chat.sdk.util.SocketConstant
import io.github.cdimascio.dotenv.dotenv
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
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

    fun sendFileMessage(paths: ArrayList<String>, lcUser:LCUser, lcSession: LCSession){
        if(isValid()){
            val files = ArrayList<String>()
            paths.forEach {
                val file = File(it)
                if (file.exists() && file.length() > 0) {
                    val bytes = file.readBytes()
                    val base64File: String = base64(bytes.toString())
                    files.add(base64File)
                }
            }
            val jsonObject = JSONObject()
            jsonObject.put(base64("body"),files)
            jsonObject.put(base64("add_message_archive"),"")
            jsonObject.put(base64("groupid"), currLCAccount!!.groupId)
            jsonObject.put(base64("reply"), 0)
            jsonObject.put(base64("type"), "live-chat-sdk")
            jsonObject.put(base64("from"), lcSession.visitorJid)
            jsonObject.put(base64("name"), lcUser.fullName)
            jsonObject.put(base64("session_id"), lcSession.sessionId)
            jsonObject.put(base64("host_name"), currLCAccount!!.hostName)
            jsonObject.put(base64("visitor_jid"), lcSession.visitorJid)
            jsonObject.put(base64("is_file"), 1)
            LCLog.logI("Send message with: $jsonObject")
            socketClient!!.emit(SocketConstant.SEND_MESSAGE, jsonObject)
            observingSendMessage(LCSendMessageEnum.SENDING,null)
        }
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
    fun observingInitialSession(success: Boolean, lcSession: LCSession) {
        if (listeners == null) return
        for (listener in listeners!!) {
            listener.onInitialSessionStateChanged(success, lcSession)
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
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                val body = JSONObject()
                body.put(base64("groupid"),currLCAccount!!.groupId.toString())
                body.put(base64("host_name"),currLCAccount!!.hostName)
                body.put(base64("visitor_name"),user.fullName)
                body.put(base64("visitor_email"),user.email)
                body.put(base64("visitor_phone"),user.phone)
                body.put(base64("url_visit"),user.deviceName)
                body.put(base64("token"), token)
                LCLog.logI("Init session with: $body")
                socketClient!!.emit(SocketConstant.INITIALIZE_SESSION, body)
            })
        }
    }

    fun addEventListener(listener: LiveChatListener) {
        if (listeners == null) listeners = ArrayList()
        listeners!!.add(listener)
    }

    fun sendMessage(lcUser: LCUser, lcMessage: LCMessageSend) {
        if (isValid()) {
            val jsonObject = JSONObject()
            jsonObject.put(base64("body"),lcMessage.content)
            jsonObject.put(base64("add_message_archive"),"")
            jsonObject.put(base64("groupid"), currLCAccount!!.groupId)
            jsonObject.put(base64("reply"), 0)
            jsonObject.put(base64("type"), "live-chat-sdk")
            jsonObject.put(base64("from"), lcMessage.lcSession.visitorJid)
            jsonObject.put(base64("name"), lcUser.fullName)
            jsonObject.put(base64("session_id"), lcMessage.lcSession.sessionId)
            jsonObject.put(base64("host_name"), currLCAccount!!.hostName)
            jsonObject.put(base64("visitor_jid"), lcMessage.lcSession.visitorJid)
            jsonObject.put(base64("is_file"), 0)
            LCLog.logI("Send message with: $jsonObject")
            socketClient!!.emit(SocketConstant.SEND_MESSAGE, jsonObject)
            observingSendMessage(LCSendMessageEnum.SENDING,null)
        }
    }

    fun getMessages(sessionId: String) {
        if (isValid()) {
            var jsonObject = JSONObject()
            jsonObject.put(base64("host_name"), currLCAccount!!.hostName)
            jsonObject.put(base64("session_id"), sessionId)
            jsonObject.put(base64("groupid"), currLCAccount!!.groupId)
            socket!!.emit(SocketConstant.GET_MESSAGES,jsonObject)
        }
    }

    fun authorize(apiKey: String) {
        if (isInitialized) {
            socket!!.emit(SocketConstant.AUTHENTICATION, apiKey)
        }
    }

    fun initialize(context: Context) {
        val isNotificationGranted = NotificationManagerCompat.from(context).areNotificationsEnabled()
        if (!isNotificationGranted) {
            LCLog.logE("LiveChatSDK require post notification permission to use!")
            return
        }
        this.context = context
        try {
            socket = IO.socket(BuildConfig.BASE_URL_SOCKET)
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
                socket!!.on(SocketConstant.RECEIVE_MESSAGE) { data ->
                    LCLog.logI("RECEIVE_MESSAGE: ${data[0]}")
//                    val jsonObject = data[0] as JSONObject
//                    val messageRaw = jsonObject.getJSONObject("data")
//                    val fromRaw = messageRaw.getJSONObject("from")
//                    val lcMessage = LCMessage(
//                        messageRaw.getInt("id"),
//                        messageRaw.getString("content"),
//                        LCSender(fromRaw.getString("id"),fromRaw.getString("name")),
//                        messageRaw.getString("created_at"),
//                    )
//                    if(lcMessage.from.id != PrefUtil.getString("currVisitorJid")) observingMessage(lcMessage)
                }
                socketClient!!.on(SocketConstant.CONFIRM_SEND_MESSAGE) { data ->
                    LCLog.logI("CONFIRM_SEND_MESSAGE: ${data[0]}")
                    val jsonObject = data[0] as JSONObject
                    val success = jsonObject.getBoolean("status")
                    val messageRaw = jsonObject.getJSONObject("data")
                    val fromRaw = messageRaw.getJSONObject("from")
                    val lcMessage = LCMessage(
                        messageRaw.getInt("id"),
                        messageRaw.getString("content"),
                        LCSender(fromRaw.getString("id"),fromRaw.getString("name")),
                        messageRaw.getString("created_at"),
                    )

                    observingSendMessage(if (success) LCSendMessageEnum.SENT_SUCCESS else LCSendMessageEnum.SENT_FAILED,if (success) lcMessage else null)
                }
                socketClient!!.on(SocketConstant.RESULT_INITIALIZE_SESSION) { data ->
                    val jsonObject = data[0] as JSONObject
                    LCLog.logI(jsonObject.toString())
                    val success: Boolean = jsonObject.getBoolean("status")
                    val sessionId: String = jsonObject.getString("session_id")
                    val visitorJid: String = jsonObject.getString("visitor_jid")
                    observingInitialSession(success, LCSession(sessionId,visitorJid))
                }
                socketClient!!.connect()
                observingAuthorize(true, "Authorization successful", currLCAccount)
            } catch (ignored: URISyntaxException) {
            }
        }
        socket!!.on(SocketConstant.RESULT_GET_MESSAGES) {
                data ->
            val jsonObject = data[0] as JSONObject
            LCLog.logI(jsonObject.toString())
            val messagesRaw = jsonObject.getJSONArray("data")
            val messages = ArrayList<LCMessage>()
            for (i in 0..<messagesRaw.length()){
                val jsonMessage = messagesRaw.getJSONObject(i)
                val jsonSender = jsonMessage.getJSONObject("from")
                messages.add(
                    LCMessage(
                        jsonMessage.getInt("id"),
                        jsonMessage.getString("content"),
                        LCSender(
                            jsonSender.getString("id"),
                            jsonSender.getString("name")
                        ),
                        jsonMessage.getString("created_at"),
                    )
                )
            }

        }
        listeners = ArrayList()
        isInitialized = true
    }
}
