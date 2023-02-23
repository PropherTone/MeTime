package com.protone.gallery.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.updateLayoutParams
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.protone.common.entity.GalleryMedia
import com.protone.component.view.adapter.BaseAdapter
import com.protone.component.view.customView.videoPlayer.DefaultVideoController
import com.protone.gallery.databinding.PictureBoxAdapterGifLayoutBinding
import com.protone.gallery.databinding.PictureBoxAdapterLayoutBinding
import com.protone.gallery.databinding.PictureBoxAdapterVideoLayoutBinding
import kotlin.math.roundToInt

class PictureBoxAdapter(
    context: Context,
    private val glideLoader: RequestBuilder<Drawable>,
    picUri: MutableList<GalleryMedia>
) : BaseAdapter<GalleryMedia, ViewDataBinding, Any>(context) {

    init {
        mList.addAll(picUri)
    }

    companion object {
        const val IMAGE = 0
        const val VIDEO = 1
        const val GIF = 3
    }

    override fun getItemViewType(position: Int): Int {
        return if (mList[position].name.contains("gif")) GIF else if (mList[position].isVideo) VIDEO else IMAGE
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Holder<ViewDataBinding> {
        val binding: ViewDataBinding = when (viewType) {
            VIDEO -> PictureBoxAdapterVideoLayoutBinding
                .inflate(LayoutInflater.from(context), parent, false)
            GIF -> PictureBoxAdapterGifLayoutBinding
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
                    glideLoader.load(mList[position].path).into(image)
                }
            is PictureBoxAdapterLayoutBinding ->
                (holder.binding as PictureBoxAdapterLayoutBinding).apply {
                    image.setImageResource(mList[position].uri)
                }
            is PictureBoxAdapterVideoLayoutBinding ->
                (holder.binding as PictureBoxAdapterVideoLayoutBinding).apply {
                    videoPlayer.controller = DefaultVideoController(context)
                    videoPlayer.setPath(mList[position].uri, glideLoader)
                    videoPlayer.controller?.setTitle(mList[position].name)
                }
        }
    }

    override fun onFailedToRecycleView(holder: Holder<ViewDataBinding>): Boolean {
        release(holder)
        return true
    }

    override fun onViewRecycled(holder: Holder<ViewDataBinding>) {
        release(holder)
        super.onViewRecycled(holder)
    }

    private fun release(holder: Holder<ViewDataBinding>) {
        when (holder.binding) {
            is PictureBoxAdapterLayoutBinding ->
                (holder.binding as PictureBoxAdapterLayoutBinding).image.clear()
            is PictureBoxAdapterVideoLayoutBinding ->
                (holder.binding as PictureBoxAdapterVideoLayoutBinding).videoPlayer.release()
        }
    }

}