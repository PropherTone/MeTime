package com.protone.metime.viewModel

import com.protone.common.baseType.withIOContext
import com.protone.common.context.MApplication
import com.protone.common.utils.ALL_MUSIC
import com.protone.component.BaseViewModel
import com.protone.component.database.userConfig
import java.io.File

class SplashViewModel : BaseViewModel() {

    suspend fun firstBootWork() {
        withIOContext {
            if (userConfig.isFirstBoot) {
                val dir = File("${MApplication.app.filesDir.absolutePath}/SharedMedia")
                if (!dir.exists()) dir.mkdirs()
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