package com.protone.gallery.viewModel

import androidx.lifecycle.viewModelScope
import com.google.android.material.tabs.TabLayout
import com.protone.common.R
import com.protone.common.baseType.bufferCollect
import com.protone.common.baseType.getString
import com.protone.common.entity.GalleryMedia
import com.protone.component.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class GalleryViewModel : BaseViewModel(), TabLayout.OnTabSelectedListener {

    companion object {
        const val CHOOSE_MODE = "ChooseData"
        const val URI = "Uri"
        const val Gallery_DATA = "GalleryData"
        const val CHOOSE_PHOTO = "PHOTO"
        const val CHOOSE_VIDEO = "VIDEO"
        const val CHOOSE_MEDIA = "MEDIA"
    }

    var chooseData: MutableList<GalleryMedia>? = null
        private set

    private var rightMailer = 0

    private val mailers = arrayOfNulls<MutableSharedFlow<GalleryFragmentViewModel.FragEvent>>(2)

    fun setMailer(
        frag1: MutableSharedFlow<GalleryFragmentViewModel.FragEvent>? = null,
        frag2: MutableSharedFlow<GalleryFragmentViewModel.FragEvent>? = null
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            if (frag1 != null) {
                mailers[0] = frag1
                mailers[0]?.bufferCollect(onAction())
            }
            if (frag2 != null) {
                mailers[1] = frag2
                mailers[1]?.bufferCollect(onAction())
            }
        }
    }

    fun intoBox() {
        viewModelScope.launch {
            getCurrentMailer()?.emit(GalleryFragmentViewModel.FragEvent.IntoBox)
        }
    }

    fun selectAll() {
        viewModelScope.launch {
            getCurrentMailer()?.emit(GalleryFragmentViewModel.FragEvent.SelectAll)
        }
    }

    private fun onAction(): suspend (value: GalleryFragmentViewModel.FragEvent) -> Unit = {
        when (it) {
            is GalleryFragmentViewModel.FragEvent.OnSelect -> {
                if (chooseData == null) chooseData = it.galleryMedia
                if (it.galleryMedia.size > 0) getCurrentMailer()?.emit(GalleryFragmentViewModel.FragEvent.OnActionBtn)
            }
            else -> Unit
        }
    }

    private fun getCurrentMailer() = mailers[rightMailer]

    override fun onTabSelected(tab: TabLayout.Tab?) {
        when (tab?.text) {
            R.string.photo.getString() -> rightMailer = 0
            R.string.video.getString() -> rightMailer = 1
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
    override fun onTabReselected(tab: TabLayout.Tab?) = Unit
}