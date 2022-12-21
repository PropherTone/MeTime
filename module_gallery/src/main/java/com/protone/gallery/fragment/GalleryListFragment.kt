package com.protone.gallery.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import com.protone.common.baseType.bufferCollect
import com.protone.common.baseType.launchDefault
import com.protone.common.context.intent
import com.protone.common.entity.GalleryMedia
import com.protone.common.utils.ALL_GALLERY
import com.protone.common.utils.IntentDataHolder
import com.protone.component.BaseFragment
import com.protone.gallery.activity.PictureBoxActivity
import com.protone.gallery.adapter.GalleryListAdapter
import com.protone.gallery.component.GalleryItemDecoration
import com.protone.gallery.databinding.GalleryListFragmentLayoutBinding
import com.protone.gallery.viewModel.GalleryFragmentViewModel
import com.protone.gallery.viewModel.GalleryListFragmentViewModel
import com.protone.gallery.viewModel.GalleryViewModel
import kotlinx.coroutines.flow.SharedFlow

class GalleryListFragment :
    BaseFragment<GalleryListFragmentLayoutBinding, GalleryListFragmentViewModel>() {

    private lateinit var liveSelectData : MutableList<GalleryMedia>

    fun connect(
        mailer: SharedFlow<GalleryViewModel.GalleryListEvent>,
        liveData: MutableList<GalleryMedia>
    ) {
        liveSelectData = liveData
        launchDefault {
            mailer.bufferCollect {
                when (it) {
                    is GalleryViewModel.GalleryListEvent.OnGallerySelected -> {}
                    is GalleryViewModel.GalleryListEvent.SelectAll -> {
                        getListAdapter().selectAll()
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
                    else -> Unit
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
        }
    }

    private fun GalleryListFragmentLayoutBinding.initList() {
        galleryList.apply {
            layoutManager = GridLayoutManager(context, 4)
            addItemDecoration(GalleryItemDecoration(paddingEnd))
            post {
                adapter =
                    GalleryListAdapter(context = context, useSelect = true, itemCount = 0).also {
                        it.multiChoose = true
                        it.setOnSelectListener(object : GalleryListAdapter.OnSelect {
                            override fun select(galleryMedia: GalleryMedia) {
                                liveSelectData.add(galleryMedia)
                            }

                            override fun select(galleryMedia: MutableList<GalleryMedia>) {
                            }

                            override fun openView(galleryMedia: GalleryMedia) {
                            }

                        })
                    }
            }
        }
    }

    private fun getListAdapter() = binding.galleryList.adapter as GalleryListAdapter

}