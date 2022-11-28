package com.protone.music.viewModel

import android.net.Uri
import com.protone.common.baseType.deleteFile
import com.protone.common.baseType.getString
import com.protone.common.baseType.imageSaveToDisk
import com.protone.common.entity.MusicBucket
import com.protone.common.utils.MUSIC_BUCKET
import com.protone.common.utils.todayDate
import com.protone.component.BaseViewModel
import com.protone.music.R

class AddBucketViewModel : BaseViewModel() {

    companion object {
        @JvmStatic
        val BUCKET_NAME = "BUCKET_NAME"
    }

    sealed class AddBucketEvent : ViewEvent {
        object Confirm : AddBucketEvent()
        object ChooseIcon : AddBucketEvent()
        object Cancel : AddBucketEvent()
    }

    var editName: String? = null
    var musicBucket: MusicBucket? = null

    suspend fun addMusicBucket(
        name: String,
        uri: Uri?,
        detail: String,
        callback: (Boolean, String) -> Unit
    ) {
        musicDAO.addMusicBucketWithCallBack(
            MusicBucket(
                name,
                uri?.imageSaveToDisk(name, MUSIC_BUCKET),
                0,
                detail,
                todayDate("yyyy/MM/dd")
            )
        ) { result, rName ->
            callback.invoke(result, rName)
        }
    }

    suspend fun updateMusicBucket(
        musicBucket: MusicBucket, name: String, uri: Uri?, detail: String
    ) = musicDAO.updateMusicBucket(
        musicBucket.also { mb ->
            if (mb.name != name) mb.name = name
            mb.icon?.deleteFile()
            val toFile = uri?.imageSaveToDisk(name, R.string.music_bucket.getString())
            if (mb.icon?.equals(toFile) == false) mb.icon = toFile
            if (mb.detail != detail) mb.detail = detail
            todayDate("yyyy/MM/dd")
        }
    )

    suspend fun getMusicBucketByName(name: String) = musicDAO.getMusicBucketByName(name)

}