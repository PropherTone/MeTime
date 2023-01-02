package com.protone.metime.viewModel

import android.graphics.Bitmap
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.protone.common.R
import com.protone.common.baseType.toBitmap
import com.protone.common.baseType.withDefaultContext
import com.protone.common.baseType.withIOContext
import com.protone.common.context.MApplication
import com.protone.common.utils.displayUtils.Blur
import com.protone.component.BaseViewModel
import com.protone.metime.repository.TimeMediaDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    suspend fun getMusics(bucketName: String) = withContext(Dispatchers.IO) {
        musicDAO.getMusicBucketByName(bucketName)?.musicBucketId?.let {
            musicDAO.getMusicWithMusicBucket(it)
        }
    }

    suspend fun loadBlurIcon(path: String): Bitmap? = withDefaultContext {
        return@withDefaultContext try {
            Blur.blur(
                path.toBitmap(),
                radius = 10,
                sampling = 10
            )
        } catch (e: Exception) {
            null
        }
    }

}