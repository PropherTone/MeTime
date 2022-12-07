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
import com.protone.common.entity.GalleryBucket
import com.protone.common.utils.ALL_GALLERY
import com.protone.component.view.adapter.SelectListAdapter
import com.protone.gallery.databinding.GalleryBucketListLayoutBinding

class GalleryBucketAdapter(
    context: Context,
    block: GalleryBucketAdapterDataProxy.() -> Unit
) : SelectListAdapter<GalleryBucketListLayoutBinding, Pair<Uri, Array<String>>, GalleryBucketAdapter.GalleryBucketEvent>(
    context, true
) {

    init {
        GalleryBucketAdapterDataProxy().block()
    }

    sealed class GalleryBucketEvent {
        data class DeleteBucket(val bucket: Pair<Uri, Array<String>>) : GalleryBucketEvent()
        data class RefreshBucket(val bucket: Pair<Uri, Array<String>>) : GalleryBucketEvent()
        data class InsertBucket(val bucket: Pair<Uri, Array<String>>) : GalleryBucketEvent()
    }

    override suspend fun onEventIO(data: GalleryBucketEvent) {
        when (data) {
            is GalleryBucketEvent.DeleteBucket -> {
                mList.find { it.second[0] == data.bucket.second[0] }?.let {
                    val index = mList.indexOf(it)
                    mList.removeAt(index)
                    selectList.remove(it)
                    notifyItemRemovedCO(index)
                }
            }
            is GalleryBucketEvent.RefreshBucket -> {
                if (data.bucket.second[0] != ALL_GALLERY &&
                    data.bucket.second[1].toInt() <= 0
                ) {
                    mList.find { data.bucket.second[0] == it.second[0] }
                        ?.let { deleteBucket(it) }
                    return
                }
                val iterator = mList.iterator()
                var index = 0
                while (iterator.hasNext()) {
                    if (iterator.next().second[0] == data.bucket.second[0]) {
                        if (selectList.size > 0) {
                            if (selectList[0].second[0] == data.bucket.second[0]) {
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

    override fun itemIndex(path: Pair<Uri, Array<String>>): Int {
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
                            deleteGalleryBucket?.invoke(mList[position].second[0])
                            deleteBucket(mList[position])
                            dialog.dismiss()
                        }.setNegativeButton(R.string.cancel) { dialog, _ ->
                            dialog.dismiss()
                        }.create().show()
                    false
                }
                bucketThumb.let { thumb ->
                    if (thumb.tag != data.first) {
                        thumb.tag = data.first
                        Image.load(data.first).with(context).into(thumb)
                    }
                }
                data.second.also { sec ->
                    bucketName.text = sec[0]
                    bucketItemNumber.text = sec[1]
                    bucket.setOnClickListener {
                        checkSelect(position, data)
                        selectBucket?.invoke(sec[0])
                    }
                }
            }
        }
    }

    override fun checkSelect(
        position: Int, item: Pair<Uri, Array<String>>
    ) {
        if (!multiChoose) clearSelected()
        selectList.add(item)
        notifyItemChanged(position, (SELECT))
    }

    fun deleteBucket(bucket: Pair<Uri, Array<String>>) {
        emit(GalleryBucketEvent.DeleteBucket(bucket))
    }

    fun refreshBucket(item: Pair<Uri, Array<String>>) {
        emit(GalleryBucketEvent.RefreshBucket(item))
    }

    fun insertBucket(item: Pair<Uri, Array<String>>) {
        emit(GalleryBucketEvent.InsertBucket(item))
    }

    private var deleteGalleryBucket: ((String) -> Unit)? = null
    private var selectBucket: ((String) -> Unit)? = null

    inner class GalleryBucketAdapterDataProxy {

        fun deleteGalleryBucket(block: (String) -> Unit) {
            deleteGalleryBucket = block
        }

        fun selectBucket(block: (String) -> Unit) {
            selectBucket = block
        }
    }
}