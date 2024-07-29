package com.mitek.build.live.chat.sdk.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.mitek.build.live.chat.sdk.R
import com.mitek.build.live.chat.sdk.model.attachment.LCAttachment
import com.mitek.build.live.chat.sdk.model.chat.LCMessage
import com.mitek.build.live.chat.sdk.model.user.LCSession


class MessageAdapter(private val mContext: Context,private val  mList: ArrayList<LCMessage>,private val lcSession: LCSession) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(if(viewType == VIEW_TYPE_MESSAGE_SENT) R.layout.user_send else R.layout.user_receive, parent, false)
        return if(viewType == VIEW_TYPE_MESSAGE_SENT) ViewHolderSelf(view) else ViewHolderTarget(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = mList[position]
        if(holder.itemViewType == VIEW_TYPE_MESSAGE_SENT) {
            initSelf(holder as ViewHolderSelf, item)
        } else {
            initTarget(holder as ViewHolderTarget, item)
        }
    }

    private fun initSelf(holder: ViewHolderSelf, lcMessage: LCMessage){
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
                holder.cardView.visibility = View.GONE
            }
            "file" -> {
                val attachments = lcMessage.content!!.contentMessage as ArrayList<LCAttachment>
                val adapter = FileListAdapter(attachments)
                holder.rvImg.adapter = adapter
                holder.rvImg.layoutManager = CustomLayoutManager(mContext,false)
                holder.cardView.visibility = View.GONE
            }
            else -> {
                holder.tvContentMessage.text = lcMessage.content!!.contentMessage as String
                holder.rvImg.visibility = View.GONE
            }
        }
        holder.tvCreatedAt.text = lcMessage.timeCreated
    }

    private fun initTarget(holder: ViewHolderTarget, lcMessage: LCMessage){
        when (lcMessage.content!!.contentType) {
            "image" -> {
                var attachments = lcMessage.content!!.contentMessage as ArrayList<LCAttachment>
                val urls = ArrayList<String>()
                attachments.forEach {
                    urls.add(it.url)
                }
                val adapter = ImageListAdapter(urls)
                holder.rvImg.adapter = adapter
                holder.rvImg.layoutManager = CustomLayoutManager(mContext,false)
                holder.cardView.visibility = View.GONE
            }
            "file" -> {
                var attachments = lcMessage.content!!.contentMessage as ArrayList<LCAttachment>
                val urls = ArrayList<String>()
                val adapter = FileListAdapter(attachments)
                holder.rvImg.adapter = adapter
                holder.rvImg.layoutManager = CustomLayoutManager(mContext,false)
                holder.cardView.visibility = View.GONE
            }
            else -> {
                holder.tvContentMessage.text = lcMessage.content!!.contentMessage as String
                holder.rvImg.visibility = View.GONE
            }
        }
        holder.tvNameSender.text = lcMessage.from!!.name
        holder.tvCreatedAt.text = lcMessage.timeCreated
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if(mList[position].from!!.id == lcSession.visitorJid){
            VIEW_TYPE_MESSAGE_SENT
        } else {
            VIEW_TYPE_MESSAGE_RECEIVED
        }
    }

    // Holds the views for adding it to image and text
    class ViewHolderSelf(item: View) : RecyclerView.ViewHolder(item) {
        val tvContentMessage: TextView = itemView.findViewById(R.id.content_message)
        val tvCreatedAt: TextView = itemView.findViewById(R.id.created_at)
        val cardView: CardView = itemView.findViewById(R.id.cardContent)
        val rvImg: RecyclerView = itemView.findViewById(R.id.rv_img)
    }

    // Holds the views for adding it to image and text
    class ViewHolderTarget(item: View) : RecyclerView.ViewHolder(item) {
        val tvNameSender: TextView = itemView.findViewById(R.id.sender_name)
        val tvContentMessage: TextView = itemView.findViewById(R.id.content_message)
        val cardView: CardView = itemView.findViewById(R.id.cardContent)
        val tvCreatedAt: TextView = itemView.findViewById(R.id.created_at)
        val rvImg: RecyclerView = itemView.findViewById(R.id.rv_img)
    }

    companion object {
        private const val VIEW_TYPE_MESSAGE_SENT = 1
        private const val VIEW_TYPE_MESSAGE_RECEIVED = 2
    }
}