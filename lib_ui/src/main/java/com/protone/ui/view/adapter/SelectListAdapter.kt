package com.protone.ui.view.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.protone.ui.R
import com.protone.common.utils.displayUtils.AnimationHelper
import kotlinx.coroutines.launch

abstract class SelectListAdapter<V : ViewDataBinding, T, D>(
    context: Context,
    handleEvent: Boolean = false
) : BaseAdapter<V, D>(context, handleEvent) {

    var selectList = mutableListOf<T>()
    var multiChoose = false

    var hasFixedSize = true

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.setHasFixedSize(hasFixedSize)
        super.onAttachedToRecyclerView(recyclerView)
    }

    open fun checkSelect(holder: Holder<V>, item: T) {
        if (selectList.contains(item)) {
            selectList.remove(item)
            setSelect(holder, false)
        } else {
            if (!multiChoose) clearSelected()
            selectList.add(item)
            setSelect(holder, true)
        }
    }

    abstract val select: (holder: Holder<V>, isSelect: Boolean) -> Unit
    abstract fun itemIndex(path: T): Int

    fun setSelect(holder: Holder<V>, state: Boolean) = launch {
        select(holder, state)
    }

    fun clearSelected() {
        if (selectList.size > 0) {
            val itemIndex = itemIndex(selectList[0])
            selectList.clear()
            if (itemIndex != -1) {
                launch {
                    notifyItemChanged(itemIndex)
                }
            }
        }
    }

    fun clearAllSelected() {
        val list = selectList.map {
            itemIndex(it)
        }.toList()
        selectList.clear()
        list.forEach {
            if (it != -1) notifyItemChanged(it)
        }
    }

    fun clickAnimation(
        pressed: Boolean,
        background: View?,
        container: ViewGroup?,
        visible: View?,
        vararg texts: TextView,
        dispatch: Boolean = true,
        @ColorRes backgroundColor: Int = R.color.white,
        @ColorRes backgroundColorPressed: Int = R.color.blue_1,
        @ColorRes textsColor: Int = R.color.black,
        @ColorRes textsColorPressed: Int = R.color.white
    ) {
        background?.setBackgroundColor(
            ContextCompat.getColor(
                context,
                if (pressed) backgroundColorPressed else backgroundColor
            )
        )
        visible?.visibility = if (pressed) View.VISIBLE else View.GONE
        texts.forEach {
            it.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (pressed) textsColorPressed else textsColor
                )
            )
        }
        itemClickChange(
            if (pressed) backgroundColorPressed else backgroundColor,
            if (pressed) textsColorPressed else textsColor,
            background, container, texts, pressed && dispatch
        )
    }

    fun itemClickChange(
        backgroundColor: Int,
        textsColor: Int,
        background: View?,
        container: ViewGroup?,
        texts: Array<out TextView>,
        pressed: Boolean,
    ) {
        background?.setBackgroundColor(
            ContextCompat.getColor(
                context,
                backgroundColor
            )
        )
        texts.forEach {
            it.setTextColor(
                ContextCompat.getColor(
                    context,
                    textsColor
                )
            )
        }
        if (container != null && pressed) {
            startAnimation(container)
        }
    }

    private fun startAnimation(target: ViewGroup) {
        AnimationHelper.apply {
            val x = scaleX(target, 0.96f, duration = 50)
            val y = scaleY(target, 0.96f, duration = 50)
            val x1 = scaleX(target, 1f, duration = 360)
            val y1 = scaleY(target, 1f, duration = 360)
            animatorSet(x, y, play = true, doOnEnd = { animatorSet(x1, y1, play = true) })
        }
    }
}