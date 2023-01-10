package com.protone.common.media.musicPlayer

abstract class BaseMusicPlayer : MusicPlay {

    protected var mProgressCallbackDuration = 100L
    protected var duration = -1L

    protected var onCompletion: (() -> Unit)? = null
    protected var onStart: (() -> Unit)? = null
    protected var onPause: (() -> Unit)? = null
    protected var onProgress: ((Long) -> Unit)? = null

    override var progress: Long = -1

    override fun onPlayState(block: OnPlayDSL.() -> Unit) {
        PlayListenerDSL().also(block)
    }

    override fun setProgressCallbackDuration(duration: Long) {
        this.mProgressCallbackDuration = duration
    }

    inner class PlayListenerDSL : OnPlayDSL {

        override fun onCompletion(onCompletionBlock: () -> Unit) {
            onCompletion = onCompletionBlock
        }

        override fun onStart(onStartBlock: () -> Unit) {
            onStart = onStartBlock
        }

        override fun onPause(onPauseBlock: () -> Unit) {
            onPause = onPauseBlock
        }

        override fun onProgress(onProgressBlock: (Long) -> Unit) {
            onProgress = onProgressBlock
        }

    }
}