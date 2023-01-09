package com.protone.gallery.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isGone
import androidx.core.view.updateLayoutParams
import androidx.databinding.ViewDataBinding
import com.protone.common.entity.GalleryMedia
import com.protone.common.utils.displayUtils.imageLoader.Image
import com.protone.common.utils.displayUtils.imageLoader.LoadSuccessResult
import com.protone.common.utils.displayUtils.imageLoader.RequestInterceptor
import com.protone.component.view.adapter.BaseAdapter
import com.protone.gallery.databinding.PictureBoxAdapterGifLayoutBinding
import com.protone.gallery.databinding.PictureBoxAdapterLayoutBinding
import com.protone.gallery.databinding.PictureBoxAdapterVideoLayoutBinding
import kotlin.math.roundToInt

class PictureBoxAdapter(context: Context, picUri: MutableList<GalleryMedia>) :
    BaseAdapter<GalleryMedia, ViewDataBinding, Any>(context) {

    init {
        mList.addAll(picUri)
    }

    private val image = 0
    private val video = 1
    private val gif = 3

    override fun getItemViewType(position: Int): Int {
        return if (mList[position].name.contains("gif")) gif else if (mList[position].isVideo) video else image
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Holder<ViewDataBinding> {
        val binding: ViewDataBinding = when (viewType) {
            video -> PictureBoxAdapterVideoLayoutBinding
                .inflate(LayoutInflater.from(context), parent, false)
            gif -> PictureBoxAdapterGifLayoutBinding
                .inflate(LayoutInflater.from(context), parent, false)
            else -> PictureBoxAdapterLayoutBinding
                .inflate(LayoutInflater.from(context), parent, false)
        }
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder<ViewDataBinding>, position: Int) {
        when (holder.binding) {
            is PictureBoxAdapterGifLayoutBinding ->
                (holder.binding as PictureBoxAdapterGifLayoutBinding).apply {
                    image.scaleType = ImageView.ScaleType.FIT_XY
                    loadingMedia(position, image)
                }
            is PictureBoxAdapterLayoutBinding ->
                (holder.binding as PictureBoxAdapterLayoutBinding).apply {
                    image.setImageResource(mList[position].uri)
                }
            is PictureBoxAdapterVideoLayoutBinding ->
                (holder.binding as PictureBoxAdapterVideoLayoutBinding).apply {
                    start.isGone = false
                    videoCover.isGone = false
                    loadingMedia(position, videoCover)
                    start.setOnClickListener {
                        start.isGone = true
                        videoCover.isGone = true
                        videoPlayer.setVideoPath(mList[holder.layoutPosition].uri)
                    }
                    videoPlayer.doOnCompletion {
                        videoPlayer.release()
                        start.isGone = false
                        videoCover.isGone = false
                    }
                }
        }
    }

    override fun onViewRecycled(holder: Holder<ViewDataBinding>) {
        when (holder.binding) {
            is PictureBoxAdapterLayoutBinding ->
                (holder.binding as PictureBoxAdapterLayoutBinding).apply {
                    image.clear()
                }
            is PictureBoxAdapterVideoLayoutBinding ->
                (holder.binding as PictureBoxAdapterVideoLayoutBinding).apply {
                    start.isGone = false
                    videoCover.isGone = false
                    videoPlayer.release()
                }
        }
        super.onViewRecycled(holder)
    }

    private fun loadingMedia(position: Int, view: ImageView) {
        Image.load(mList[position].path).with(context)
            .setInterceptor(object : RequestInterceptor() {
                override fun onLoadSuccess(result: LoadSuccessResult) {
                    result.resource?.apply {
                        val mix = this.intrinsicWidth.toFloat().let {
                            view.width / it
                        }
                        val heightSpan = (this.intrinsicHeight * mix).roundToInt()
                        view.updateLayoutParams {
                            this.height = heightSpan
                        }
                        view.updateLayoutParams {
                            this.height = heightSpan
                        }
                    }
                }
            }).into(view)
    }
}