package com.protone.metime.viewModel

import android.graphics.Bitmap
import com.protone.common.R
import com.protone.common.baseType.toBitmap
import com.protone.common.baseType.withDefaultContext
import com.protone.common.baseType.withIOContext
import com.protone.common.context.MApplication
import com.protone.common.entity.GalleryMedia
import com.protone.component.database.photoInToday
import com.protone.component.database.randomNote
import com.protone.component.database.videoInToday
import com.protone.common.utils.displayUtils.Blur
import com.protone.component.BaseViewModel
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

    suspend fun getMusics(bucketName: String) = withContext(Dispatchers.IO) {
        musicDAO.getMusicBucketByName(bucketName)?.musicBucketId?.let {
            musicDAO.getMusicWithMusicBucket(it)
        }
    }

    suspend fun loadBlurIcon(path: String): Bitmap? = withDefaultContext {
        return@withDefaultContext try {
            Blur.blur(
                withIOContext { path.toBitmap() },
                radius = 10,
                sampling = 10
            )
        } catch (e: Exception) {
            null
        }
    }

}