package com.protone.gallery.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.protone.common.baseType.bufferCollect
import com.protone.common.baseType.launchMain
import com.protone.common.context.doHoverSelect
import com.protone.common.entity.GalleryMedia
import com.protone.common.utils.json.toJson
import com.protone.component.BaseFragment
import com.protone.component.toGalleryView
import com.protone.gallery.adapter.GalleryListAdapter
import com.protone.gallery.component.GalleryItemDecoration
import com.protone.gallery.databinding.GalleryListFragmentLayoutBinding
import com.protone.gallery.viewModel.GalleryListFragmentViewModel
import com.protone.gallery.viewModel.GalleryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class GalleryListFragment :
    BaseFragment<GalleryListFragmentLayoutBinding, GalleryListFragmentViewModel>() {

    private var isInit = false
    private lateinit var selectFlow: MutableSharedFlow<GalleryListFragmentViewModel.GallerySelectData?>
    private var galleryName = ""

    private val onSelect by lazy {
        object : GalleryListAdapter.OnSelect {
            override fun select(media: GalleryMedia) {
                launch {
                    selectFlow.emit(
                        GalleryListFragmentViewModel
                            .GallerySelectData
                            .OnGalleryMediaSelect(media)
                    )
                }
            }

            override fun select(medias: List<GalleryMedia>) {
                launch {
                    selectFlow.emit(
                        GalleryListFragmentViewModel
                            .GallerySelectData
                            .OnGalleryMediasSelect(medias)
                    )
                }
            }

            override fun openView(galleryMedia: GalleryMedia, elementView: View) {
                toGalleryView(galleryMedia.toJson(), galleryMedia.isVideo, galleryName)
            }

            override fun onItemLongClick() {
                binding.galleryList.doHoverSelect {
                    getListAdapter().select(it)
                }
            }

        }
    }

    fun connect(
        multiChoose: Boolean,
        mailer: Flow<GalleryViewModel.GalleryListEvent>,
        flow: MutableSharedFlow<GalleryListFragmentViewModel.GallerySelectData?>
    ) {
        selectFlow = flow
        launchMain {
            fun changeSpanCount(isOpen: Boolean, layoutManager: GridLayoutManager) {
                val count = if (isOpen) 2 else 4
                if (layoutManager.spanCount == count) return
                layoutManager.spanCount = count
                layoutManager.requestSimpleAnimationsInNextLayout()
            }
            mailer.bufferCollect {
                while (!isInit) delay(20L)
                when (it) {
                    is GalleryViewModel.GalleryListEvent.OnGallerySelected -> {
                        if (galleryName == it.gallery.name) return@bufferCollect
                        galleryName = it.gallery.name
                        if (it.isDrawerOpen) changeSpanCount(true, getLayoutManager())
                        if (getListAdapter().itemCount > 0) {
                            binding.galleryList.swapAdapter(
                                GalleryListAdapter(
                                    requireContext(),
                                    true,
                                    itemCount = it.gallery.size
                                ).also { adapter ->
                                    adapter.itemLength = getListAdapter().itemLength
                                    adapter.multiChoose = multiChoose
                                    adapter.setOnSelectListener(onSelect)
                                }, false
                            )
                        }
                        viewModel.getGalleryData(it.gallery, it.isVideo, it.combine)?.let { data ->
                            getListAdapter().setData(data)
                        }
                    }
                    is GalleryViewModel.GalleryListEvent.SelectAll -> {
                        getListAdapter().selectAll()
                    }
                    is GalleryViewModel.GalleryListEvent.QuiteSelect -> {
                        getListAdapter().quitSelectMod()
                    }
                    is GalleryViewModel.GalleryListEvent.OnDrawerEvent -> {
                        changeSpanCount(it.isOpen, getLayoutManager())
                    }
                    is GalleryViewModel.GalleryListEvent.OnMediaDeleted -> {
                        getListAdapter().noticeListItemDelete(it.media)
                    }
                    is GalleryViewModel.GalleryListEvent.OnMediaInserted -> {
                        getListAdapter().noticeListItemInsert(it.media)
                    }
                    is GalleryViewModel.GalleryListEvent.OnMediaUpdated -> {
                        getListAdapter().noticeListItemUpdate(it.media)
                    }
                    is GalleryViewModel.GalleryListEvent.OnMediasInserted ->
                        getListAdapter().noticeListInsert(it.medias)
                }
            }
        }
    }

    fun getGalleryData() = getListAdapter().mList

    override fun createViewModel(): GalleryListFragmentViewModel {
        val model: GalleryListFragmentViewModel by viewModels()
        return model
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): GalleryListFragmentLayoutBinding {
        return GalleryListFragmentLayoutBinding.inflate(inflater, container, false).apply {
            initList()
            isInit = true
        }
    }

    private fun GalleryListFragmentLayoutBinding.initList() {
        galleryList.apply {
            layoutManager = GridLayoutManager(context, 4)
            addItemDecoration(GalleryItemDecoration(paddingEnd))
            adapter =
                GalleryListAdapter(context = context, useSelect = true, itemCount = 0).also {
                    it.multiChoose = true
                    it.setOnSelectListener(onSelect)
                }
        }
    }

    private fun getListAdapter() = binding.galleryList.adapter as GalleryListAdapter

    private fun getLayoutManager() = binding.galleryList.layoutManager as GridLayoutManager

}