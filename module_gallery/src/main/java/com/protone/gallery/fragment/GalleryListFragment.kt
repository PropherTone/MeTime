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
import com.protone.gallery.R
import com.protone.gallery.adapter.GalleryListAdapter
import com.protone.gallery.component.GalleryItemDecoration
import com.protone.gallery.databinding.GalleryListFragmentLayoutBinding
import com.protone.gallery.viewModel.GalleryListFragmentViewModel
import com.protone.gallery.viewModel.GalleryViewModel
import com.protone.gallery.viewModel.MediaSelectedList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

class GalleryListFragment :
    BaseFragment<GalleryListFragmentLayoutBinding, GalleryListFragmentViewModel>() {

    private var selectedMedias = MediaSelectedList()
    private var isInit = false

    private val onSelect by lazy {
        object : GalleryListAdapter.OnSelect {

            override fun openView(galleryMedia: GalleryMedia, elementView: View) {
                toGalleryView(
                    galleryMedia.toJson(),
                    galleryMedia.isVideo,
                    viewModel.isCustom,
                    viewModel.galleryName
                )
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
        selectedList: MediaSelectedList,
    ) {
        selectedMedias = selectedList
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
                        if (viewModel.galleryName == it.gallery.name) return@bufferCollect
                        viewModel.galleryName = it.gallery.name
                        viewModel.isCustom = it.gallery.custom
                        if (it.isDrawerOpen) changeSpanCount(true, getLayoutManager())
                        if (getListAdapter().itemCount > 0) {
                            binding.galleryList.swapAdapter(
                                GalleryListAdapter(
                                    requireContext(),
                                    true,
                                    itemCount = it.gallery.size
                                ).also { adapter ->
                                    adapter.onSelectMod = getListAdapter().onSelectMod
                                    adapter.selectList = selectedMedias
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
                    is GalleryViewModel.GalleryListEvent.ExitSelect -> {
                        getListAdapter().exitSelectMod()
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
            addItemDecoration(GalleryItemDecoration(resources.getDimensionPixelSize(R.dimen.item_margin)))
            adapter = GalleryListAdapter(context = context, useSelect = true, itemCount = 0).also {
                it.multiChoose = true
                it.selectList = selectedMedias
                it.setOnSelectListener(onSelect)
            }
        }
    }

    private fun getListAdapter() = binding.galleryList.adapter as GalleryListAdapter

    private fun getLayoutManager() = binding.galleryList.layoutManager as GridLayoutManager

}