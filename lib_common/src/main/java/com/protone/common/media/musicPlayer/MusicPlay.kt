package com.protone.common.media.musicPlayer

import android.content.Context
import com.protone.common.entity.Music

interface MusicPlay {
    var progress: Long
    fun init(context: Context)
    fun prepare(music: Music)
    fun play(music: Music?)
    fun pause()
    fun release()
    fun seekTo(progress: Long)
    fun setProgressCallbackDuration(duration: Long)
    fun onPlayState(block: OnPlayDSL.() -> Unit)
}