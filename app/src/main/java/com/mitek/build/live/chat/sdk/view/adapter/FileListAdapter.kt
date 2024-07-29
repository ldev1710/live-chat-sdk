package com.mitek.build.live.chat.sdk.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mitek.build.live.chat.sdk.R
import com.mitek.build.live.chat.sdk.model.attachment.LCAttachment

class FileListAdapter(private val files: ArrayList<LCAttachment>): RecyclerView.Adapter<FileListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.file_message_view,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvFileName.text = files[position].fileName
    }

    override fun getItemCount(): Int {
        return files.size
    }

    class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val tvFileName: TextView = itemView.findViewById(R.id.tvFileName)
    }
}