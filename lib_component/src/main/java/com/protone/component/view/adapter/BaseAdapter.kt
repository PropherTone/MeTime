package com.protone.component.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.protone.common.baseType.bufferCollect
import com.protone.common.baseType.launchDefault
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

abstract class BaseAdapter<B : ViewDataBinding, T>(
    val context: Context,
    private val handleEvent: Boolean = false
) : RecyclerView.Adapter<BaseAdapter.Holder<B>>(),
    CoroutineScope by CoroutineScope(Dispatchers.Main) {

    open class Holder<B : ViewDataBinding>(val binding: B) : RecyclerView.ViewHolder(binding.root)

    private val _adapterFlow = MutableSharedFlow<T>()
    private val adapterFlow get() = _adapterFlow.asSharedFlow()

    open suspend fun onEventIO(data: T) {}

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
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

    suspend fun notifyItemChangedCO(position: Int): Unit = withContext(Dispatchers.Main) {
        notifyItemChanged(position)
    }

    suspend fun notifyItemInsertedCO(position: Int): Unit = withContext(Dispatchers.Main) {
        notifyItemInserted(position)
    }

    suspend fun notifyItemRemovedCO(position: Int): Unit = withContext(Dispatchers.Main) {
        notifyItemRemoved(position)
    }

    @SuppressLint("NotifyDataSetChanged")
    suspend fun notifyDataSetChangedCO(): Unit = withContext(Dispatchers.Main) {
        notifyDataSetChanged()
    }

    suspend fun notifyItemRangeInsertedCO(positionStart: Int, itemCount: Int): Unit =
        withContext(Dispatchers.Main) {
            notifyItemRangeInserted(positionStart, itemCount)
        }

    suspend fun notifyItemRangeRemovedCO(positionStart: Int, itemCount: Int): Unit =
        withContext(Dispatchers.Main) {
            notifyItemRangeRemoved(positionStart, itemCount)
        }

    suspend fun notifyItemRangeChangedCO(positionStart: Int, itemCount: Int): Unit =
        withContext(Dispatchers.Main) {
            notifyItemRangeChanged(positionStart, itemCount)
        }

    suspend fun notifyItemRangeChangedCO(positionStart: Int, itemCount: Int, payload: Any?): Unit =
        withContext(Dispatchers.Main) {
            notifyItemRangeChanged(positionStart, itemCount, payload)
        }
}