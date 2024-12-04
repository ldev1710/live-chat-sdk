package com.mitek.build.live.chat.sdk.view.adapter

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration


class CustomLayoutManager(private val context: Context,private val isScrollable: Boolean) : LinearLayoutManager(context) {
    override fun canScrollVertically(): Boolean {
        return isScrollable
    }
}

class RecyclerViewMargin(
    private val margin: Int,
) :
    ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State
    ) {
        outRect.bottom = margin
    }
}
