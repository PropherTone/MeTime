package com.protone.component.view.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.protone.component.R
import com.protone.common.utils.displayUtils.AnimationHelper
import kotlinx.coroutines.launch
import java.util.concurrent.LinkedBlockingDeque

abstract class SelectListAdapter<VB : ViewDataBinding, Item : Any, Event>(
    context: Context,
    handleEvent: Boolean = false
) : BaseAdapter<Item, VB, Event>(context, handleEvent) {

    var selectList = LinkedBlockingDeque<Item>()
    var multiChoose = false

    var hasFixedSize = true

    companion object {
        const val SELECT = "SELECT"
        const val UNSELECT = "UNSELECT"
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.setHasFixedSize(hasFixedSize)
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onBindViewHolder(holder: Holder<VB>, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            this.onBindViewHolder(holder, position)
            return
        }
        if (payloads.first() !is String) return
        setSelect(
            holder.binding,
            position,
            when (payloads.first()) {
                SELECT -> true
                UNSELECT -> false
                else -> false
            }
        )
    }

    open fun checkSelect(position: Int, item: Item) {
        if (selectList.contains(item)) {
            selectList.remove(item)
            notifyItemChangedChecked(position, UNSELECT)
        } else {
            if (!multiChoose) clearSelected()
            selectList.add(item)
            notifyItemChangedChecked(position, SELECT)
        }
    }

    abstract val select: (content: VB, position: Int, isSelect: Boolean) -> Unit
    abstract fun itemIndex(path: Item): Int

    fun setSelect(content: VB, position: Int, state: Boolean) = select(content, position, state)

    fun clearSelected() {
        if (selectList.size > 0) {
            val itemIndex = itemIndex(selectList.first)
            selectList.clear()
            if (itemIndex != -1) {
                launch {
                    notifyItemChangedChecked(itemIndex, UNSELECT)
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
            if (it != -1) launch {
                notifyItemChangedChecked(it, UNSELECT)
            }
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