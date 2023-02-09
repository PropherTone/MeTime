package com.protone.gallery.adapter

import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.RequestBuilder
import com.protone.common.baseType.getString
import com.protone.common.context.newLayoutInflater
import com.protone.common.entity.Gallery
import com.protone.common.entity.Gallery.ItemState
import com.protone.component.R
import com.protone.component.view.adapter.SelectListAdapter
import com.protone.gallery.databinding.GalleryBucketListLayoutBinding

class GalleryBucketAdapter(
    context: Context,
    private val glideLoader: RequestBuilder<Drawable>,
    block: GalleryBucketAdapterDataProxy.() -> Unit
) : SelectListAdapter<GalleryBucketListLayoutBinding, Gallery, GalleryBucketAdapter.GalleryBucketEvent>(
    context, true
) {

    init {
        GalleryBucketAdapterDataProxy().block()
    }

    sealed class GalleryBucketEvent {
        data class DeleteBucket(val bucket: Gallery) : GalleryBucketEvent()
        data class RefreshBucket(val bucket: Gallery, val itemState: ItemState) :
            GalleryBucketEvent()

        data class InsertBucket(val bucket: Gallery) : GalleryBucketEvent()
        data class InsertBuckets(val buckets: List<Gallery>) : GalleryBucketEvent()
        data class SetSelected(val gallery: Gallery) : GalleryBucketEvent()
    }

    override suspend fun handleEventAsynchronous(data: GalleryBucketEvent) {
        when (data) {
            is GalleryBucketEvent.DeleteBucket -> {
                mList.find { it.name == data.bucket.name }?.let {
                    val index = mList.indexOf(it)
                    mList.removeAt(index)
                    selectList.remove(it)
                    notifyItemRemovedCO(index)
                }
            }
            is GalleryBucketEvent.RefreshBucket -> {
                mList.indexOfFirst { it.name == data.bucket.name }.let {
                    if (it != -1) notifyItemChangedCO(it, data.itemState)
                }
            }
            is GalleryBucketEvent.InsertBucket -> {
                mList.add(data.bucket)
                notifyItemInsertedCO(mList.size)
            }
            is GalleryBucketEvent.InsertBuckets -> {
                mList.addAll(data.buckets)
                notifyItemRangeInsertedCO(mList.size - data.buckets.size, data.buckets.size)
            }
            is GalleryBucketEvent.SetSelected -> {
                mList.indexOfFirst {
                    it.name == data.gallery.name
                }.takeIf {
                    it != -1
                }?.let {
                    val contains = selectList.contains(mList[it])
                    selectList.clear()
                    selectList.add(mList[it])
                    notifyItemChangedCO(it)
                    if (contains) selectBucket?.invoke(mList[it])
                } ?: selectList.add(data.gallery)
            }
        }
    }

    override fun setSelect(
        content: GalleryBucketListLayoutBinding,
        position: Int,
        isSelect: Boolean
    ) {
        content.bucket.check = isSelect
    }

    override fun itemIndex(path: Gallery): Int {
        return mList.indexOf(path)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Holder<GalleryBucketListLayoutBinding> {
        return Holder(
            GalleryBucketListLayoutBinding.inflate(context.newLayoutInflater, parent, false).apply {
                bucket.thumb.scaleType = ImageView.ScaleType.CENTER_CROP
            })
    }

    private fun GalleryBucketListLayoutBinding.changedText(item: Gallery) {
        bucket.size.text = item.size.toString()
    }

    private fun GalleryBucketListLayoutBinding.changedUri(item: Gallery) {
        if (bucket.thumb.tag != item.uri) {
            bucket.thumb.tag = item.uri
            glideLoader.load(item.uri).into(bucket.thumb)
        }
    }

    override fun onBindViewHolder(
        holder: Holder<GalleryBucketListLayoutBinding>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            val gallery = mList[position]
            when (payloads.first()) {
                ItemState.SIZE_CHANGED -> holder.binding.apply {
                    changedText(gallery)
                }
                ItemState.URI_CHANGED -> holder.binding.apply {
                    changedUri(gallery)
                }
                ItemState.ALL_CHANGED -> holder.binding.apply {
                    changedText(gallery)
                    changedUri(gallery)
                }
            }
        }
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun onBindViewHolder(holder: Holder<GalleryBucketListLayoutBinding>, position: Int) {
        mList[position].let { data ->
            setSelect(holder.binding, position, data in selectList)
            holder.binding.apply {
                root.setOnLongClickListener {
                    AlertDialog.Builder(context)
                        .setTitle(R.string.delete.getString())
                        .setPositiveButton(
                            R.string.confirm
                        ) { dialog, _ ->
                            deleteGalleryBucket?.invoke(mList[position].name)
                            deleteBucket(mList[position])
                            dialog.dismiss()
                        }.setNegativeButton(R.string.cancel) { dialog, _ ->
                            dialog.dismiss()
                        }.create().show()
                    false
                }
                bucket.name.text = data.name
                changedText(data)
                changedUri(data)
                bucket.setOnClickListener {
                    if (selectList.contains(data)) return@setOnClickListener
                    checkSelect(position, data)
                    selectBucket?.invoke(data)
                }
            }
        }
    }

    override fun checkSelect(
        position: Int, item: Gallery
    ) {
        if (!multiChoose) clearSelected()
        selectList.add(item)
        refreshVisiblePosition()
        notifyItemChangedChecked(position, (SELECT))
    }

    override fun setData(collection: Collection<Gallery>) {
        if (collection.isEmpty()) return
        notifyItemRangeRemoved(0, mList.size)
        super.setData(collection)
        selectList.clear()
        notifyItemRangeInserted(0, mList.size)
    }

    override fun onFailedToRecycleView(holder: Holder<GalleryBucketListLayoutBinding>): Boolean {
        holder.binding.bucket.stopAnimate()
        return true
    }

    fun setSelected(gallery: Gallery) {
        emit(GalleryBucketEvent.SetSelected(gallery))
    }

    fun deleteBucket(bucket: Gallery) {
        emit(GalleryBucketEvent.DeleteBucket(bucket))
    }

    fun refreshBucket(item: Gallery, itemState: ItemState) {
        emit(GalleryBucketEvent.RefreshBucket(item, itemState))
    }

    fun insertBucket(item: Gallery) {
        emit(GalleryBucketEvent.InsertBucket(item))
    }

    fun insertBucket(items: List<Gallery>) {
        emit(GalleryBucketEvent.InsertBuckets(items))
    }

    private var deleteGalleryBucket: ((String) -> Unit)? = null
    private var selectBucket: ((Gallery) -> Unit)? = null

    inner class GalleryBucketAdapterDataProxy {

        fun deleteGalleryBucket(block: (String) -> Unit) {
            deleteGalleryBucket = block
        }

        fun selectBucket(block: (Gallery) -> Unit) {
            selectBucket = block
        }
    }
}