package com.mitek.build.live.chat.sdk.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.mitek.build.live.chat.sdk.R
import com.squareup.picasso.Picasso


class ImageListAdapter(private val urls: ArrayList<String>): RecyclerView.Adapter<ImageListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.img_message_view,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(urls[position].isEmpty()) return
        Picasso.get()
            .load(urls[position])
            .into(holder.imageView)

    }

    override fun getItemCount(): Int {
        return urls.size
    }

    class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val imageView: ImageView = itemView.findViewById(R.id.img_el)
    }
}
