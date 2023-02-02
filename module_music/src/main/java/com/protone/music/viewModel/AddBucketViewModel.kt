package com.protone.music.viewModel

import android.net.Uri
import com.protone.common.baseType.deleteFile
import com.protone.common.baseType.getString
import com.protone.common.baseType.imageSaveToDisk
import com.protone.common.baseType.toBase64
import com.protone.common.entity.MusicBucket
import com.protone.common.utils.MUSIC_BUCKET
import com.protone.common.utils.todayDate
import com.protone.component.BaseViewModel
import com.protone.component.view.customView.musicPlayer.bitmapCachePool
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
                uri?.imageSaveToDisk(name.getBucketSaveIconName(), MUSIC_BUCKET),
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
            val toFile = uri?.imageSaveToDisk(
                mb.name.getBucketSaveIconName(),
                MUSIC_BUCKET
            )
            if (mb.icon == null || mb.icon?.equals(toFile) == false) {
                mb.icon = toFile
                toFile?.let { icon -> bitmapCachePool.remove(icon) }
            }
            if (mb.detail != detail) mb.detail = detail
            todayDate("yyyy/MM/dd")
        }
    )

    private fun String.getBucketSaveIconName(): String {
        return "${this}[${todayDate("yyyy/MM/dd-hh:mm:ss")}]".toBase64() ?: this
    }

    suspend fun getMusicBucketByName(name: String) = musicDAO.getMusicBucketByName(name)

}