package com.mitek.build.live.chat.sdk.view.adapter

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.mitek.build.live.chat.sdk.R
import com.mitek.build.live.chat.sdk.model.attachment.LCAttachment
import com.mitek.build.live.chat.sdk.model.chat.LCMessage
import com.mitek.build.live.chat.sdk.model.chat.LCMessageEntity
import com.mitek.build.live.chat.sdk.model.chat.LCStatusMessage
import com.mitek.build.live.chat.sdk.model.user.LCSession


class MessageAdapter(private val mContext: Context, private val  mList: ArrayList<LCMessageEntity?>, private val lcSession: LCSession) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            when (viewType) {
                VIEW_TYPE_MESSAGE_SENT -> R.layout.user_send
                VIEW_TYPE_MESSAGE_RECEIVED -> R.layout.user_receive
                else -> R.layout.loading_more
            },
            parent,
            false
        )
        return if(viewType == VIEW_TYPE_MESSAGE_SENT) ViewHolderSelf(view) else if(viewType == VIEW_TYPE_MESSAGE_RECEIVED) ViewHolderTarget(view) else ViewHolderLoadingMore(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = mList[position]

        when (holder.itemViewType) {
            VIEW_TYPE_MESSAGE_SENT -> {
                initSelf(holder as ViewHolderSelf, item!!, position)
            }
            VIEW_TYPE_MESSAGE_RECEIVED -> {
                initTarget(holder as ViewHolderTarget, item!!.lcMessage)
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
        } else {
            holder.tvStatusSend.text = "Đã gửi"
            if(position == mList.size-1){
                holder.tvStatusSend.visibility = View.VISIBLE
            }
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

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return if(mList[position]==null) {
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

    class ViewHolderLoadingMore(item: View) : RecyclerView.ViewHolder(item) {
        val prg: ProgressBar = itemView.findViewById(R.id.fetching_more)
    }

    companion object {
        private const val VIEW_TYPE_MESSAGE_SENT = 1
        private const val VIEW_TYPE_MESSAGE_RECEIVED = 2
        private const val VIEW_TYPE_FETCHING = 3
    }
}