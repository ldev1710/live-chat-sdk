package com.mitek.build.live.chat.sdk.view

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.mitek.build.live.chat.sdk.R
import com.mitek.build.live.chat.sdk.core.LiveChatFactory
import com.mitek.build.live.chat.sdk.listener.publisher.LiveChatListener
import com.mitek.build.live.chat.sdk.model.chat.LCMessage
import com.mitek.build.live.chat.sdk.model.chat.LCMessageSend
import com.mitek.build.live.chat.sdk.model.chat.LCSendMessageEnum
import com.mitek.build.live.chat.sdk.model.user.LCSession
import com.mitek.build.live.chat.sdk.util.LCLog.logI
import com.mitek.build.live.chat.sdk.util.RealPathUtil
import com.mitek.build.live.chat.sdk.view.adapter.MessageAdapter

class LCChatActivity(val lcSession: LCSession) : AppCompatActivity() {

    private lateinit var rvChat: RecyclerView
    private lateinit var adapter: MessageAdapter
    private lateinit var messagesGlo: ArrayList<LCMessage>
    private lateinit var edtMessage: EditText
    private lateinit var btnSend: ImageView
    private lateinit var btnAttach: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        initView()
        LiveChatFactory.addEventListener(object : LiveChatListener() {
            override fun onGotDetailConversation(messages: ArrayList<LCMessage>) {
                super.onGotDetailConversation(messages)
                logI("onGotDetailConversation: $messages")
                messagesGlo = ArrayList(messages.reversed())
                adapter = MessageAdapter(this@LCChatActivity,messagesGlo,lcSession)
                runOnUiThread {
                    rvChat.adapter = adapter
                    rvChat.smoothScrollToPosition(adapter.itemCount)
                }
            }

            override fun onSendMessageStateChange(state: LCSendMessageEnum, message: LCMessage?, errorMessage: String?) {
                super.onSendMessageStateChange(state, message, errorMessage)
                logI("onSendMessageStateChange: $state | $message")
                if(state == LCSendMessageEnum.SENT_SUCCESS){
                    messagesGlo.add(message!!)
                    runOnUiThread {
                        adapter.notifyDataSetChanged()
                        rvChat.smoothScrollToPosition(adapter.itemCount)
                    }
                }
            }

            override fun onReceiveMessage(lcMessage: LCMessage) {
                super.onReceiveMessage(lcMessage)
                logI("onReceiveMessage: $lcMessage")
                messagesGlo.add(lcMessage)
                runOnUiThread {
                    adapter.notifyDataSetChanged()
                    rvChat.smoothScrollToPosition(adapter.itemCount)
                }
            }
        })
        LiveChatFactory.getMessages(lcSession.sessionId,0,5)
    }

    private fun initView(){
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
            LiveChatFactory.sendFileMessage(paths)
        }
    }
}