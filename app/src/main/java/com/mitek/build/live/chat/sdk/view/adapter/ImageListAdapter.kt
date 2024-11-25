package com.mitek.build.live.chat.sdk.view.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.mitek.build.live.chat.sdk.R
import com.squareup.picasso.Picasso
import java.io.File


class ImageListAdapter(private val context:Context, private val urls: ArrayList<String>): RecyclerView.Adapter<ImageListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.img_message_view,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(urls[position].isEmpty()) return
        if(urls[position].startsWith("http")) {
            Picasso.get()
                .load(urls[position])
                .into(holder.imageView)
            holder.imageView.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(urls[position])
                }
                context.startActivity(intent)
            }
        } else {
            val file =  File(urls[position])
            Picasso.get()
                .load(file)
                .into(holder.imageView)
        }
        holder.imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        holder.imageView.setBackgroundColor(Color.WHITE)
    }

    override fun getItemCount(): Int {
        return urls.size
    }

    class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val imageView: ImageView = itemView.findViewById(R.id.img_el)
    }
}
