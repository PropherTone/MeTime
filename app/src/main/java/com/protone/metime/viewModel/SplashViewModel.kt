package com.protone.metime.viewModel

import androidx.lifecycle.viewModelScope
import com.protone.common.baseType.getString
import com.protone.common.baseType.imageSaveToFile
import com.protone.common.context.MApplication
import com.protone.common.entity.MusicBucket
import com.protone.common.utils.todayDate
import com.protone.common.R
import com.protone.common.media.scanAudio
import com.protone.common.utils.ALL_MUSIC
import com.protone.common.utils.MUSIC_BUCKET
import com.protone.component.BaseViewModel
import com.protone.component.database.MediaAction
import com.protone.component.database.dao.DatabaseHelper
import com.protone.component.database.userConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class SplashViewModel : BaseViewModel() {

    sealed class SplashEvent : ViewEvent {
        object InitConfig : SplashEvent()
        object UpdateMedia : SplashEvent()
    }

    suspend fun firstBootWork() {
        if (userConfig.isFirstBoot) {
            withContext(Dispatchers.IO) {
                val dir = File("${MApplication.app.filesDir.absolutePath}/SharedMedia")
                if (!dir.exists()) {
                    dir.mkdirs()
                }
            }
            userConfig.apply {
                isFirstBoot = false
                lastMusicBucket = ALL_MUSIC
                playedMusicPosition = -1
                lastMusicBucketCover = musicDAO.getAllMusic()
                    .takeIf { it?.isNotEmpty() == true }?.first()?.uri.toString()
            }
        }
    }
}