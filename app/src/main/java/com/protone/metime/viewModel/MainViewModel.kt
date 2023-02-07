package com.protone.metime.viewModel

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.protone.common.baseType.launchDefault
import com.protone.common.baseType.withIOContext
import com.protone.common.context.MApplication
import com.protone.common.entity.Music
import com.protone.common.utils.ALL_MUSIC
import com.protone.component.BaseViewModel
import com.protone.component.R
import com.protone.component.database.userConfig
import com.protone.metime.repository.TimeMediaDataSource
import kotlinx.coroutines.cancel
import java.util.concurrent.atomic.AtomicBoolean

class MainViewModel : BaseViewModel() {
    var btnY = 0f
    val btnH = MApplication.app.resources.getDimensionPixelSize(R.dimen.action_icon_p)

    sealed class MainViewEvent : ViewEvent {
        object Gallery : MainViewEvent()
        object Music : MainViewEvent()
        object Note : MainViewEvent()
        object UserConfig : MainViewEvent()
    }

    var isBindMusicService: AtomicBoolean? = null
        private set
    var isMusicsUpdated = false

    fun observeMusicEvent(block: (List<Music>) -> Unit) {
        viewModelScope.launchDefault {
            isBindMusicService = observeMusicDataMutable(viewModelScope) {
                getMusics(userConfig.lastMusicBucket)?.let {
                    block.invoke(it)
                    isMusicsUpdated = true
                    cancel()
                }
            }
        }
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