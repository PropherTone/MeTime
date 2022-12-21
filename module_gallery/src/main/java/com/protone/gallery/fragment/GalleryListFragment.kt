package com.protone.gallery.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.protone.common.entity.GalleryMedia
import com.protone.component.BaseFragment
import com.protone.gallery.adapter.GalleryListAdapter
import com.protone.gallery.component.GalleryItemDecoration
import com.protone.gallery.databinding.GalleryListFragmentLayoutBinding
import com.protone.gallery.viewModel.GalleryListFragmentViewModel

class GalleryListFragment :
    BaseFragment<GalleryListFragmentLayoutBinding, GalleryListFragmentViewModel>() {

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
}