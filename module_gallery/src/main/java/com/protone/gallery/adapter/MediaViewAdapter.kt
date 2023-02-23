package com.protone.gallery.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.protone.common.entity.GalleryMedia
import com.protone.component.view.adapter.BaseAdapter
import com.protone.component.view.customView.videoPlayer.DefaultVideoController
import com.protone.component.view.customView.videoPlayer.VideoBaseController
import com.protone.gallery.databinding.ImageLayoutBinding
import com.protone.gallery.databinding.VideoLayoutBinding

class MediaViewAdapter(context: Context, private val glideLoader: RequestManager) :
    BaseAdapter<GalleryMedia, ViewDataBinding, Any>(context) {

    companion object {
        const val IMAGE = 0
        const val VIDEO = 1
    }

    var onSingleTap: (() -> Unit)? = null

    override fun getItemViewType(position: Int): Int {
        return if (mList[position].isVideo) VIDEO else IMAGE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder<ViewDataBinding> {
        return Holder(
            when (viewType) {
                VIDEO -> VideoLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
                else -> ImageLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
            }
        )
    }

    override fun onBindViewHolder(holder: Holder<ViewDataBinding>, position: Int) {
        when (holder.binding) {
            is ImageLayoutBinding ->
                (holder.binding as ImageLayoutBinding).apply {
                    if (mList[position].name.contains("gif"))
                        glideLoader.load(mList[position].uri).into(image)
                    else image.setImageResource(mList[position].uri)
                    image.onSingleTap = onSingleTap
                }
            is VideoLayoutBinding ->
                (holder.binding as VideoLayoutBinding).apply {
                    player.controller = DefaultVideoController(context)
                    player.setPath(mList[position].uri, glideLoader.asDrawable())
                    player.controller?.setTitle(mList[position].name)
                    player.setOnFocusChangeListener { _, hasFocus ->
                        Log.d("TAG", "onBindViewHolder: $hasFocus")
                        if (hasFocus && player.controller?.state == VideoBaseController.PlayState.PAUSE) {
                            player.controller?.play()
                        }
                    }
                }
        }
    }
}

