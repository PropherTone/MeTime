package com.protone.component.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.protone.common.baseType.bufferCollect
import com.protone.common.baseType.launchDefault
import com.protone.common.baseType.withMainContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

abstract class BaseListAdapter<D, B : ViewDataBinding, T>(
    val context: Context,
    private val handleEvent: Boolean = false,
    config: AsyncDifferConfig<D>
) : ListAdapter<D, BaseListAdapter.Holder<B>>(config),
    CoroutineScope by CoroutineScope(Dispatchers.Main) {

    open class Holder<B : ViewDataBinding>(val binding: B) : RecyclerView.ViewHolder(binding.root)

    private val _adapterFlow = MutableSharedFlow<T>()
    private val adapterFlow get() = _adapterFlow.asSharedFlow()

    open suspend fun onEventIO(data: T) {}

    var layoutManager: LinearLayoutManager? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        if (recyclerView.layoutManager is LinearLayoutManager) {
            layoutManager = recyclerView.layoutManager as LinearLayoutManager
        }
        super.onAttachedToRecyclerView(recyclerView)
        if (handleEvent) launchDefault {
            adapterFlow.bufferCollect {
                onEventIO(it)
            }
        }
    }

    protected fun emit(value: T) {
        launch {
            _adapterFlow.emit(value)
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        cancel()
    }

    private inline fun LinearLayoutManager.onVisibleSite(block: (Int, Int) -> Unit) {
        block.invoke(findFirstVisibleItemPosition(), findLastVisibleItemPosition())
    }

    private inline fun LinearLayoutManager.isPositionInVisibleSite(
        position: Int, located: () -> Unit
    ) {
        onVisibleSite { first, last ->
            if (position in first..last) located()
        }
    }

    private inline fun LinearLayoutManager.isPositionInVisibleSite(
        positionStart: Int, itemCount: Int, located: () -> Unit
    ) {
        onVisibleSite { first, last ->
            if (positionStart in first..last && positionStart + itemCount <= last) located()
        }
    }

    fun notifyItemChangedChecked(position: Int, payload: Any? = null) {
        layoutManager?.isPositionInVisibleSite(position) {
            payload?.let { notifyItemChanged(position, payload) } ?: notifyItemChanged(position)
        }
    }

    fun notifyItemInsertedChecked(position: Int) {
        layoutManager?.isPositionInVisibleSite(position) {
            notifyItemInserted(position)
        }
    }

    fun notifyItemRemovedChecked(position: Int) {
        layoutManager?.isPositionInVisibleSite(position) {
            notifyItemRemoved(position)
        }
    }

    fun notifyItemRangeInsertedChecked(positionStart: Int, itemCount: Int) {
        layoutManager?.isPositionInVisibleSite(positionStart, itemCount) {
            notifyItemRangeInserted(positionStart, itemCount)
        }
    }

    fun notifyItemRangeRemovedChecked(positionStart: Int, itemCount: Int) {
        layoutManager?.isPositionInVisibleSite(positionStart, itemCount) {
            notifyItemRangeRemoved(positionStart, itemCount)
        }
    }

    fun notifyItemRangeChangedChecked(positionStart: Int, itemCount: Int, payload: Any? = null) {
        layoutManager?.isPositionInVisibleSite(positionStart, itemCount) {
            payload?.let { notifyItemRangeChanged(positionStart, itemCount, payload) }
                ?: notifyItemRangeChanged(positionStart, itemCount)
        }
    }


    suspend fun notifyItemChangedCO(position: Int, payload: Any? = null): Unit = withMainContext {
        if (payload != null)
            notifyItemChangedChecked(position, payload)
        else notifyItemChangedChecked(position)
    }

    suspend fun notifyItemInsertedCO(position: Int): Unit = withMainContext {
        notifyItemInsertedChecked(position)
    }

    suspend fun notifyItemRemovedCO(position: Int): Unit = withMainContext {
        notifyItemRemovedChecked(position)
    }

    @SuppressLint("NotifyDataSetChanged")
    suspend fun notifyDataSetChangedCO(): Unit = withMainContext {
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

    suspend fun notifyItemRangeChangedCO(
        positionStart: Int,
        itemCount: Int,
        payload: Any? = null
    ): Unit =
        withMainContext {
            if (payload == null)
                notifyItemRangeChangedChecked(positionStart, itemCount)
            else notifyItemRangeChangedChecked(positionStart, itemCount, payload)
        }
}