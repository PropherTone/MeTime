package com.protone.gallery.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.protone.common.entity.GalleryMedia
import com.protone.common.utils.displayUtils.imageLoader.Image
import com.protone.common.utils.displayUtils.imageLoader.LoadSuccessResult
import com.protone.common.utils.displayUtils.imageLoader.RequestInterceptor
import com.protone.component.databinding.RichVideoLayoutBinding
import com.protone.component.view.customView.LoadingStatesListener
import com.protone.component.view.customView.videoPlayer.DefaultVideoController
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
        return if (galleryMedia.isVideo) RichVideoLayoutBinding
            .inflate(inflater, container, false)
            .also { binding ->
                videoBinding = binding.apply {
                    richVideo.controller = DefaultVideoController(richVideo.context)
                    richVideo.setPath(galleryMedia.uri)
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
                    Image.load(galleryMedia.uri).with(this)
                        .setInterceptor(object : RequestInterceptor() {
                            override fun onLoadSuccess(result: LoadSuccessResult) {
                                super.onLoadSuccess(result)
                                activity?.startPostponedEnterTransition()
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
        } else {
//            videoBinding?.richVideo?.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        imageBinding?.image?.clear()
    }

    override fun onPause() {
        super.onPause()
        if (galleryMedia.isVideo) videoBinding?.richVideo?.controller
    }
}