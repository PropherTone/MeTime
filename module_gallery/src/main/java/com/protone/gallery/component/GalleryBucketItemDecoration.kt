package com.protone.gallery.component

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.protone.common.R

class GalleryBucketItemDecoration(private val margin: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.top = margin
        outRect.left = margin
        outRect.right = margin
    }

}