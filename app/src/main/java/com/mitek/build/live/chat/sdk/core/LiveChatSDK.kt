package com.mitek.build.live.chat.sdk.core

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import com.google.gson.internal.LinkedTreeMap
import com.mitek.build.live.chat.sdk.BuildConfig
import com.mitek.build.live.chat.sdk.model.internal.InitialEnum
import com.mitek.build.live.chat.sdk.model.internal.LCAccount
import com.mitek.build.live.chat.sdk.model.internal.LCSupportType
import com.mitek.build.live.chat.sdk.model.internal.ResponseUploadFile
import com.mitek.build.live.chat.sdk.core.network.ApiClient
import com.mitek.build.live.chat.sdk.core.network.LiveChatAPI
import com.mitek.build.live.chat.sdk.listener.publisher.LiveChatListener
import com.mitek.build.live.chat.sdk.model.attachment.LCAttachment
import com.mitek.build.live.chat.sdk.model.chat.LCContent
import com.mitek.build.live.chat.sdk.model.chat.LCMessage
import com.mitek.build.live.chat.sdk.model.chat.LCMessageSend
import com.mitek.build.live.chat.sdk.model.chat.LCSendMessageEnum
import com.mitek.build.live.chat.sdk.model.chat.LCSender
import com.mitek.build.live.chat.sdk.model.internal.LCButtonAction
import com.mitek.build.live.chat.sdk.model.internal.LCMessageReceiveSource
import com.mitek.build.live.chat.sdk.model.internal.LCScript
import com.mitek.build.live.chat.sdk.model.user.LCSession
import com.mitek.build.live.chat.sdk.model.user.LCUser
import com.mitek.build.live.chat.sdk.util.LCLog
import com.mitek.build.live.chat.sdk.util.LCParseUtils
import com.mitek.build.live.chat.sdk.util.PrefUtil
import com.mitek.build.live.chat.sdk.util.SocketConstant
import com.mitek.build.live.chat.sdk.view.LCChatActivity
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


@SuppressLint("StaticFieldLeak")
object LiveChatSDK {
    private var isInitialized = false
    private var isAvailable = false
    private var listeners: ArrayList<LiveChatListener>? = null
    private lateinit var lcScripts: ArrayList<LCScript>
    private var context: Context? = null
    private var socket: Socket? = null
    private var socketClient: Socket? = null
    private var currLCAccount: LCAccount? = null
    private lateinit var accessToken: String
    private var isDebugging = false
    private var lcSession: LCSession? = null
    private var lcUser: LCUser? = null

    fun getLCSession(): LCSession {
        return lcSession!!
    }

    private fun isReady(): Boolean {
        if (!(isInitialized && isAvailable)) {
            LCLog.logE("LiveChatSDK is not ready")
            return false
        }
        return true
    }

    private fun isValid(): Boolean {
        if(!isReady()){
            return false
        }
        if(lcUser == null || lcSession == null){
            LCLog.logE("User session not has been set yet. Please call LiveChatFactory.setUserSession")
            return false
        }
        return true
    }

    fun setUserSession(lcSession: LCSession, lcUser: LCUser){
        if(isReady()){
            this.lcUser = lcUser
            this.lcSession = lcSession
            socketClient!!.emit(SocketConstant.JOIN_SESSION,lcSession.sessionId)
        }
    }

    fun sendFileMessage(paths: ArrayList<String>,contentTye: String){
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
            val mappingId = UUID.randomUUID().toString()
            val call = ApiClient.apiService.uploadFile(
                "Bearer $accessToken",
                multipartBodyFile,
                mappingId.toRequestBody(MultipartBody.FORM),
                "",
                currLCAccount!!.groupId,
                0,
                "live-chat-sdk".toRequestBody(MultipartBody.FORM),
                lcSession!!.visitorJid.toRequestBody(MultipartBody.FORM),
                lcUser!!.fullName.toRequestBody(MultipartBody.FORM),
                lcSession!!.sessionId.toRequestBody(MultipartBody.FORM),
                currLCAccount!!.hostName.toRequestBody(MultipartBody.FORM),
                lcSession!!.visitorJid.toRequestBody(MultipartBody.FORM),
                1,
            )

            call.enqueue(object : Callback<ResponseUploadFile> {
                override fun onResponse(call: Call<ResponseUploadFile>, response: Response<ResponseUploadFile>) {
                    if(response.body()!!.error){
                        observingSendMessage(
                            LCSendMessageEnum.SENT_FAILED,
                            null,
                            response.body()!!.message,
                            response.body()!!.data.mappingId
                        )
                        return
                    }
                    val rawAttachments = response.body()!!.data.content!!.contentMessage as ArrayList<LinkedTreeMap<String,String>>
                    val attachments: ArrayList<LCAttachment> = ArrayList()
                    rawAttachments.forEach{
                        val fileName = it["file-name"]
                        val extension = fileName!!.split(".").last()
                        val url = it["url"]
                        attachments.add(
                            LCAttachment(fileName,extension,url!!)
                        )
                    }
                    response.body()!!.data.content!!.contentMessage = attachments
                    observingSendMessage(LCSendMessageEnum.SENT_SUCCESS,response.body()!!.data,null,mappingId)
                }
                override fun onFailure(call: Call<ResponseUploadFile>, t: Throwable) {
                    observingSendMessage(LCSendMessageEnum.SENT_FAILED,null, t.message,mappingId)
                }
            })
            val currentTime = Date()
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val formattedTime = formatter.format(currentTime)
            val lcAttachments  = ArrayList<LCAttachment>()
            paths.map { it ->
                val fileName = it.split('/').last()
                val extension = fileName.split('.').last()
                lcAttachments.add(LCAttachment(fileName,extension,""))
            }
            val message = LCMessage(
                -1,
                mappingId,
                LCContent(contentTye,lcAttachments),
                LCSender(lcSession!!.visitorJid, lcUser!!.fullName),
                formattedTime,
            )
            observingSendMessage(LCSendMessageEnum.SENDING,message,null,message.mappingId)
        }
    }

    fun removeEventListener(listener: LiveChatListener){
        if(listeners == null) return
        listeners!!.remove(listener)
    }

    @JvmStatic
    fun observingMessage(lcMessage: LCMessage) {
        if (listeners == null) return
        if(lcMessage.from!!.id == lcSession!!.visitorJid){
            return
        }
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
    fun observingSendMessage(state: LCSendMessageEnum, message: LCMessage?,errorMessage: String?,mappingId: String?) {
        if (listeners == null) return
        for (listener in listeners!!) {
            listener.onSendMessageStateChange(state, message, errorMessage, mappingId)
        }
    }

    @JvmStatic
    fun observingInitSDK(state: InitialEnum, message: String) {
        if (listeners == null) return
        for (listener in listeners!!) {
            listener.onInitSDKStateChanged(state, message)
        }
    }


    @OptIn(ExperimentalEncodingApi::class)
    private fun base64(rawString: String): String {
        return Base64.encode(rawString.toByteArray())
    }

    fun getScripts() : ArrayList<LCScript> {
        assert(isReady())
        return lcScripts
    }

    fun initializeSession(user: LCUser,tokenFCM: String, supportType: LCSupportType) {
        if (isInitialized && isAvailable) {
            try {
                val body = JSONObject()
                body.put(base64("groupid"),currLCAccount!!.groupId.toString())
                body.put(base64("host_name"),currLCAccount!!.hostName)
                body.put(base64("visitor_name"),user.fullName)
                body.put(base64("visitor_email"),user.email)
                body.put(base64("type"),"live-chat-sdk")
                body.put(base64("visitor_phone"),user.phone)
                body.put(base64("url_visit"),user.deviceName)
                body.put(base64("token"), tokenFCM)
                body.put(base64("access_token"), accessToken)
                body.put(base64("support_type_id"), supportType.id)
                socketClient!!.emit(SocketConstant.INITIALIZE_SESSION, body)
            } catch (e: Exception){
                LCLog.logE("Please enable and initialize Firebase in your app!")
            }
        } else {
            LCLog.logE("LiveChatSDK is not ready")
        }
    }

    fun addEventListener(listener: LiveChatListener) {
        if (listeners == null) listeners = ArrayList()
        listeners!!.add(listener)
    }

    fun sendMessage(lcMessage: LCMessageSend,nextScriptId: String?) {
        if (isValid()) {
            val jsonObject = JSONObject()
            val mappingId = UUID.randomUUID().toString()
            jsonObject.put(base64("body"),lcMessage.content)
            jsonObject.put(base64("id_next"),nextScriptId)
            jsonObject.put(base64("mapping_id"),mappingId)
            jsonObject.put(base64("add_message_archive"),"")
            jsonObject.put(base64("groupid"), currLCAccount!!.groupId)
            jsonObject.put(base64("reply"), 0)
            jsonObject.put(base64("type"), "live-chat-sdk")
            jsonObject.put(base64("access_token"), accessToken)
            jsonObject.put(base64("from"), lcSession!!.visitorJid)
            jsonObject.put(base64("name"), lcUser!!.fullName)
            jsonObject.put(base64("session_id"), lcSession!!.sessionId)
            jsonObject.put(base64("host_name"), currLCAccount!!.hostName)
            jsonObject.put(base64("visitor_jid"), lcSession!!.visitorJid)
            jsonObject.put(base64("is_file"), 0)
            socketClient!!.emit(SocketConstant.SEND_MESSAGE, jsonObject)
            val currentTime = Date()
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val formattedTime = formatter.format(currentTime)
            val message = LCMessage(
                -1,
                mappingId,
                LCContent("text",lcMessage.content),
                LCSender(lcSession!!.visitorJid, lcUser!!.fullName),
                formattedTime,
            )
            observingSendMessage(LCSendMessageEnum.SENDING,message,null,message.mappingId)
        }
    }

    fun getMessages(offset:Int,limit:Int) {
        if (isValid()) {
            val jsonObject = JSONObject()
            jsonObject.put(base64("host_name"), currLCAccount!!.hostName)
            jsonObject.put(base64("session_id"), lcSession!!.sessionId)
            jsonObject.put(base64("groupid"), currLCAccount!!.groupId)
            jsonObject.put(base64("offset"), offset)
            jsonObject.put(base64("access_token"), accessToken)
            jsonObject.put(base64("limit"), limit)
            socket!!.emit(SocketConstant.GET_MESSAGES,jsonObject)
        }
    }

    fun authorize(apiKey: String) {
        if (isInitialized) {
            socket!!.emit(SocketConstant.AUTHENTICATION, apiKey)
        }
    }

    fun initialize(context: Context) {
        observingInitSDK(InitialEnum.PROCESSING,"LiveChatSDK initial is processing!")
        this.context = context
        PrefUtil.init(this.context!!)
        try {
            socket = IO.socket(BuildConfig.BASE_URL_SOCKET)
            socket!!.on(SocketConstant.CONFIRM_CONNECT) { data ->
                isInitialized = true
                observingInitSDK(InitialEnum.SUCCESS,"Initial SDK successful!")
            }
            socket!!.on(SocketConstant.RESULT_AUTHENTICATION) { data ->
                val jsonObject = data[0] as JSONObject
                val success = jsonObject.getBoolean("status")
                isAvailable = success
                if (!success) {
                    observingAuthorize(false, "Un-authorized",null)
                    return@on
                }
                val dataResp = jsonObject.getJSONObject("data")
                val rawScripts = dataResp.getJSONArray("script")
                lcScripts = ArrayList()
                for (i in 0..< rawScripts.length()){
                    val rawScript = rawScripts.getJSONObject(i)
                    val rawButtonActions = rawScript.optJSONArray("button_action") ?: continue
                    val buttonActions = ArrayList<LCButtonAction>()
                    for (j in 0 ..< rawButtonActions.length()){
                        val rawButtonAction = rawButtonActions.getJSONObject(j)
                        buttonActions.add(LCButtonAction(
                            rawButtonAction.getString("button"),
                            rawButtonAction.getString("next"),
                        ))
                    }
                    lcScripts.add(LCScript(
                        rawScript.getString("id"),
                        rawScript.getString("name"),
                        rawScript.getString("next_action"),
                        buttonActions,
                    ))
                }
                SocketConstant.CLIENT_URL_SOCKET = dataResp.getString("domain_socket")
                LCLog.logI("RES-AUTH: ${SocketConstant.CLIENT_URL_SOCKET}")
                LiveChatAPI.setUrl(SocketConstant.CLIENT_URL_SOCKET)
                accessToken = dataResp.getString("access_token")
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
                        val jsonObject = data[0] as JSONObject
                        val messageRaw = jsonObject.getJSONObject("data")
                        val fromRaw = messageRaw.getJSONObject("sender")
                        val lcMessage = LCMessage(
                            messageRaw.getInt("id"),
                            null,
                            LCParseUtils.parseLCContentFrom(messageRaw.getJSONObject("content")),
                            LCSender(fromRaw.getString("id"),fromRaw.getString("name")),
                            messageRaw.getString("created_at"),
                        )
                        if(lcMessage.from?.id == lcSession?.visitorJid) return@on
                        LCLog.logI("RECEIVE_MESSAGE: ${data[0]}")
                        observingMessage(lcMessage)
                    }
                    socketClient!!.on(SocketConstant.CONFIRM_SEND_MESSAGE) { data ->
                        LCLog.logI("CONFIRM_SEND_MESSAGE: ${data[0]}")
                        val jsonObject = data[0] as JSONObject
                        val success = jsonObject.getBoolean("status")
                        val messageRaw = jsonObject.getJSONObject("data")
                        val mappingId = messageRaw.getString("mapping_id")
                        val fromRaw = messageRaw.getJSONObject("from")
                        val rawContent = messageRaw.getJSONObject("content")
                        val lcMessage = LCMessage(
                            messageRaw.getInt("id"),
                            mappingId,
                            LCParseUtils.parseLCContentFrom(rawContent),
                            LCSender(fromRaw.getString("id"),fromRaw.getString("name")),
                            messageRaw.getString("created_at"),
                        )
                        observingSendMessage(
                            if (success) LCSendMessageEnum.SENT_SUCCESS else LCSendMessageEnum.SENT_FAILED,
                            if (success) lcMessage else null,
                            if (success) null else "Send failed",
                            lcMessage.mappingId,
                        )
                    }
                    socketClient!!.on(SocketConstant.RESULT_INITIALIZE_SESSION) { data ->
                        LCLog.logI("RESULT_INITIALIZE_SESSION: ${data[0]}")
                        val jsonObject = data[0] as JSONObject
                        val success: Boolean = jsonObject.getBoolean("status")
                        val jsonData: JSONObject = jsonObject.getJSONObject("data")
                        val sessionId: String = jsonData.getString("session_id")
                        val visitorJid: String = jsonData.getString("visitor_jid")
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
                            null,
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

    fun enableDebug(enable: Boolean) {
        isDebugging = enable
    }

    fun isDebugging() : Boolean{
        return isDebugging
    }

    fun openChatView(from: Context) {
        if(isValid()){
            val intent = Intent(from, LCChatActivity::class.java)
            from.startActivity(intent)
        }
    }
}
