package com.protone.common.media.musicPlayer

sealed interface OnPlayDSL {
    fun onCompletion(onCompletionBlock: () -> Unit)
    fun onStart(onStartBlock: () -> Unit)
    fun onPause(onPauseBlock: () -> Unit)
    fun onProgress(onProgressBlock: (Long) -> Unit)
}