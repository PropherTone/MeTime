package com.protone.gallery.adapter

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
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
import com.protone.common.entity.GalleryBucket
import com.protone.common.utils.ALL_GALLERY
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
                if (data.bucket.name != ALL_GALLERY &&
                    data.bucket.size <= 0
                ) {
                    mList.find { data.bucket.name == it.name }
                        ?.let { deleteBucket(it) }
                    return
                }
                val iterator = mList.iterator()
                var index = 0
                while (iterator.hasNext()) {
                    if (iterator.next().name == data.bucket.name) {
                        if (selectList.size > 0) {
                            if (selectList[0].name == data.bucket.name) {
                                selectList[0] = data.bucket
                            }
                        }
                        mList[index] = data.bucket
                        notifyItemChangedCO(index)
                        break
                    }
                    index++
                }
            }
            is GalleryBucketEvent.InsertBucket -> {
                mList.add(data.bucket)
                notifyItemInsertedCO(mList.size)
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
                bucketThumb.let { thumb ->
                    if (thumb.tag != data.uri) {
                        thumb.tag = data.uri
                        Image.load(data.uri).with(context).into(thumb)
                    }
                }
                bucketName.text = data.name
                bucketItemNumber.text = data.size.toString()
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
        notifyItemChanged(position, (SELECT))
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