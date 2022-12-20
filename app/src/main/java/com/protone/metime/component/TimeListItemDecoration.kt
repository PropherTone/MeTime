package com.protone.metime.component

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.protone.common.context.statuesBarHeight

class TimeListItemDecoration(private val margin: Int, private val bot: Int) :
    RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.bottom = margin
        outRect.left = margin
        outRect.right = margin
        val position = parent.getChildAdapterPosition(view)
        if (position == 0) outRect.top = parent.context.statuesBarHeight
        if (position >= (parent.layoutManager?.itemCount?.minus(1) ?: 0)) {
            outRect.bottom = outRect.bottom + bot
        }
    }
    
}