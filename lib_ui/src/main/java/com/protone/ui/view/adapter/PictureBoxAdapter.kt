package com.protone.ui.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isGone
import androidx.core.view.updateLayoutParams
import androidx.databinding.ViewDataBinding
import com.protone.ui.databinding.PictureBoxAdapterGifLayoutBinding
import com.protone.ui.databinding.PictureBoxAdapterLayoutBinding
import com.protone.ui.databinding.PictureBoxAdapterVideoLayoutBinding
import com.protone.common.entity.GalleryMedia
import com.protone.common.utils.displayUtils.imageLoader.Image
import com.protone.common.utils.displayUtils.imageLoader.LoadSuccessResult
import com.protone.common.utils.displayUtils.imageLoader.RequestInterceptor
import kotlin.math.roundToInt

class PictureBoxAdapter(context: Context, private val picUri: MutableList<GalleryMedia>) :
    BaseAdapter<ViewDataBinding, Any>(context) {

    private val image = 0
    private val video = 1
    private val gif = 3

    override fun getItemViewType(position: Int): Int {
        return if (picUri[position].name.contains("gif")) gif else if (picUri[position].isVideo) video else image
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
            is PictureBoxAdapterGifLayoutBinding -> holder.binding.apply {
                image.scaleType = ImageView.ScaleType.FIT_XY
                loadingMedia(position, image)
            }
            is PictureBoxAdapterLayoutBinding -> holder.binding.apply {
                image.setImageResource(picUri[position].uri)
            }
            is PictureBoxAdapterVideoLayoutBinding -> holder.binding.apply {
                start.isGone = false
                videoCover.isGone = false
                loadingMedia(position, videoCover)
                start.setOnClickListener {
                    start.isGone = true
                    videoCover.isGone = true
                    videoPlayer.setVideoPath(picUri[holder.layoutPosition].uri)
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
            is PictureBoxAdapterLayoutBinding -> holder.binding.apply {
                image.clear()
            }
            is PictureBoxAdapterVideoLayoutBinding -> holder.binding.apply {
                start.isGone = false
                videoCover.isGone = false
                videoPlayer.release()
            }
        }
        super.onViewRecycled(holder)
    }


    override fun getItemCount(): Int {
        return picUri.size
    }

    private fun loadingMedia(position: Int, view: ImageView) {
        Image.load(picUri[position].path).setInterceptor(object : RequestInterceptor() {
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
        }).into(context, view)
    }
}