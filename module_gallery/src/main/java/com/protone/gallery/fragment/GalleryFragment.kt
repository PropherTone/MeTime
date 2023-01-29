package com.protone.gallery.fragment

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.protone.common.R
import com.protone.common.baseType.bufferCollect
import com.protone.common.baseType.getString
import com.protone.common.baseType.launchDefault
import com.protone.common.baseType.toast
import com.protone.common.context.intent
import com.protone.common.context.onGlobalLayout
import com.protone.common.entity.Gallery
import com.protone.common.entity.GalleryMedia
import com.protone.common.utils.ALL_GALLERY
import com.protone.common.utils.IntentDataHolder
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryViewWire.GALLERY
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryViewWire.IS_VIDEO
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryViewWire.MEDIA
import com.protone.common.utils.displayUtils.AnimationHelper
import com.protone.common.utils.json.toJson
import com.protone.component.view.customView.StatusImageView
import com.protone.component.view.dialog.titleDialog
import com.protone.gallery.activity.GallerySearchActivity
import com.protone.gallery.activity.GalleryViewActivity
import com.protone.gallery.activity.PictureBoxActivity
import com.protone.gallery.adapter.GalleryBucketAdapter
import com.protone.gallery.adapter.GalleryListAdapter
import com.protone.gallery.component.GalleryItemDecoration
import com.protone.gallery.databinding.GalleryFragmentLayoutBinding
import com.protone.gallery.viewModel.GalleryFragmentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class GalleryFragment : Fragment(), CoroutineScope by MainScope(),
    GalleryListAdapter.OnSelect {

    private lateinit var viewModel: GalleryFragmentViewModel

    private lateinit var binding: GalleryFragmentLayoutBinding

    private var onSelectMod = false
        set(value) {
            launch {
                binding.galleryToolButton.isVisible = value
            }
            field = value
        }

    private lateinit var toolButtonAnimator: ValueAnimator

    private var init: (GalleryFragmentViewModel.() -> Unit)? = null

    fun onInit(
        isVideo: Boolean,
        isLock: Boolean,
        combine: Boolean,
        onAttach: (MutableSharedFlow<GalleryFragmentViewModel.FragEvent>) -> Unit
    ): GalleryFragment {
        init = {
            this.isVideo = isVideo
            this.isLock = isLock
            this.combine = combine
            this.onAttach = onAttach
        }
        return this
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val model: GalleryFragmentViewModel by viewModels()
        viewModel = model
        viewModel.apply {
            init?.invoke(this)
            attachEvent()
            init = null
            observeEvent()
        }
    }

    private fun GalleryFragmentViewModel.observeEvent() {
        launchDefault {
            fragEvent.bufferCollect {
                when (it) {
                    is GalleryFragmentViewModel.FragEvent.SelectAll -> {
                        getListAdapter().selectAll()
                        onSelectMod = true
                    }
                    is GalleryFragmentViewModel.FragEvent.OnActionBtn -> {
                        onSelectMod = true
                    }
                    is GalleryFragmentViewModel.FragEvent.IntoBox -> {
                        IntentDataHolder.put(
                            (getListAdapter().selectList.ifEmpty {
                                getGallery(getGalleryName()) ?: getGallery(ALL_GALLERY)
                            })
                        )
                        startActivity(PictureBoxActivity::class.intent)
                    }
                    is GalleryFragmentViewModel.FragEvent.OnMediaDeleted -> {
                        getListAdapter().noticeListItemDelete(it.media)
                    }
                    is GalleryFragmentViewModel.FragEvent.OnMediaInserted -> {
                        getListAdapter().noticeListItemInsert(it.media)
                    }
                    is GalleryFragmentViewModel.FragEvent.OnMediaUpdated -> {
                        getListAdapter().noticeListItemUpdate(it.media)
                    }
                    is GalleryFragmentViewModel.FragEvent.OnMediasInserted ->
                        getListAdapter().noticeListInsert(it.medias)
                    is GalleryFragmentViewModel.FragEvent.OnNewGallery -> {
                        getBucketAdapter().insertBucket(it.gallery)
                    }
                    is GalleryFragmentViewModel.FragEvent.OnGalleryRemoved -> {
                        getBucketAdapter().deleteBucket(it.gallery)
                    }
                    is GalleryFragmentViewModel.FragEvent.OnGalleryUpdated -> {
                        viewModel.run {
                            getBucketAdapter().apply {
                                val itemState = Gallery.ItemState.ALL_CHANGED
                                refreshBucket(it.gallery, itemState)
                                getBucket(ALL_GALLERY)?.let { gallery ->
                                    refreshBucket(gallery, itemState)
                                }
                            }
                        }
                    }
                    is GalleryFragmentViewModel.FragEvent.OnNewGalleries -> {
                        getBucketAdapter().insertBucket(it.galleries)
                    }
                    else -> Unit
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = initView(inflater, container)
        initList()
        viewModel.sortData()
        return binding.root
    }

    private fun initView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): GalleryFragmentLayoutBinding =
        GalleryFragmentLayoutBinding.inflate(inflater, container, false).apply {
            root.onGlobalLayout {
                galleryBucketContainer.botBlock = tabController.measuredHeight.toFloat()
                galleryBucketContainer.hideDuration = 120L
                toolButtonAnimator = AnimationHelper.rotation(galleryToolButton, 45f)
                galleryShowBucket.setOnStateListener(object : StatusImageView.StateListener {
                    override fun onActive() {
                        viewModel.isBucketShowUp = true
                        galleryToolButton.isVisible = true
                        galleryBucketContainer.show()
                        toolButtonAnimator.reverse()
                    }

                    override fun onNegative() {
                        if (viewModel.rightGallery == "") {
                            viewModel.rightGallery = ALL_GALLERY
                            launchDefault {
                                viewModel.getBucket(ALL_GALLERY)?.let { gallery ->
                                    viewModel.getGallery(gallery.name)
                                        ?.let { getListAdapter().setData(it) }
                                }
                            }
                        }
                        viewModel.isBucketShowUp = false
                        galleryToolButton.isVisible = onSelectMod
                        galleryBucketContainer.hide()
                        toolButtonAnimator.start()
                    }
                })
            }
            gallerySearch.setOnClickListener {
                launchDefault {
                    val gallery = viewModel.getGalleryName()
                    IntentDataHolder.put(viewModel.getGallery(gallery))
                    startActivity(GallerySearchActivity::class.intent.putExtra("gallery", gallery))
                }
            }
            galleryToolButton.setOnClickListener {
                if (!viewModel.isBucketShowUp) {
                    (galleryList.adapter as GalleryListAdapter).quitSelectMod()
                    onSelectMod = false
                } else {
                    activity?.titleDialog(R.string.user_name.getString(), "") {
                        if (it.isNotEmpty()) viewModel.addBucket(it)
                        else R.string.enter.getString().toast()
                    }
                }
            }
        }

    private fun initList() = binding.run {
        galleryList.apply {
            layoutManager = GridLayoutManager(context, 4)
            addItemDecoration(GalleryItemDecoration(paddingEnd))
            post {
                adapter = GalleryListAdapter(context = context, useSelect = true, itemCount = 0)
                    .also {
                        it.multiChoose = true
                        it.setOnSelectListener(this@GalleryFragment)
                    }
            }
        }
        galleryBucket.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = GalleryBucketAdapter(context) {
                selectBucket { gallery ->
                    onGallerySelected(gallery.name, gallery.size)
                }
                deleteGalleryBucket {
                    viewModel.deleteGalleryBucket(it)
                }
            }
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    super.getItemOffsets(outRect, view, parent, state)
                    outRect.top = resources.getDimension(R.dimen.icon_padding).toInt()
                }
            })
        }
    }

    private fun onGallerySelected(gallery: String, size: Int) {
        if (viewModel.rightGallery == gallery) {
            binding.galleryShowBucket.negative()
            return
        }
        viewModel.rightGallery = gallery
        if (viewModel.rightGallery != "") binding.galleryList.swapAdapter(
            GalleryListAdapter(requireContext(), true, itemCount = size).also {
                it.multiChoose = true
                it.setOnSelectListener(this@GalleryFragment)
            }, false
        )
        binding.galleryShowBucket.negative()
        launch {
            viewModel.getGallery(gallery)?.let { getListAdapter().setData(it) }
        }
    }

    private fun getBucketAdapter() = binding.galleryBucket.adapter as GalleryBucketAdapter

    private fun getListAdapter() = binding.galleryList.adapter as GalleryListAdapter

    override fun select(media: GalleryMedia) = Unit

    override fun select(medias: List<GalleryMedia>) {
        launch {
            viewModel.sendEvent(GalleryFragmentViewModel.FragEvent.OnSelect(medias), true)
        }
    }

    override fun openView(galleryMedia: GalleryMedia, elementView: View) {
        startActivity(
            GalleryViewActivity::class.intent
                .putExtra(MEDIA, galleryMedia.toJson())
                .putExtra(IS_VIDEO, galleryMedia.isVideo)
                .putExtra(GALLERY, viewModel.getGalleryName())
        )
    }

    override fun onDestroy() {
        binding.unbind()
        cancel()
        super.onDestroy()
    }

}
