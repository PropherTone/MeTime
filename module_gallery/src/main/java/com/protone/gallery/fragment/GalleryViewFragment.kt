package com.protone.gallery.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.protone.common.entity.GalleryMedia
import com.protone.component.databinding.RichVideoLayoutBinding
import com.protone.component.view.customView.LoadingStatesListener
import com.protone.component.view.customView.videoPlayer.DefaultVideoController
import com.protone.component.view.customView.videoPlayer.VideoBaseController
import com.protone.gallery.databinding.GalleryVp2AdapterLayoutBinding

class GalleryViewFragment(
    private val galleryMedia: GalleryMedia,
    private val singleClick: () -> Unit
) : Fragment() {

    private var videoBinding: RichVideoLayoutBinding? = null
    private var imageBinding: GalleryVp2AdapterLayoutBinding? = null
    private val glideLoader by lazy { Glide.with(this).asDrawable() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return if (galleryMedia.isVideo) RichVideoLayoutBinding
            .inflate(inflater, container, false)
            .also { binding ->
                videoBinding = binding.apply {
                    richVideo.controller = DefaultVideoController(richVideo.context)
                    richVideo.setPath(galleryMedia.uri, glideLoader)
                }
            }.root
        else GalleryVp2AdapterLayoutBinding
            .inflate(inflater, container, false)
            .also {
                imageBinding = it
                it.image.onSingleTap = singleClick
            }.root
    }

    override fun onResume() {
        super.onResume()
        if (!galleryMedia.isVideo) {
            if (galleryMedia.name.contains("gif")) {
                imageBinding?.image?.let {
                    Glide.with(this).load(galleryMedia.uri)
                        .addListener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean = false

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                activity?.startPostponedEnterTransition()
                                return true
                            }
                        }).into(it)
                }
            } else {
                imageBinding?.image?.onLoadingStatesListener = LoadingStatesListener {
                    activity?.startPostponedEnterTransition()
                }
                imageBinding?.image?.setImageResource(galleryMedia.uri)
            }
            imageBinding?.image?.locate()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        imageBinding?.image?.clear()
        videoBinding?.richVideo?.release()
    }

    override fun onPause() {
        if (galleryMedia.isVideo) videoBinding?.richVideo?.controller?.pause()
        super.onPause()
    }
}