package com.mitek.build.live.chat.sdk.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
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
import com.mitek.build.live.chat.sdk.model.internal.MessageReceiveSource
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
                isCanLoadMore = messages.size % 5 == 0
                runOnUiThread {
                    if(isInit){
                        messages.reversed().map {
                            messagesGlo.add(LCMessageEntity(lcMessage = it, LCStatusMessage.sent))
                        }
                        adapter = MessageAdapter(this@LCChatActivity,messagesGlo,LiveChatSDK.getLCSession())
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
                            var tmp = ArrayList<LCMessageEntity>()
                            messages.reversed().map {
                                tmp.add(LCMessageEntity(lcMessage = it,LCStatusMessage.sent))
                            }
                            messagesGlo.addAll(0,tmp)
                            adapter.notifyItemRangeChanged(0,messages.size)
                        }
                    }
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onSendMessageStateChange(state: LCSendMessageEnum, message: LCMessage?, errorMessage: String?) {
                super.onSendMessageStateChange(state, message, errorMessage)
                logI("onSendMessageStateChange: $state | $message")
                if(state == LCSendMessageEnum.SENT_SUCCESS){
                    val indexFound = messagesGlo.indexOfFirst { it != null && it.lcMessage.mappingId == message!!.mappingId }
                    messagesGlo[indexFound]!!.status = LCStatusMessage.sent
                    runOnUiThread {
                        Thread.sleep(3000)
                        adapter.notifyItemRangeChanged(indexFound,1)
                    }
                } else if(state == LCSendMessageEnum.SENDING){
                    messagesGlo.add(LCMessageEntity(lcMessage = message!!,LCStatusMessage.sending))
                    runOnUiThread {
                        adapter.notifyDataSetChanged()
                        rvChat.smoothScrollToPosition(adapter.itemCount)
                    }
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onReceiveMessage(lcMessage: LCMessage) {
                super.onReceiveMessage(lcMessage)
                logI("onReceiveMessage: $lcMessage")
                messagesGlo.add(LCMessageEntity(lcMessage=lcMessage,LCStatusMessage.sent))
                runOnUiThread {
                    adapter.notifyDataSetChanged()
                    rvChat.smoothScrollToPosition(adapter.itemCount)
                }
            }
        })
        LiveChatFactory.getMessages(0,limit)
    }

    private fun initView(){
        var sources = ArrayList<MessageReceiveSource>()
        sources.add(MessageReceiveSource.socket)
        LiveChatSDK.setMessageReceiveSource(sources)
        rvChat = findViewById(R.id.rv_chat)
        edtMessage = findViewById(R.id.edt_message)
        btnSend = findViewById(R.id.btnSend)
        btnAttach = findViewById(R.id.btnAttach)
        btnSend.setOnClickListener {
            if(edtMessage.text.toString().isEmpty()) return@setOnClickListener
            LiveChatFactory.sendMessage(LCMessageSend(edtMessage.text.toString()))
            edtMessage.text.clear()
        }

        btnAttach.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
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
        if (resultCode == RESULT_OK) {
            paths = ArrayList()
            if (data!!.clipData != null) {
                val sizeChooser = data.clipData!!.itemCount
                for (i in 0 until sizeChooser) {
                    val path: String = RealPathUtil.getRealPath(this@LCChatActivity, data.clipData!!.getItemAt(i).uri)!!
                    paths.add(path)
                }
            } else if (data.data != null) {
                val path: String = RealPathUtil.getRealPath(this@LCChatActivity, data.data!!)!!
                paths.add(path)
            }
            logI(paths.toString())
            LiveChatFactory.sendFileMessage(paths,"image")
        }
    }
}