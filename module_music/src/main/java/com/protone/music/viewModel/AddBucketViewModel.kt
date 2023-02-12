package com.protone.music.viewModel

import android.net.Uri
import com.protone.common.baseType.deleteFile
import com.protone.common.baseType.imageSaveToDisk
import com.protone.common.baseType.toBase64
import com.protone.common.entity.MusicBucket
import com.protone.common.utils.MUSIC_BUCKET
import com.protone.common.utils.todayDate
import com.protone.component.BaseViewModel

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
                todayDate("yyyy-MM-dd hh:mm:ss")
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
            uri?.let {
                mb.icon?.deleteFile()
                it.imageSaveToDisk(mb.name.getBucketSaveIconName(), MUSIC_BUCKET)
            }?.takeIf { mb.icon == null || mb.icon?.equals(it) == false }?.let {
                if (mb.icon == null || mb.icon?.equals(it) == false) {
                    mb.icon = it
                }
            }
            if (mb.detail != detail) mb.detail = detail
        }
    )

    private fun String.getBucketSaveIconName(): String {
        return "${this}[${todayDate("yyyy/MM/dd-hh:mm:ss")}]".toBase64() ?: this
    }

    suspend fun getMusicBucketByName(name: String) = musicDAO.getMusicBucketByName(name)

}