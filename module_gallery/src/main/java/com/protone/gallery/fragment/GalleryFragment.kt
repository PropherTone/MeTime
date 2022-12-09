package com.protone.gallery.fragment

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
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
import com.protone.common.context.putExtras
import com.protone.common.entity.GalleryMedia
import com.protone.common.utils.ALL_GALLERY
import com.protone.common.utils.IntentDataHolder
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryViewWire.GALLERY
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryViewWire.IS_VIDEO
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryViewWire.MEDIA
import com.protone.common.utils.TAG
import com.protone.common.utils.displayUtils.AnimationHelper
import com.protone.common.utils.json.toJson
import com.protone.component.view.dialog.titleDialog
import com.protone.component.view.customView.StatusImageView
import com.protone.gallery.activity.GallerySearchActivity
import com.protone.gallery.activity.GalleryViewActivity
import com.protone.gallery.activity.PictureBoxActivity
import com.protone.gallery.adapter.GalleryBucketAdapter
import com.protone.gallery.adapter.GalleryItemDecoration
import com.protone.gallery.adapter.GalleryListAdapter
import com.protone.gallery.databinding.GalleryFragmentLayoutBinding
import com.protone.gallery.viewModel.GalleryFragmentViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow

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

    private var init: (() -> Unit)? = null

    fun onInit(
        isVideo: Boolean,
        isLock: Boolean,
        combine: Boolean,
        onAttach: (MutableSharedFlow<GalleryFragmentViewModel.FragEvent>) -> Unit
    ): GalleryFragment {
        init = {
            viewModel.apply {
                this.isVideo = isVideo
                this.isLock = isLock
                this.combine = combine
                this.onAttach = onAttach
            }
        }
        return this
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val model: GalleryFragmentViewModel by viewModels()
        viewModel = model
        init?.invoke()
        init = null
        viewModel.observeEvent()
    }

    private fun GalleryFragmentViewModel.observeEvent() {
        launchDefault {
            fragEvent.bufferCollect {
                Log.d(TAG, "observeEvent: $it")
                when (it) {
                    is GalleryFragmentViewModel.FragEvent.AddToGalleryBucket -> {
                        insertNewMedias(it.name, it.list)
                    }
                    is GalleryFragmentViewModel.FragEvent.SelectAll -> {
                        getListAdapter().selectAll()
                        onSelectMod = true
                    }
                    is GalleryFragmentViewModel.FragEvent.OnActionBtn -> {
                        onSelectMod = true
                    }
                    is GalleryFragmentViewModel.FragEvent.IntoBox -> {
                        IntentDataHolder.put(
                            (if (getListAdapter().selectList.size > 0) {
                                getListAdapter().selectList
                            } else {
                                getGallery(getGalleryName())
                                    ?: getGallery(ALL_GALLERY)
                            })
                        )
                        startActivity(PictureBoxActivity::class.intent)
                    }
                    is GalleryFragmentViewModel.FragEvent.OnNewGallery -> {
                        getBucketAdapter().insertBucket(it.gallery)
                    }
                    is GalleryFragmentViewModel.FragEvent.OnGalleryRemoved -> {
                        getBucketAdapter().deleteBucket(it.gallery)
                    }
                    is GalleryFragmentViewModel.FragEvent.OnMediaDeleted -> {
                        noticeListUpdate(
                            it.galleryMedia,
                            GalleryListAdapter.MediaStatus.DELETED
                        )
                    }
                    is GalleryFragmentViewModel.FragEvent.OnMediaInserted -> {
                        noticeListUpdate(
                            it.galleryMedia,
                            GalleryListAdapter.MediaStatus.INSERTED
                        )
                    }
                    is GalleryFragmentViewModel.FragEvent.OnMediaUpdated -> {
                        noticeListUpdate(
                            it.galleryMedia,
                            GalleryListAdapter.MediaStatus.UPDATED
                        )
                    }
                    is GalleryFragmentViewModel.FragEvent.OnGalleryUpdated -> {
                        viewModel.run {
                            getBucketAdapter().apply {
                                refreshBucket(it.gallery)
                                getBucket(ALL_GALLERY)?.let { gallery -> refreshBucket(gallery) }
                            }
                        }
                    }
                    is GalleryFragmentViewModel.FragEvent.OnNewGalleries -> {}
                    is GalleryFragmentViewModel.FragEvent.OnGalleriesRemoved -> {}
                    is GalleryFragmentViewModel.FragEvent.OnGalleriesUpdated -> {}
                    is GalleryFragmentViewModel.FragEvent.OnMediasDeleted -> {}
                    is GalleryFragmentViewModel.FragEvent.OnMediasInserted -> {}
                    is GalleryFragmentViewModel.FragEvent.OnMediasUpdated -> {}
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
        binding = GalleryFragmentLayoutBinding.inflate(inflater, container, false).apply {
            root.onGlobalLayout {
                galleryBucketContainer.botBlock = tabController.measuredHeight.toFloat()
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
                            onGallerySelected(ALL_GALLERY, 100)
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
                    startActivity(GallerySearchActivity::class.intent.putExtras {
                        putString("gallery", gallery)
                    })
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
        initList()
        viewModel.sortData()
        return binding.root
    }

    override fun onDestroy() {
        binding.unbind()
        cancel()
        super.onDestroy()
    }

    private fun initList() = binding.run {
        galleryList.apply {
            layoutManager = GridLayoutManager(context, 4)
            adapter = GalleryListAdapter(context = context, useSelect = true, itemCount = 0).also {
                it.multiChoose = true
                it.setOnSelectListener(this@GalleryFragment)
            }
            addItemDecoration(GalleryItemDecoration(paddingEnd))
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
        if (viewModel.rightGallery == gallery) return
        launch {
            viewModel.rightGallery = gallery
            binding.galleryShowBucket.negative()
            if (viewModel.isBucketShowUp) return@launch
            binding.galleryList.swapAdapter(
                GalleryListAdapter(requireContext(), true, itemCount = size).also {
                    it.multiChoose = true
                    it.setOnSelectListener(this@GalleryFragment)
                }, false
            )
            viewModel.getGallery(gallery)?.let { getListAdapter().setData(it) }
        }
    }

    private fun noticeListUpdate(media: GalleryMedia, status: GalleryListAdapter.MediaStatus) {
        if (viewModel.isBucketShowUp) return
        when (status) {
            GalleryListAdapter.MediaStatus.INSERTED -> {
                getListAdapter().noticeListItemInsert(media)
            }
            GalleryListAdapter.MediaStatus.DELETED -> {
                getListAdapter().noticeListItemDelete(media)
            }
            GalleryListAdapter.MediaStatus.UPDATED -> {
                getListAdapter().noticeListItemUpdate(media)
            }
        }
    }

    private fun getBucketAdapter() = binding.galleryBucket.adapter as GalleryBucketAdapter

    private fun getListAdapter() = binding.galleryList.adapter as GalleryListAdapter

    override fun select(galleryMedia: GalleryMedia) = Unit

    override fun select(galleryMedia: MutableList<GalleryMedia>) {
        launch {
            viewModel.sendEvent(GalleryFragmentViewModel.FragEvent.OnSelect(galleryMedia), true)
        }
    }

    override fun openView(galleryMedia: GalleryMedia) {
        startActivity(GalleryViewActivity::class.intent.putExtras {
            putString(MEDIA, galleryMedia.toJson())
            putBoolean(IS_VIDEO, galleryMedia.isVideo)
            putString(GALLERY, viewModel.getGalleryName())
        })
    }

}
