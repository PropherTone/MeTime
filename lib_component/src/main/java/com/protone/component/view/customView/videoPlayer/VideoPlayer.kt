package com.protone.component.view.customView.videoPlayer

interface VideoPlayer {
    fun play()
    fun pause()
    fun stop()
    fun onPre()
    fun onNext()
    fun seekTo(progress: Long)
}