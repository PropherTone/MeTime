package com.protone.metime.viewModel

import android.graphics.Bitmap
import com.protone.common.R
import com.protone.common.baseType.toBitmap
import com.protone.common.context.MApplication
import com.protone.common.entity.GalleryMedia
import com.protone.common.utils.displayUtils.Blur
import com.protone.worker.BaseViewModel
import com.protone.worker.database.DatabaseHelper
import com.protone.worker.media.photoInToday
import com.protone.worker.media.randomNote
import com.protone.worker.media.videoInToday
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
        DatabaseHelper.instance.run {
            musicBucketDAOBridge.getMusicBucketByName(bucketName)?.musicBucketId?.let {
                musicWithMusicBucketDAOBridge.getMusicWithMusicBucket(it)
            }
        }
    }

    fun loadBlurIcon(path: String): Bitmap? {
        return try {
            Blur.blur(
                path.toBitmap(),
                radius = 10,
                sampling = 10
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getPhotoInToday(): GalleryMedia? = withContext(Dispatchers.Default) {
        photoInToday()
    }


    suspend fun getVideoInToday() = withContext(Dispatchers.Default) {
        videoInToday()
    }

    suspend fun getRandomNote() = withContext(Dispatchers.Default) {
        randomNote()
    }

}