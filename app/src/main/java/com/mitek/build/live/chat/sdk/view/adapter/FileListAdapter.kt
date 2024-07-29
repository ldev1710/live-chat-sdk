package com.vn.build.examplelivechatsdk.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mitek.build.live.chat.sdk.model.attachment.LCAttachment
import com.mitek.build.live.chat.sdk.model.attachment.LCFile
import com.squareup.picasso.Picasso
import com.vn.build.examplelivechatsdk.R

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