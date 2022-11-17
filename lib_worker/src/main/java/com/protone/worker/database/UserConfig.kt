package com.protone.worker.database

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.protone.common.context.MApplication
import com.protone.database.sp.DataDelegate
import com.protone.database.sp.DataStoreTool

val userConfig = UserConfig(MApplication.app)

class UserConfig(context: Context) {

    private val Context.dataProvider by preferencesDataStore(name = "USER_CONFIG")

    private val config = DataStoreTool(DataDelegate(context.dataProvider))

    var isFirstBoot by config.boolean("FIRST_BOOT", true)

    var userName by config.string("USER_NAME", "")

    var isLogin by config.boolean("USER_IS_LOGIN", false)

    var userIcon by config.string("USER_ICON_PATH", "")

    var userPassword by config.string("USER_PASSWORD", "")

    var lockGallery by config.string("LOCK_GALLERY", "")

    var lockNote by config.string("LOCK_NOTE", "")

    var lockMusic by config.string("LOCK_MUSIC", "")

    var combineGallery by config.boolean("COMBINE_GALLERY", false)


    var lastMusicBucket by config.string(
        "PLAYED_MUSIC_BUCKET",
        MApplication.app.getString(com.protone.common.R.string.all_music)
    )

    var lastMusic by config.string("LAST_TIME_PLAYED_MUSIC", "")

    var lastMusicProgress by config.long("LAST_MUSIC_PROGRESS", 0L)

    var playedMusicPosition by config.int("MUSIC_POSITION", 0)

    var musicLoopMode by config.int("LOOP_MODE", 1)

    data class UserData(
        val userName: String,
        val isLogin: Boolean,
        val userIcon: String,
        val userPassword: String,
        val lockGallery: String,
        val lockNote: String,
        val lockMusic: String
    )

    fun getJson() = this.run {
        val userData = UserData(
            userName,
            isLogin,
            userIcon,
            userPassword,
            lockGallery,
            lockNote,
            lockMusic
        )
        userData
    }

}