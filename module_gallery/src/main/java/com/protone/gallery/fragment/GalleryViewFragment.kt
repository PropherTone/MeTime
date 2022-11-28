package com.protone.gallery.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.protone.common.entity.GalleryMedia
import com.protone.common.utils.displayUtils.imageLoader.Image
import com.protone.component.databinding.RichVideoLayoutBinding
import com.protone.gallery.databinding.GalleryVp2AdapterLayoutBinding

class GalleryViewFragment(
    private val galleryMedia: GalleryMedia,
    private val singleClick: () -> Unit
) : Fragment() {

    private var videoBinding: RichVideoLayoutBinding? = null
    private var imageBinding: GalleryVp2AdapterLayoutBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return if (galleryMedia.isVideo) {
            RichVideoLayoutBinding.inflate(inflater, container, false).also {
                videoBinding = it
                it.richVideo.setVideoPath(galleryMedia.uri)
                it.richVideo.title = galleryMedia.name
                it.root.setOnClickListener {
                    singleClick.invoke()
                }
            }.root
        } else {
            GalleryVp2AdapterLayoutBinding.inflate(inflater, container, false).also {
                imageBinding = it
                it.image.onSingleTap = singleClick
            }.root
        }
    }

    override fun onResume() {
        super.onResume()
        if (!galleryMedia.isVideo) {
            if (galleryMedia.name.contains("gif")) {
                imageBinding?.image?.let { Image.load(galleryMedia.uri).with(this).into(it) }
            } else {
                imageBinding?.image?.setImageResource(galleryMedia.uri)
            }
            imageBinding?.image?.locate()
        } else {
            videoBinding?.richVideo?.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (galleryMedia.isVideo) videoBinding?.richVideo?.release()
        else imageBinding?.image?.clear()
    }

    override fun onPause() {
        super.onPause()
        if (galleryMedia.isVideo) videoBinding?.richVideo?.pause()
    }
}