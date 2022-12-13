package com.protone.gallery.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.protone.common.baseType.getString
import com.protone.common.context.MApplication
import com.protone.common.context.newLayoutInflater
import com.protone.common.utils.displayUtils.imageLoader.Image
import com.protone.common.R
import com.protone.common.entity.Gallery
import com.protone.component.view.adapter.SelectListAdapter
import com.protone.gallery.databinding.GalleryBucketListLayoutBinding

class GalleryBucketAdapter(
    context: Context,
    block: GalleryBucketAdapterDataProxy.() -> Unit
) : SelectListAdapter<GalleryBucketListLayoutBinding, Gallery, GalleryBucketAdapter.GalleryBucketEvent>(
    context, true
) {

    init {
        GalleryBucketAdapterDataProxy().block()
    }

    sealed class GalleryBucketEvent {
        data class DeleteBucket(val bucket: Gallery) : GalleryBucketEvent()
        data class RefreshBucket(val bucket: Gallery) : GalleryBucketEvent()
        data class InsertBucket(val bucket: Gallery) : GalleryBucketEvent()
        data class InsertBuckets(val buckets: List<Gallery>) : GalleryBucketEvent()
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
                    if (it != -1) notifyItemChangedCO(it, data.bucket.itemState)
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
        }
    }

    override val select: (GalleryBucketListLayoutBinding, Int, isSelect: Boolean) -> Unit =
        { binding, _, isSelect ->
            binding.bucketCheck.apply {
                isChecked = isSelect
                isVisible = isSelect
            }
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
                root.updateLayoutParams { height = MApplication.screenHeight / 10 }
                bucketThumb.scaleType = ImageView.ScaleType.CENTER_CROP
            })
    }

    private fun GalleryBucketListLayoutBinding.changedText(item: Gallery) {
        bucketItemNumber.text = item.size.toString()
    }

    private fun GalleryBucketListLayoutBinding.changedUri(item: Gallery) {
        if (bucketThumb.tag != item.uri) {
            bucketThumb.tag = item.uri
            Image.load(item.uri).with(context).into(bucketThumb)
        }
    }

    override fun onBindViewHolder(
        holder: Holder<GalleryBucketListLayoutBinding>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            when (payloads.first()) {
                Gallery.ItemState.SIZE_CHANGED -> holder.binding.apply {
                    changedText(mList[position])
                }
                Gallery.ItemState.URI_CHANGED -> holder.binding.apply {
                    changedUri(mList[position])
                }
                Gallery.ItemState.ALL_CHANGED -> holder.binding.apply {
                    changedText(mList[position])
                    changedUri(mList[position])
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
                bucketName.text = data.name
                changedText(data)
                changedUri(data)
                bucket.setOnClickListener {
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
        notifyItemChangedChecked(position, (SELECT))
    }

    fun deleteBucket(bucket: Gallery) {
        emit(GalleryBucketEvent.DeleteBucket(bucket))
    }

    fun refreshBucket(item: Gallery) {
        emit(GalleryBucketEvent.RefreshBucket(item))
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