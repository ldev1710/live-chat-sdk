package com.mitek.build.live.chat.sdk.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mitek.build.live.chat.sdk.R
import com.mitek.build.live.chat.sdk.core.LiveChatFactory
import com.mitek.build.live.chat.sdk.core.LiveChatSDK
import com.mitek.build.live.chat.sdk.listener.publisher.LiveChatListener
import com.mitek.build.live.chat.sdk.model.chat.LCMessage
import com.mitek.build.live.chat.sdk.model.chat.LCMessageEntity
import com.mitek.build.live.chat.sdk.model.chat.LCMessageSend
import com.mitek.build.live.chat.sdk.model.chat.LCSendMessageEnum
import com.mitek.build.live.chat.sdk.model.chat.LCStatusMessage
import com.mitek.build.live.chat.sdk.model.internal.LCButtonAction
import com.mitek.build.live.chat.sdk.util.LCLog.logI
import com.mitek.build.live.chat.sdk.util.RealPathUtil
import com.mitek.build.live.chat.sdk.view.adapter.MessageAdapter

class LCChatActivity : AppCompatActivity() {

    private lateinit var rvChat: RecyclerView
    private lateinit var adapter: MessageAdapter
    private lateinit var messagesGlo: ArrayList<LCMessageEntity?>
    private lateinit var edtMessage: EditText
    private lateinit var btnSend: ImageView
    private lateinit var btnAttach: ImageView
    private lateinit var btnBack: ImageView
    private var page :Int = 1
    private var limit = 10
    private var isInit = true
    private var isCanLoadMore = true
    private var isFetchingMore = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        initView()
        LiveChatFactory.addEventListener(object : LiveChatListener() {
            @SuppressLint("NotifyDataSetChanged")
            override fun onGotDetailConversation(messages: ArrayList<LCMessage>) {
                super.onGotDetailConversation(messages)
                logI("onGotDetailConversation: $messages")
                isCanLoadMore = messages.isNotEmpty() && messages.size % limit == 0
                runOnUiThread {
                    if(isInit){
                        messages.reversed().map {
                            messagesGlo.add(LCMessageEntity(lcMessage = it, LCStatusMessage.sent))
                        }
                        adapter = MessageAdapter(
                            this@LCChatActivity,
                            messagesGlo,
                            LiveChatSDK.getLCSession(),
                            LiveChatFactory.getScripts(),
                        )
                        rvChat.adapter = adapter
                        rvChat.smoothScrollToPosition(adapter.itemCount)
                        rvChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                                super.onScrolled(recyclerView, dx, dy)
                                if(!isCanLoadMore) return
                                if(isInit){
                                    isInit = false
                                    return
                                }
                                if(isFetchingMore){
                                    isFetchingMore = false
                                    return
                                }
                                val layoutManager = recyclerView.layoutManager
                                if (layoutManager is LinearLayoutManager) {
                                    if (layoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                                        page++
                                        LiveChatFactory.getMessages(offset = page * limit,limit)
                                        isFetchingMore = true
                                        messagesGlo.add(0,null)
                                        adapter.notifyDataSetChanged()
                                    }
                                }
                            }
                        })
                    } else {
                        runOnUiThread {
                            messagesGlo.removeAt(0)
                            val tmp = ArrayList<LCMessageEntity>()
                            messages.reversed().map {
                                tmp.add(LCMessageEntity(lcMessage = it, LCStatusMessage.sent))
                            }
                            messagesGlo.addAll(0,tmp)
                            adapter.notifyItemRangeChanged(0,messages.size)
                        }
                    }
                }
            }

            override fun onRestartScripting(buttonActions: ArrayList<LCButtonAction>) {
                super.onRestartScripting(buttonActions)
                runOnUiThread {
                    adapter.restartScripting(buttonActions)
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onSendMessageStateChange(state: LCSendMessageEnum, message: LCMessage?, errorMessage: String?,mappingId: String?) {
                super.onSendMessageStateChange(state, message, errorMessage,mappingId)
                logI("onSendMessageStateChange: $state | $message | $mappingId")
                when (state) {
                    LCSendMessageEnum.SENT_SUCCESS -> {
                        val indexFound = messagesGlo.indexOfFirst { it != null && it.lcMessage.mappingId == message!!.mappingId }
                        if(indexFound==-1) return
                        messagesGlo[indexFound]!!.lcMessage = message!!
                        messagesGlo[indexFound]!!.status = LCStatusMessage.sent
                        runOnUiThread {
                            adapter.notifyItemRangeChanged(indexFound,1)
                        }
                    }
                    LCSendMessageEnum.SENDING -> {
                        messagesGlo.add(
                            LCMessageEntity(lcMessage = message!!,
                                LCStatusMessage.sending)
                        )
                        runOnUiThread {
                            adapter.notifyDataSetChanged()
                            rvChat.smoothScrollToPosition(adapter.itemCount)
                        }
                    }
                    else -> {
                        val indexFound = messagesGlo.indexOfFirst { it != null && it.lcMessage.mappingId == message!!.mappingId }
                        messagesGlo[indexFound]!!.status = LCStatusMessage.failed
                        runOnUiThread {
                            adapter.notifyItemRangeChanged(indexFound,1)
                        }
                    }
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onReceiveMessage(lcMessage: LCMessage) {
                super.onReceiveMessage(lcMessage)
                logI("onReceiveMessage: $lcMessage")
                messagesGlo.add(LCMessageEntity(lcMessage=lcMessage, LCStatusMessage.sent))
                runOnUiThread {
                    adapter.notifyDataSetChanged()
                    rvChat.smoothScrollToPosition(adapter.itemCount)
                }
            }
        })
        LiveChatFactory.getMessages(0,limit)
    }

    private fun initView(){
        rvChat = findViewById(R.id.rv_chat)
        edtMessage = findViewById(R.id.edt_message)
        btnSend = findViewById(R.id.btnSend)
        btnAttach = findViewById(R.id.btnAttach)
        btnBack = findViewById(R.id.back_img)
        btnSend.setOnClickListener {
            if (edtMessage.text.toString().isEmpty()) return@setOnClickListener
            if (!adapter.isWaiting()) adapter.setIsScripting(false)
            if (adapter.getIndexWait() < adapter.getCurrScript().answers.size) {
                val answer = adapter.getCurrScript().answers[adapter.getIndexWait()]
                if (answer.type == "assign" || answer.type == "assign_team") {
                    adapter.setIsScripting(false)
                    adapter.setIndexWait(0)
                    adapter.setIsWaiting(false)
                    LiveChatFactory.sendMessage(
                        LCMessageSend(edtMessage.text.toString()),
                        if (adapter.isWaiting()) adapter.getIndexWait() else null,
                        if (adapter.isWaiting()) adapter.getCurrScript().id else null,
                    )
                    edtMessage.text.clear()
                    return@setOnClickListener
                }
                adapter.setIndexWait(adapter.getIndexWait() + 1)
            } else {
                adapter.setIsWaiting(false)
            }
            LiveChatFactory.sendMessage(
                LCMessageSend(edtMessage.text.toString()),
                if (adapter.isWaiting()) adapter.getIndexWait() else null,
                if (adapter.isWaiting()) adapter.getCurrScript().id else null,
            )
            if(adapter.getIndexWait() == adapter.getCurrScript().answers.size){
                adapter.setIsWaiting(false)
            }
            edtMessage.text.clear()
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnAttach.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.setType("image/*")
            startActivityForResult(intent, 0)
        }
        messagesGlo = ArrayList()
    }

    private lateinit var paths: ArrayList<String>

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 0){
            if (resultCode == RESULT_OK) {
                if(data != null){
                    paths = ArrayList()
                    if (data.clipData != null) {
                        logI("data!!.clipData ${data.clipData}")
                        val sizeChooser = data.clipData!!.itemCount
                        for (i in 0 until sizeChooser) {
                            val path: String = RealPathUtil.getRealPath(this@LCChatActivity, data.clipData!!.getItemAt(i).uri)!!
                            paths.add(path)
                        }
                    } else if (data.data != null) {
                        logI("data.data ${data.data}")
                        val path: String = RealPathUtil.getRealPath(this@LCChatActivity, data.data!!)!!
                        paths.add(path)
                    }
                    logI(paths.toString())
                    LiveChatFactory.sendFileMessage(paths,"image")
                }

            }
        }

    }
}
