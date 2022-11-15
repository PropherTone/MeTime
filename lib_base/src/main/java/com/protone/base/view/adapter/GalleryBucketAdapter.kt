package com.protone.base.view.adapter

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.Glide
import com.protone.base.R
import com.protone.base.databinding.GalleryBucketListLayoutBinding
import com.protone.common.baseType.getString
import com.protone.common.context.MApplication
import com.protone.common.context.newLayoutInflater

class GalleryBucketAdapter(
    context: Context,
    private val galleryBucketAdapterDataProxy: GalleryBucketAdapterDataProxy,
    private val selectBucket: (String) -> Unit
) : SelectListAdapter<GalleryBucketListLayoutBinding, Pair<Uri, Array<String>>, GalleryBucketAdapter.GalleryBucketEvent>(
    context, true
) {

    sealed class GalleryBucketEvent {
        data class DeleteBucket(val bucket: Pair<Uri, Array<String>>) : GalleryBucketEvent()
        data class RefreshBucket(val bucket: Pair<Uri, Array<String>>) : GalleryBucketEvent()
        data class InsertBucket(val bucket: Pair<Uri, Array<String>>) : GalleryBucketEvent()
    }

    private var galleries: MutableList<Pair<Uri, Array<String>>> = mutableListOf()

    override suspend fun onEventIO(data: GalleryBucketEvent) {
        when (data) {
            is GalleryBucketEvent.DeleteBucket -> {
                galleries.find { it.second[0] == data.bucket.second[0] }?.let {
                    val index = galleries.indexOf(it)
                    galleries.removeAt(index)
                    selectList.remove(it)
                    notifyItemRemovedCO(index)
                }
            }
            is GalleryBucketEvent.RefreshBucket -> {
                if (data.bucket.second[0] != R.string.all_gallery.getString() &&
                    data.bucket.second[1].toInt() <= 0
                ) {
                    galleries.find { data.bucket.second[0] == it.second[0] }
                        ?.let { deleteBucket(it) }
                    return
                }
                val iterator = galleries.iterator()
                var index = 0
                while (iterator.hasNext()) {
                    if (iterator.next().second[0] == data.bucket.second[0]) {
                        if (selectList.size > 0) {
                            if (selectList[0].second[0] == data.bucket.second[0]) {
                                selectList[0] = data.bucket
                            }
                        }
                        galleries[index] = data.bucket
                        notifyItemChangedCO(index)
                        break
                    }
                    index++
                }
            }
            is GalleryBucketEvent.InsertBucket -> {
                galleries.add(data.bucket)
                notifyItemInsertedCO(galleries.size)
            }
        }
    }

    override val select: (holder: Holder<GalleryBucketListLayoutBinding>, isSelect: Boolean) -> Unit =
        { holder, isSelect ->
            holder.binding.bucketCheck.apply {
                isChecked = isSelect
                isVisible = isSelect
            }
        }

    override fun itemIndex(path: Pair<Uri, Array<String>>): Int {
        return galleries.indexOf(path)
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
        galleries[position].let { data ->
            setSelect(holder, data in selectList)
            holder.binding.apply {
                root.setOnLongClickListener {
                    AlertDialog.Builder(context)
                        .setTitle(R.string.delete.getString())
                        .setPositiveButton(
                            R.string.confirm
                        ) { dialog, _ ->
                            galleryBucketAdapterDataProxy.deleteGalleryBucket(galleries[position].second[0])
                            deleteBucket(galleries[position])
                            dialog.dismiss()
                        }.setNegativeButton(R.string.cancel) { dialog, _ ->
                            dialog.dismiss()
                        }.create().show()
                    false
                }
                bucketThumb.let { thumb ->
                    if (thumb.tag != data.first) {
                        thumb.tag = data.first
                        Glide.with(context).load(data.first)
                            .into(thumb)
                    }
                }
                data.second.also { sec ->
                    bucketName.text = sec[0]
                    bucketItemNumber.text = sec[1]
                    bucket.setOnClickListener {
                        checkSelect(holder, data)
                        selectBucket(sec[0])
                    }
                }
            }
        }
    }

    override fun checkSelect(
        holder: Holder<GalleryBucketListLayoutBinding>,
        item: Pair<Uri, Array<String>>
    ) {
        if (!multiChoose) clearSelected()
        selectList.add(item)
        setSelect(holder, true)
    }

    private fun deleteBucket(bucket: Pair<Uri, Array<String>>) {
        emit(GalleryBucketEvent.DeleteBucket(bucket))
    }

    fun refreshBucket(item: Pair<Uri, Array<String>>) {
        emit(GalleryBucketEvent.RefreshBucket(item))
    }

    fun insertBucket(item: Pair<Uri, Array<String>>) {
        emit(GalleryBucketEvent.InsertBucket(item))
    }

    override fun getItemCount(): Int {
        return galleries.size
    }

    interface GalleryBucketAdapterDataProxy {
        fun deleteGalleryBucket(bucket: String)
    }
}