package com.protone.common.context

import android.content.Intent
import android.content.IntentFilter
import kotlin.reflect.KClass

const val MUSIC_PLAY = "ControlMusic"
const val MUSIC_PLAY_CUR = "ControlMusicCurrent"
const val MUSIC_PAUSE = "PauseMusic"
const val MUSIC_NEXT = "NextMusic"
const val MUSIC_PREVIOUS = "PreviousMusic"
const val MUSIC_FINISH = "FinishMusic"
const val MUSIC_REFRESH = "RefreshNotification"

const val FINISH = "FINISH"
const val ACTIVITY_FINISH = "ACTIVITY_FINISH"
const val ACTIVITY_RESTART = "ACTIVITY_RESTART"
const val MUSIC = "MUSIC"

const val UPDATE_MUSIC = "UPDATE_MUSIC"
const val UPDATE_GALLERY = "UPDATE_GALLERY"

val musicIntentFilter: IntentFilter
    get() {
        return IntentFilter().apply {
            addAction(MUSIC_PLAY)
            addAction(MUSIC_PAUSE)
            addAction(MUSIC_FINISH)
            addAction(MUSIC_NEXT)
            addAction(MUSIC_PREVIOUS)
            addAction(MUSIC_REFRESH)
            addAction(MUSIC_PLAY_CUR)
        }
    }

val workIntentFilter: IntentFilter
    get() {
        return IntentFilter().apply {
            addAction(UPDATE_MUSIC)
            addAction(UPDATE_GALLERY)
        }
    }

val appIntentFilter: IntentFilter
    get() {
        return IntentFilter().apply {
            addAction(FINISH)
            addAction(MUSIC)
        }
    }

val KClass<*>.intent
    get() = Intent(MApplication.app, this.java)