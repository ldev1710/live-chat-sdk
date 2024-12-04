package com.mitek.build.live.chat.sdk.view.adapter

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.mitek.build.live.chat.sdk.R
import com.mitek.build.live.chat.sdk.model.attachment.LCAttachment

class VideoListAdapter(private val context: Context, private val urls: ArrayList<LCAttachment>) :
    RecyclerView.Adapter<VideoListAdapter.ViewHolder>() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.vid_message_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val videoUrl = urls[position].url
        if (videoUrl.isEmpty()) return

        if (videoUrl.startsWith("http")) {
            holder.videoView.setVideoURI(Uri.parse(videoUrl))
        } else {
            holder.videoView.setVideoPath(videoUrl)
        }

        holder.videoView.setOnCompletionListener {
            updateSeekBar(holder)
        }

        holder.videoView.setOnPreparedListener { mediaPlayer ->
            holder.isProgressing = false
            holder.prgBar.visibility = View.GONE
            holder.seekBar.max = mediaPlayer.duration
            mediaPlayer.start()
            updateSeekBar(holder)
        }

        holder.playPauseButton.setOnClickListener {
            if (holder.videoView.isPlaying) {
                holder.videoView.pause()
                holder.playPauseButton.setImageResource(R.drawable.ic_play) // Change to play icon
            } else {
                holder.videoView.start()
                holder.playPauseButton.setImageResource(R.drawable.ic_pause) // Change to pause icon
                updateSeekBar(holder)
            }
        }

        holder.videoView.setOnClickListener {
            if(holder.isProgressing) return@setOnClickListener
            if (holder.playPauseButton.visibility == View.VISIBLE) {
                holder.playPauseButton.visibility = View.GONE
                holder.seekBar.visibility = View.GONE
                holder.overlayView.visibility = View.INVISIBLE
            } else {
                holder.playPauseButton.visibility = View.VISIBLE
                holder.seekBar.visibility = View.VISIBLE
                holder.overlayView.visibility = View.VISIBLE
            }
        }

        holder.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    holder.videoView.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun updateSeekBar(holder: ViewHolder) {
        holder.seekBar.progress = holder.videoView.currentPosition
        if (holder.videoView.isPlaying) {
            handler.postDelayed({ updateSeekBar(holder) }, 1000)
        }
    }

    override fun getItemCount(): Int {
        return urls.size
    }

    class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val videoView: VideoView = itemView.findViewById(R.id.video_view)
        val overlayView: View = itemView.findViewById(R.id.overlay_view)
        val playPauseButton: ImageButton = itemView.findViewById(R.id.btn_play_pause)
        val seekBar: SeekBar = itemView.findViewById(R.id.seek_bar)
        val prgBar: ProgressBar = itemView.findViewById(R.id.prg_video)
        var isProgressing: Boolean = true
    }
}
