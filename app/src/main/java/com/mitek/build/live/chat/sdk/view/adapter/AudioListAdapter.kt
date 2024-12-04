package com.mitek.build.live.chat.sdk.view.adapter

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView
import com.mitek.build.live.chat.sdk.R
import com.mitek.build.live.chat.sdk.model.attachment.LCAttachment


class AudioListAdapter(private val context: Context, private val urls: ArrayList<LCAttachment>): RecyclerView.Adapter<AudioListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.audio_message_view,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.runnable = Runnable { updateSeekBar(holder) }
        holder.mediaPlayer = MediaPlayer().apply {
            setDataSource(urls[position].url)
            prepareAsync()
            setOnPreparedListener {
                holder.audioSeekBar.max = it.duration
                updateSeekBar(holder)
            }
            setOnCompletionListener {
                holder.isPlaying = false
                holder.playPauseButton.setImageResource(R.drawable.ic_play)
            }
        }

        // Set up play/pause button functionality
        holder.playPauseButton.setOnClickListener {
            if (holder.isPlaying) {
                pauseAudio(holder)
            } else {
                playAudio(holder)
            }
        }

        holder.audioSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    holder.mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun playAudio(holder: ViewHolder) {
        holder.mediaPlayer?.start()
        holder.isPlaying = true
        holder.playPauseButton.setImageResource(R.drawable.ic_pause)
        updateSeekBar(holder)
    }

    private fun pauseAudio(holder: ViewHolder) {
        holder.mediaPlayer?.pause()
        holder.isPlaying = false
        holder.playPauseButton.setImageResource(R.drawable.ic_play)
    }

    private fun updateSeekBar(holder: ViewHolder) {
        holder.mediaPlayer?.let {
            holder.audioSeekBar.progress = it.currentPosition
            holder.handler.postDelayed(holder.runnable, 1000)
            if(!holder.mediaPlayer!!.isPlaying){
                holder.handler.removeCallbacks(holder.runnable)
            }
        }
    }

    override fun getItemCount(): Int {
        return urls.size
    }

    class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        var playPauseButton: ImageButton = item.findViewById(R.id.playPauseButton)
        var audioSeekBar: SeekBar = item.findViewById(R.id.audioSeekBar)
        var mediaPlayer: MediaPlayer? = null
        val handler = Handler()
        lateinit var runnable: Runnable
        var isPlaying = false
    }
}
