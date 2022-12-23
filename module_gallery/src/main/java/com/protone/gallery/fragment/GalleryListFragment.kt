package com.protone.gallery.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import com.protone.common.baseType.bufferCollect
import com.protone.common.baseType.launchDefault
import com.protone.common.baseType.launchMain
import com.protone.common.baseType.withMainContext
import com.protone.common.context.intent
import com.protone.common.context.putExtras
import com.protone.common.entity.GalleryMedia
import com.protone.common.utils.ALL_GALLERY
import com.protone.common.utils.IntentDataHolder
import com.protone.common.utils.RouterPath
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryViewWire.GALLERY
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryViewWire.IS_VIDEO
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryViewWire.MEDIA
import com.protone.common.utils.json.toJson
import com.protone.component.BaseFragment
import com.protone.gallery.activity.GalleryViewActivity
import com.protone.gallery.activity.PictureBoxActivity
import com.protone.gallery.adapter.GalleryListAdapter
import com.protone.gallery.component.GalleryItemDecoration
import com.protone.gallery.databinding.GalleryListFragmentLayoutBinding
import com.protone.gallery.viewModel.GalleryFragmentViewModel
import com.protone.gallery.viewModel.GalleryListFragmentViewModel
import com.protone.gallery.viewModel.GalleryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class GalleryListFragment :
    BaseFragment<GalleryListFragmentLayoutBinding, GalleryListFragmentViewModel>() {

    private var isInit = false
    private lateinit var liveSelectData: MutableLiveData<GalleryMedia>
    private var galleryName = ALL_GALLERY

    private val onSelect by lazy {
        object : GalleryListAdapter.OnSelect {
            override fun select(galleryMedia: GalleryMedia) {
                liveSelectData.postValue(galleryMedia)
            }

            override fun select(galleryMedia: MutableList<GalleryMedia>) {
                launchDefault {
                    galleryMedia.forEach {
                        liveSelectData.postValue(it)
                    }
                }
            }

            override fun openView(galleryMedia: GalleryMedia) {
                startActivity(GalleryViewActivity::class.intent.putExtras {
                    putString(MEDIA, galleryMedia.toJson())
                    putBoolean(IS_VIDEO, galleryMedia.isVideo)
                    putString(GALLERY, galleryName)
                })
            }

        }
    }

    fun connect(
        mailer: Flow<GalleryViewModel.GalleryListEvent>,
        liveData: MutableLiveData<GalleryMedia>
    ) {
        liveSelectData = liveData
        launchMain {
            mailer.bufferCollect {
                while (!isInit) delay(20L)
                when (it) {
                    is GalleryViewModel.GalleryListEvent.OnGallerySelected -> {
                        galleryName = it.gallery.name
                        binding.galleryList.swapAdapter(
                            GalleryListAdapter(
                                requireContext(),
                                true,
                                itemCount = it.gallery.size
                            ).also { adapter ->
                                adapter.itemLength = getListAdapter().itemLength
                                adapter.multiChoose = true
                                adapter.setOnSelectListener(onSelect)
                            }, false
                        )
                        viewModel.getGallery(it.gallery, it.isVideo, it.combine)
                            ?.let { data -> getListAdapter().setData(data) }
                    }
                    is GalleryViewModel.GalleryListEvent.SelectAll -> {
                        getListAdapter().selectAll()
                    }
                    is GalleryViewModel.GalleryListEvent.QuiteSelect -> {
                        getListAdapter().quitSelectMod()
                    }
                    is GalleryViewModel.GalleryListEvent.OnDrawerEvent -> {
                        if (it.isOpen) {
                            getLayoutManager().spanCount = 2
                        } else {
                            getLayoutManager().spanCount = 4
                        }
                        getLayoutManager().requestSimpleAnimationsInNextLayout()
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