package com.protone.gallery.activity

import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.protone.common.R
import com.protone.common.baseType.getString
import com.protone.common.baseType.toast
import com.protone.common.context.root
import com.protone.common.entity.GalleryMedia
import com.protone.common.utils.RouterPath
import com.protone.component.BaseActivity
import com.protone.component.BaseViewModel
import com.protone.component.customView.ScalableRegionLoadingImageView
import com.protone.component.customView.bitmapCache
import com.protone.gallery.adapter.PictureBoxAdapter
import com.protone.gallery.databinding.PictureBoxActivityBinding
import com.protone.gallery.viewModel.PictureBoxViewModel

@Route(path = RouterPath.GalleryRouterPath.Box)
class PictureBoxActivity :
    BaseActivity<PictureBoxActivityBinding, PictureBoxViewModel, BaseViewModel.ViewEvent>(false) {
    override val viewModel: PictureBoxViewModel by viewModels()

    override fun createView(): PictureBoxActivityBinding {
        return PictureBoxActivityBinding.inflate(layoutInflater, root, false)
    }

    override suspend fun PictureBoxViewModel.init() {
        val gainListData = getGainListData<GalleryMedia>()
        if (gainListData != null) {
            initPictureBox(gainListData as MutableList<GalleryMedia>)
        } else {
            R.string.no_data.getString().toast()
            finish()
        }
    }

    private fun initPictureBox(picUri: MutableList<GalleryMedia>) {
        binding.picView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = PictureBoxAdapter(context, picUri)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState != RecyclerView.SCROLL_STATE_IDLE) return
                    (layoutManager as LinearLayoutManager).also {
                        val firstVisible = it.findFirstVisibleItemPosition()
                        val lastVisible = it.findLastVisibleItemPosition()
                        for (i in firstVisible..lastVisible) {
                            when (val child = it.findViewByPosition(i)) {
                                is ScalableRegionLoadingImageView -> if (child.isLongImage()) {
                                    child.reZone()
                                }
                            }
                        }
                    }
                }
            })
        }
    }

    override suspend fun doFinish() {
        bitmapCache.evictAll()
    }
}