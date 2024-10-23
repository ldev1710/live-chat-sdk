package com.mitek.build.live.chat.sdk.view.adapter

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.mitek.build.live.chat.sdk.R
import com.mitek.build.live.chat.sdk.listener.observe.OnClickObserve
import com.mitek.build.live.chat.sdk.model.attachment.LCAttachment
import com.mitek.build.live.chat.sdk.model.chat.LCMessage
import com.mitek.build.live.chat.sdk.model.chat.LCMessageEntity
import com.mitek.build.live.chat.sdk.model.chat.LCStatusMessage
import com.mitek.build.live.chat.sdk.model.internal.LCButtonAction
import com.mitek.build.live.chat.sdk.model.internal.LCScript
import com.mitek.build.live.chat.sdk.model.user.LCSession
import com.mitek.build.live.chat.sdk.util.LCLog


class MessageAdapter(
    private val mContext: Context,
    private val mList: ArrayList<LCMessageEntity?>,
    private val lcSession: LCSession,
    private var scripts: ArrayList<LCScript>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var isWaiting = false
    private lateinit var currScript: LCScript
    private var isScriptingMessage: Boolean
    private var indexWait = 0

    init {
        isScriptingMessage = scripts.isNotEmpty()
        if(isScriptingMessage){
            currScript = scripts.first()
        }
        LCLog.logI("scripts: $scripts")
    }

    fun getIndexWait() : Int {
        return this.indexWait
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setIsScripting(isScripting: Boolean){
        this.isScriptingMessage = isScripting
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setIsWaiting(isWaiting: Boolean){
        this.isWaiting = isWaiting
        notifyDataSetChanged()
    }

    fun isWaiting() : Boolean {
        return isWaiting
    }

    fun getCurrScript() : LCScript {
        return currScript
    }

    fun setIndexWait(newValue: Int) {
        indexWait = newValue
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            when (viewType) {
                VIEW_TYPE_MESSAGE_SENT -> R.layout.user_send
                VIEW_TYPE_MESSAGE_RECEIVED -> R.layout.user_receive
                VIEW_TYPE_SCRIPTING -> R.layout.script_layout
                else -> R.layout.loading_more
            },
            parent,
            false
        )
        return when (viewType) {
            VIEW_TYPE_MESSAGE_SENT -> ViewHolderSelf(view)
            VIEW_TYPE_MESSAGE_RECEIVED -> ViewHolderTarget(view)
            VIEW_TYPE_SCRIPTING -> ViewHolderScripting(view)
            else -> ViewHolderLoadingMore(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder.itemViewType) {
            VIEW_TYPE_MESSAGE_SENT -> {
                val item = mList[position]
                initSelf(holder as ViewHolderSelf, item!!, position)
            }
            VIEW_TYPE_MESSAGE_RECEIVED -> {
                val item = mList[position]
                initTarget(holder as ViewHolderTarget, item!!.lcMessage)
            }
            VIEW_TYPE_SCRIPTING -> {
                initScripting(holder as ViewHolderScripting)
            }
            else -> {

            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showPopupMenu(view: View, message: LCMessage) {
        val popupMenu = PopupMenu(mContext, view)
        popupMenu.menuInflater.inflate(R.menu.menu_context, popupMenu.menu)
        val textCopy = if(message.content!!.contentType == "image" || message.content!!.contentMessage == "file"){
            (message.content!!.contentMessage as ArrayList<LCAttachment>).first().url
        } else {
            message.content!!.contentMessage as String
        }

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.action_copy -> {
                    copyToClipboard(textCopy)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun copyToClipboard(text: String) {
        val clipboard = mContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", text)
        clipboard.setPrimaryClip(clip)
    }

    private fun initSelf(holder: ViewHolderSelf, lcMessageEntity: LCMessageEntity, position: Int){
        holder.rvImg.visibility = View.GONE
        holder.cardView.visibility = View.GONE
        holder.tvStatusSend.visibility = View.GONE
        if(lcMessageEntity.status == LCStatusMessage.sending) {
            holder.tvStatusSend.text = "Đang gửi"
            holder.tvStatusSend.visibility = View.VISIBLE
        } else if(lcMessageEntity.status == LCStatusMessage.sent){
            holder.tvStatusSend.text = "Đã gửi"
            if(position == mList.size-1){
                holder.tvStatusSend.visibility = View.VISIBLE
            }
        } else {
            holder.tvStatusSend.text = "Không thể gửi"
            holder.tvStatusSend.setTextColor(Color.RED)
            holder.tvStatusSend.visibility = View.VISIBLE
        }
        when (lcMessageEntity.lcMessage.content!!.contentType) {
            "image" -> {
                val attachments = lcMessageEntity.lcMessage.content!!.contentMessage as ArrayList<LCAttachment>
                val urls = ArrayList<String>()
                attachments.forEach {
                    urls.add(it.url)
                }
                val adapter = ImageListAdapter(urls)
                holder.rvImg.adapter = adapter
                holder.rvImg.layoutManager = CustomLayoutManager(mContext,false)
                holder.rvImg.visibility = View.VISIBLE
            }
            "file" -> {
                val attachments = lcMessageEntity.lcMessage.content!!.contentMessage as ArrayList<LCAttachment>
                val adapter = FileListAdapter(attachments)
                holder.rvImg.adapter = adapter
                holder.rvImg.layoutManager = CustomLayoutManager(mContext,false)
                holder.rvImg.visibility = View.VISIBLE
            }
            else -> {
                holder.tvContentMessage.setOnLongClickListener { view ->
                    showPopupMenu(view,lcMessageEntity.lcMessage)
                    true
                }
                holder.tvContentMessage.text = lcMessageEntity.lcMessage.content!!.contentMessage as String
                holder.cardView.visibility = View.VISIBLE
            }
        }
        holder.tvCreatedAt.text = lcMessageEntity.lcMessage.timeCreated
    }

    private fun initTarget(holder: ViewHolderTarget, lcMessage: LCMessage){
        holder.rvImg.visibility = View.GONE
        holder.cardView.visibility = View.GONE
        when (lcMessage.content!!.contentType) {
            "image" -> {
                val attachments = lcMessage.content!!.contentMessage as ArrayList<LCAttachment>
                val urls = ArrayList<String>()
                attachments.forEach {
                    urls.add(it.url)
                }
                val adapter = ImageListAdapter(urls)
                holder.rvImg.adapter = adapter
                holder.rvImg.layoutManager = CustomLayoutManager(mContext,false)
                holder.rvImg.visibility = View.VISIBLE
            }
            "file" -> {
                var attachments = lcMessage.content!!.contentMessage as ArrayList<LCAttachment>
                val adapter = FileListAdapter(attachments)
                holder.rvImg.adapter = adapter
                holder.rvImg.layoutManager = CustomLayoutManager(mContext,false)
                holder.rvImg.visibility = View.VISIBLE
            }
            else -> {
                holder.tvContentMessage.setOnLongClickListener { view ->
                    showPopupMenu(view,lcMessage)
                    true
                }
                holder.tvContentMessage.text = lcMessage.content!!.contentMessage as String
                holder.cardView.visibility = View.VISIBLE
            }
        }
        holder.tvNameSender.text = lcMessage.from!!.name
        holder.tvCreatedAt.text = lcMessage.timeCreated
    }

    private fun initScripting(holder: ViewHolderScripting){
        val layoutManager = FlexboxLayoutManager(mContext)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.FLEX_START
        holder.scriptView.layoutManager = layoutManager
        val nextScript = scripts.find { it.id == currScript.id }
        if(nextScript == null){
            setIsScripting(false)
            return
        }
        val adapter = ScriptAdapter(mContext, nextScript.buttonAction, object : OnClickObserve {
            override fun onClick(lcButtonAction: LCButtonAction) {
                LCLog.logI("nextId: ${lcButtonAction.nextId}")
                val nextScript = scripts.find { it.id == lcButtonAction.nextId }
                LCLog.logI("nextScript: $nextScript")
                if(nextScript == null){
                    setIsScripting(false)
                    return
                }
                indexWait = 0
                currScript = nextScript
                setIsWaiting(currScript.answers.find { it.type == "customer" } != null)
                LCLog.logI("isWait: $isWaiting")
            }
        })
        holder.scriptView.setAdapter(adapter)

    }

    override fun getItemCount(): Int {
        return mList.size + if(isScriptingMessage && !isWaiting) 1 else 0
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return if(position == itemCount-1 && isScriptingMessage && !isWaiting){
            VIEW_TYPE_SCRIPTING
        } else if(mList[position]==null) {
            VIEW_TYPE_FETCHING
        } else if(mList[position]!!.lcMessage.from!!.id == lcSession.visitorJid){
            VIEW_TYPE_MESSAGE_SENT
        } else {
            VIEW_TYPE_MESSAGE_RECEIVED
        }
    }

    class ViewHolderSelf(item: View) : RecyclerView.ViewHolder(item) {
        val tvContentMessage: TextView = itemView.findViewById(R.id.content_message)
        val tvCreatedAt: TextView = itemView.findViewById(R.id.created_at)
        val tvStatusSend: TextView = itemView.findViewById(R.id.status_send)
        val cardView: CardView = itemView.findViewById(R.id.cardContent)
        val rvImg: RecyclerView = itemView.findViewById(R.id.rv_img)
    }

    class ViewHolderTarget(item: View) : RecyclerView.ViewHolder(item) {
        val tvNameSender: TextView = itemView.findViewById(R.id.sender_name)
        val tvContentMessage: TextView = itemView.findViewById(R.id.content_message)
        val cardView: CardView = itemView.findViewById(R.id.cardContent)
        val tvCreatedAt: TextView = itemView.findViewById(R.id.created_at)
        val rvImg: RecyclerView = itemView.findViewById(R.id.rv_img)
    }

    class ViewHolderScripting(item: View): RecyclerView.ViewHolder(item){
        val scriptView: RecyclerView = itemView.findViewById(R.id.script_view)
    }

    class ViewHolderLoadingMore(item: View) : RecyclerView.ViewHolder(item) {
        val prg: ProgressBar = itemView.findViewById(R.id.fetching_more)
    }

    companion object {
        private const val VIEW_TYPE_MESSAGE_SENT = 1
        private const val VIEW_TYPE_MESSAGE_RECEIVED = 2
        private const val VIEW_TYPE_FETCHING = 3
        private const val VIEW_TYPE_SCRIPTING = 4
    }
}
