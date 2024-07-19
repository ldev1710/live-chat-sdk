package com.mitek.build.live.chat.sdk.core

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.mitek.build.live.chat.sdk.BuildConfig
import com.mitek.build.live.chat.sdk.core.model.InitialEnum
import com.mitek.build.live.chat.sdk.core.model.LCAccount
import com.mitek.build.live.chat.sdk.core.model.LCSupportType
import com.mitek.build.live.chat.sdk.core.model.ResponseUploadFile
import com.mitek.build.live.chat.sdk.core.network.ApiClient
import com.mitek.build.live.chat.sdk.listener.publisher.LiveChatListener
import com.mitek.build.live.chat.sdk.model.chat.LCMessage
import com.mitek.build.live.chat.sdk.model.chat.LCMessageSend
import com.mitek.build.live.chat.sdk.model.chat.LCSendMessageEnum
import com.mitek.build.live.chat.sdk.model.chat.LCSender
import com.mitek.build.live.chat.sdk.model.user.LCSession
import com.mitek.build.live.chat.sdk.model.user.LCUser
import com.mitek.build.live.chat.sdk.util.LCLog
import com.mitek.build.live.chat.sdk.util.LCParseUtils
import com.mitek.build.live.chat.sdk.util.SocketConstant
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


@SuppressLint("StaticFieldLeak")
object LiveChatSDK {
    private var isInitialized = false
    private var isAvailable = false
    private var listeners: ArrayList<LiveChatListener>? = null
    private var context: Context? = null
    private var socket: Socket? = null
    private var socketClient: Socket? = null
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
            if(paths.size > 3){
                LCLog.logE("You are only allowed to send a maximum of 3 files")
                return
            }
            val multipartBodyFile = ArrayList<MultipartBody.Part>()
            paths.forEach {
                val file = File(it)
                if (file.exists() && file.length() > 0) {
                    val requestBodyFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                    val fileName = file.name.replace(" ", "-")
                    multipartBodyFile.add(MultipartBody.Part.createFormData("body", fileName, requestBodyFile))
                }
            }
            val call = ApiClient.apiService.uploadFile(
                multipartBodyFile,
                "",
                currLCAccount!!.groupId,
                0,
                "live-chat-sdk",
                lcSession.visitorJid,
                lcUser.fullName,
                lcSession.sessionId,
                currLCAccount!!.hostName,
                lcSession.visitorJid,
                1,
            )

            call.enqueue(object : Callback<ResponseUploadFile> {
                override fun onResponse(call: Call<ResponseUploadFile>, response: Response<ResponseUploadFile>) {
                    LCLog.logI("Response upload file: ${response.body()}")
                    response.body()!!.data.content!!.contentMessage = LCParseUtils.parseLCContentFrom(JSONObject(response.body()!!.data.content!!.contentMessage.toString().replace("/","\\/")))
                    observingSendMessage(LCSendMessageEnum.SENT_SUCCESS,response.body()!!.data,null)
                }
                override fun onFailure(call: Call<ResponseUploadFile>, t: Throwable) {
                    observingSendMessage(LCSendMessageEnum.SENT_FAILED,null, t.message)
                }
            })
            observingSendMessage(LCSendMessageEnum.SENDING,null,null)
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
    fun observingGotMessages(messages: ArrayList<LCMessage>) {
        if (listeners == null) return
        for (listener in listeners!!) {
            listener.onGotDetailConversation(messages)
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
    fun observingSendMessage(state: LCSendMessageEnum, message: LCMessage?,errorMessage: String?) {
        if (listeners == null) return
        for (listener in listeners!!) {
            listener.onSendMessageStateChange(state, message, errorMessage)
        }
    }

    @JvmStatic
    fun observingInitSDK(state: InitialEnum,message: String) {
        if (listeners == null) return
        for (listener in listeners!!) {
            listener.onInitSDKStateChanged(state, message)
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
//                LCLog.logI("Init session with: $body")
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
//            LCLog.logI("Send message with: $jsonObject")
            socketClient!!.emit(SocketConstant.SEND_MESSAGE, jsonObject)
            observingSendMessage(LCSendMessageEnum.SENDING,null,null)
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
//            LCLog.logI("AUTHENTICATION with: $apiKey")
            socket!!.emit(SocketConstant.AUTHENTICATION, apiKey)
        }
    }

    fun initialize(context: Context) {
        val isNotificationGranted = NotificationManagerCompat.from(context).areNotificationsEnabled()
        if (!isNotificationGranted) {
            observingInitSDK(InitialEnum.FAILED,"LiveChatSDK require post notification permission to use!")
            return
        }
        observingInitSDK(InitialEnum.PROCESSING,"LiveChatSDK initial is processing!")
        this.context = context
        try {
            socket = IO.socket(BuildConfig.BASE_URL_SOCKET)
            socket!!.on(SocketConstant.CONFIRM_CONNECT) { data ->
//                LCLog.logI("CONFIRM_CONNECT: sv")
                isInitialized = true
                observingInitSDK(InitialEnum.SUCCESS,"Initial SDK successful!")
            }
            socket!!.on(SocketConstant.RESULT_AUTHENTICATION) { data ->
                val jsonObject = data[0] as JSONObject
//                LCLog.logI("RESULT_AUTHENTICATION: $jsonObject")
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
                val supportTypes: ArrayList<LCSupportType> = ArrayList()
                for(i in 0..<supportTypesRaw.length()){
                    val jsonObject: JSONObject = supportTypesRaw.getJSONObject(i)
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
                    socketClient!!.on(SocketConstant.CONFIRM_CONNECT) { data ->
                        LCLog.logI("CONFIRM_CONNECT CLIENT: "+currLCAccount!!.groupId)
                        socketClient!!.emit(SocketConstant.JOIN_CLIENT,currLCAccount!!.groupId)
                        observingAuthorize(true, "Authorization successful", currLCAccount)
                    }
                    socketClient!!.on(SocketConstant.RECEIVE_MESSAGE) { data ->
//                        LCLog.logI("RECEIVE_MESSAGE: ${data[0]}")
//                        val jsonObject = data[0] as JSONObject
//                        val messageRaw = jsonObject.getJSONObject("data")
//                        val fromRaw = messageRaw.getJSONObject("from")
//                        val lcMessage = LCMessage(
//                            messageRaw.getInt("id"),
//                            messageRaw.getString("content"),
//                            LCSender(fromRaw.getString("id"),fromRaw.getString("name")),
//                            messageRaw.getString("created_at"),
//                        )
//                    if(lcMessage.from.id != PrefUtil.getString("currVisitorJid")) observingMessage(lcMessage)
                    }
                    socketClient!!.on(SocketConstant.CONFIRM_SEND_MESSAGE) { data ->
                        LCLog.logI("CONFIRM_SEND_MESSAGE: ${data[0]}")
                        val jsonObject = data[0] as JSONObject
                        val success = jsonObject.getBoolean("status")
                        val messageRaw = jsonObject.getJSONObject("data")
                        val fromRaw = messageRaw.getJSONObject("from")
                        val rawContent = messageRaw.getJSONObject("content")
                        val lcMessage = LCMessage(
                            messageRaw.getInt("id"),
                            LCParseUtils.parseLCContentFrom(rawContent),
                            LCSender(fromRaw.getString("id"),fromRaw.getString("name")),
                            messageRaw.getString("created_at"),
                        )
                        observingSendMessage(
                            if (success) LCSendMessageEnum.SENT_SUCCESS else LCSendMessageEnum.SENT_FAILED,
                            if (success) lcMessage else null,
                            if (success) null else "Send failed",
                        )
                    }
                    socketClient!!.on(SocketConstant.RESULT_INITIALIZE_SESSION) { data ->
                        val jsonObject = data[0] as JSONObject
                        val success: Boolean = jsonObject.getBoolean("status")
                        val sessionId: String = jsonObject.getString("session_id")
                        val visitorJid: String = jsonObject.getString("visitor_jid")
                        observingInitialSession(success, LCSession(sessionId,visitorJid))
                    }
                    socketClient!!.connect()
                } catch (e: Exception) {
                    LCLog.logE(e.toString())
                }
            }
            socket!!.on(SocketConstant.RESULT_GET_MESSAGES) {
                    data ->
                val jsonObject = data[0] as JSONObject
                val messagesRaw = jsonObject.getJSONArray("data")
                val messages = ArrayList<LCMessage>()
                for (i in 0..<messagesRaw.length()){
                    val jsonMessage = messagesRaw.getJSONObject(i)
                    val jsonSender = jsonMessage.getJSONObject("from")
                    val rawContent = jsonMessage.getJSONObject("content")
                    messages.add(
                        LCMessage(
                            jsonMessage.getInt("id"),
                            LCParseUtils.parseLCContentFrom(rawContent),
                            LCSender(
                                jsonSender.getString("id"),
                                jsonSender.getString("name")
                            ),
                            jsonMessage.getString("created_at"),
                        )
                    )
                }
                observingGotMessages(messages)
            }
            socket!!.connect()
        } catch (e: Exception) {
            LCLog.logE(e.toString())
        }

    }
}
