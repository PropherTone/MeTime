package com.protone.ui.itemDecoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ModelListItemDecoration(private val interval: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val childPosition = parent.getChildLayoutPosition(view)
        outRect.top = interval
        outRect.left = interval
        outRect.right = interval
        if (childPosition >= parent.childCount - 1) {
            outRect.bottom = interval
        }
    }
}