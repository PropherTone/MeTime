package com.protone.metime.viewModel

import androidx.lifecycle.viewModelScope
import com.protone.common.baseType.getString
import com.protone.common.baseType.imageSaveToFile
import com.protone.common.context.MApplication
import com.protone.common.entity.MusicBucket
import com.protone.common.utils.todayDate
import com.protone.common.R
import com.protone.component.BaseViewModel
import com.protone.worker.database.dao.DatabaseHelper
import com.protone.worker.database.MediaAction
import com.protone.worker.database.userConfig
import com.protone.worker.media.scanAudio
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
            DatabaseHelper.instance.run {
                withContext(Dispatchers.IO) {
                    musicDAOBridge.insertMusicMulti(scanAudio { _, _ -> })
                }

                val allMusicRs = musicDAOBridge.getAllMusic() ?: return

                var launch: Job? = null
                launch = viewModelScope.launch(Dispatchers.Default) {
                    mediaNotifier.collect {
                        if (it is MediaAction.OnNewMusicBucket) {
                            musicWithMusicBucketDAOBridge.insertMusicMultiAsyncWithBucket(
                                R.string.all_music.getString(),
                                allMusicRs
                            )
                            launch?.cancel()
                        }
                    }
                }

                musicBucketDAOBridge.addMusicBucketAsync(
                    MusicBucket(
                        R.string.all_music.getString(),
                        if (allMusicRs.isNotEmpty()) allMusicRs[0].uri.imageSaveToFile(
                            R.string.all_music.getString(),
                            R.string.music_bucket.getString()
                        ) else null,
                        allMusicRs.size,
                        null,
                        todayDate("yyyy/MM/dd")
                    )
                )
            }
            withContext(Dispatchers.IO) {
                val dir = File("${MApplication.app.filesDir.absolutePath}/SharedMedia")
                if (!dir.exists()) {
                    dir.mkdirs()
                }
            }
            userConfig.apply {
                isFirstBoot = false
                lastMusicBucket = R.string.all_music.getString()
                playedMusicPosition = -1
            }
        }
    }
}