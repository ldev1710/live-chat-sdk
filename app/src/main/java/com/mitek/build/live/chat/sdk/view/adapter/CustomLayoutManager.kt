package com.mitek.build.live.chat.sdk.view.adapter

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

class CustomLayoutManager(private val context: Context,private val isScrollable: Boolean) : LinearLayoutManager(context) {
    override fun canScrollVertically(): Boolean {
        return isScrollable
    }
}