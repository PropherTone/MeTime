package com.protone.component.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.protone.common.baseType.bufferCollect
import com.protone.common.baseType.launchDefault
import com.protone.common.baseType.withMainContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

abstract class BaseAdapter<Item : Any, VB : ViewDataBinding, Event>(
    val context: Context,
    private val handleEvent: Boolean = false
) : RecyclerView.Adapter<BaseAdapter.Holder<VB>>(),
    CoroutineScope by CoroutineScope(Dispatchers.Main) {

    open class Holder<B : ViewDataBinding>(val binding: B) : RecyclerView.ViewHolder(binding.root)

    private val _adapterFlow = MutableSharedFlow<Event>()
    private val adapterFlow get() = _adapterFlow.asSharedFlow()

    val mList = mutableListOf<Item>()

    protected var layoutManager: LinearLayoutManager? = null
    protected var firstPosition: Int = -1
    protected var lastPosition: Int = -1

    interface AdapterDiff<Item : Any> {
        fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean
        fun getChangePayload(oldItem: Item, newItem: Item): Boolean
    }

    private var diff: AdapterDiff<Item>? = null

    fun setAdapterDiff(adapterDiff: AdapterDiff<Item>) {
        diff = adapterDiff
    }

    fun getDefaultDiff() = object : AdapterDiff<String> {
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

    }

    open fun setData(collection: Collection<Item>) {
        mList.clear()
        mList.addAll(collection)
    }

    open suspend fun handleEventAsynchronous(data: Event) {}

    fun refreshVisiblePosition() {
        layoutManager?.apply {
            firstPosition = findFirstVisibleItemPosition()
            lastPosition = findLastVisibleItemPosition()
        }
    }

    protected fun emit(value: Event) {
        launch {
            _adapterFlow.emit(value)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        layoutManager =
            recyclerView.layoutManager.takeIf { it is LinearLayoutManager } as LinearLayoutManager
        refreshVisiblePosition()
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    refreshVisiblePosition()
                }
            }
        })
        super.onAttachedToRecyclerView(recyclerView)
        if (handleEvent) launchDefault {
            adapterFlow.bufferCollect {
                handleEventAsynchronous(it)
            }
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        cancel()
    }

    override fun getItemCount(): Int = mList.size

    fun notifyListChangedCO(collection: List<Item>) {
        launchDefault {
            diff?.apply {
                DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                    override fun getOldListSize(): Int = mList.size

                    override fun getNewListSize(): Int = collection.size

                    override fun areItemsTheSame(
                        oldItemPosition: Int,
                        newItemPosition: Int
                    ): Boolean {
                        return mList[oldItemPosition] == collection[newItemPosition]
                    }

                    override fun areContentsTheSame(
                        oldItemPosition: Int,
                        newItemPosition: Int
                    ): Boolean {
                        return areContentsTheSame(
                            mList[oldItemPosition],
                            collection[newItemPosition]
                        )
                    }

                    override fun getChangePayload(
                        oldItemPosition: Int,
                        newItemPosition: Int
                    ): Any {
                        return getChangePayload(
                            mList[oldItemPosition],
                            collection[newItemPosition]
                        )
                    }
                }).also {
                    setData(collection)
                    withMainContext {
                        it.dispatchUpdatesTo(this@BaseAdapter)
                    }
                }
            }
        }
    }

    private inline fun onVisibleSite(block: (Int, Int) -> Unit) {
        block.invoke(firstPosition, lastPosition)
    }

    private inline fun isPositionInVisibleSite(
        position: Int, located: () -> Unit
    ) {
        onVisibleSite { first, last ->
            if (position in first..last || (first == -1 && last == -1))
                located()
        }
    }

    private inline fun isPositionInVisibleSite(
        positionStart: Int, itemCount: Int, located: (Int, Int) -> Unit
    ) {
        onVisibleSite { first, last ->
            val lastIndex = positionStart + itemCount
            if ((positionStart in first..last) || (first == -1 && last == -1))
                located(
                    positionStart,
                    if (lastIndex > mList.size) itemCount - (lastIndex - mList.size) else itemCount
                )
        }
    }

    fun notifyItemChangedChecked(position: Int, payload: Any? = null) {
        isPositionInVisibleSite(position) {
            payload?.let { notifyItemChanged(position, payload) } ?: notifyItemChanged(position)
        }
    }

    fun notifyItemInsertedChecked(position: Int) {
        isPositionInVisibleSite(position) {
            notifyItemInserted(position)
        }
    }

    fun notifyItemRemovedChecked(position: Int) {
        isPositionInVisibleSite(position) {
            notifyItemRemoved(position)
        }
    }

    fun notifyItemRangeInsertedChecked(positionStart: Int, itemCount: Int) {
        isPositionInVisibleSite(positionStart, itemCount) { start, count ->
            notifyItemRangeInserted(start, count)
        }
    }

    fun notifyItemRangeRemovedChecked(positionStart: Int, itemCount: Int) {
        isPositionInVisibleSite(positionStart, itemCount) { start, count ->
            notifyItemRangeRemoved(start, count)
        }
    }

    fun notifyItemRangeChangedChecked(positionStart: Int, itemCount: Int, payload: Any? = null) {
        isPositionInVisibleSite(positionStart, itemCount) { start, count ->
            payload?.let { notifyItemRangeChanged(start, count, payload) }
                ?: notifyItemRangeChanged(start, count)
        }
    }


    suspend fun notifyItemChangedCO(position: Int, payload: Any? = null): Unit =
        withMainContext {
            if (payload != null)
                notifyItemChangedChecked(position, payload)
            else notifyItemChangedChecked(position)
        }

    suspend fun notifyItemInsertedCO(position: Int): Unit =
        withMainContext {
            notifyItemInsertedChecked(position)
        }

    suspend fun notifyItemRemovedCO(position: Int): Unit =
        withMainContext {
            notifyItemRemovedChecked(position)
        }

    @SuppressLint("NotifyDataSetChanged")
    suspend fun notifyDataSetChangedCO(): Unit =
        withMainContext {
            notifyDataSetChanged()
        }

    suspend fun notifyItemRangeInsertedCO(positionStart: Int, itemCount: Int): Unit =
        withMainContext {
            notifyItemRangeInsertedChecked(positionStart, itemCount)
        }

    suspend fun notifyItemRangeRemovedCO(positionStart: Int, itemCount: Int): Unit =
        withMainContext {
            notifyItemRangeRemovedChecked(positionStart, itemCount)
        }

    suspend fun notifyItemRangeChangedCO(positionStart: Int, itemCount: Int, payload: Any? = null) =
        withMainContext {
            if (payload == null)
                notifyItemRangeChangedChecked(positionStart, itemCount)
            else notifyItemRangeChangedChecked(positionStart, itemCount, payload)
        }

}