package com.protone.metime.viewModel

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.protone.common.baseType.withIOContext
import com.protone.common.context.MApplication
import com.protone.common.utils.ALL_MUSIC
import com.protone.component.BaseViewModel
import com.protone.component.R
import com.protone.metime.repository.TimeMediaDataSource

class MainViewModel : BaseViewModel() {
    var btnY = 0f
    val btnH = MApplication.app.resources.getDimensionPixelSize(R.dimen.action_icon_p)

    sealed class MainViewEvent : ViewEvent {
        object Gallery : MainViewEvent()
        object Music : MainViewEvent()
        object Note : MainViewEvent()
        object UserConfig : MainViewEvent()
    }

    fun getTimeMediaPager(pageSize: Int) = Pager(
        config = PagingConfig(pageSize),
        pagingSourceFactory = { TimeMediaDataSource() }
    ).flow.cachedIn(viewModelScope)

    suspend fun getMusics(bucketName: String) = withIOContext {
        if (bucketName == ALL_MUSIC) {
            musicDAO.getAllMusic()
        } else musicDAO.getMusicBucketByName(bucketName)?.musicBucketId?.let {
            musicDAO.getMusicWithMusicBucket(it)
        }
    }

}