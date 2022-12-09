package com.protone.component.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
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

    open fun setData(collection: Collection<Item>) {
        mList.clear()
        mList.addAll(collection)
    }

    interface AdapterDiff<Item : Any> {
        fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean
        fun getChangePayload(oldItem: Item, newItem: Item): Boolean
    }

    private var diff: AdapterDiff<Item>? = null
    fun setAdapterDiff(adapterDiff: AdapterDiff<Item>) {
        diff = adapterDiff
    }

    open suspend fun handleEventAsynchronous(data: Event) {}

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        if (handleEvent) launchDefault {
            adapterFlow.bufferCollect {
                handleEventAsynchronous(it)
            }
        }
    }

    protected fun emit(value: Event) {
        launch {
            _adapterFlow.emit(value)
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
                }).apply {
                    setData(collection)
                    withMainContext {
                        dispatchUpdatesTo(this@BaseAdapter)
                    }
                }
            }
        }
    }

    suspend fun notifyItemChangedCO(position: Int, payload: Any? = null): Unit = withMainContext {
        if (payload != null)
            notifyItemChanged(position, payload)
        else notifyItemChanged(position)
    }

    suspend fun notifyItemInsertedCO(position: Int): Unit = withMainContext {
        notifyItemInserted(position)
    }

    suspend fun notifyItemRemovedCO(position: Int): Unit = withMainContext {
        notifyItemRemoved(position)
    }

    @SuppressLint("NotifyDataSetChanged")
    suspend fun notifyDataSetChangedCO(): Unit = withMainContext {
        notifyDataSetChanged()
    }

    suspend fun notifyItemRangeInsertedCO(positionStart: Int, itemCount: Int): Unit =
        withMainContext {
            notifyItemRangeInserted(positionStart, itemCount)
        }

    suspend fun notifyItemRangeRemovedCO(positionStart: Int, itemCount: Int): Unit =
        withMainContext {
            notifyItemRangeRemoved(positionStart, itemCount)
        }

    suspend fun notifyItemRangeChangedCO(
        positionStart: Int,
        itemCount: Int,
        payload: Any? = null
    ): Unit =
        withMainContext {
            if (payload == null)
                notifyItemRangeChanged(positionStart, itemCount)
            else notifyItemRangeChanged(positionStart, itemCount)
        }

}