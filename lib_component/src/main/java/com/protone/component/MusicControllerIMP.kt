package com.protone.component

import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.protone.common.baseType.getString
import com.protone.common.context.*
import com.protone.common.entity.Music
import com.protone.component.broadcast.musicBroadCastManager
import com.protone.component.service.MusicBinder
import com.protone.component.view.customView.Bubble
import com.protone.component.view.customView.ColorfulProgressBar
import com.protone.component.view.customView.musicPlayer.BaseMusicPlayer

class MusicControllerIMP(private val controller: BaseMusicPlayer) {
    var binder: MusicBinder? = null

    companion object {
        const val LOOP_SINGLE = 0
        const val LOOP_LIST = 1
        const val PLAY_LIST = 2
        const val NO_LOOP = 3
        const val RANDOM = 4
    }

    private var loop = 1

    private val bubble = Bubble(controller.context)

    fun setBinder(
        lifecycle: LifecycleOwner,
        binder: MusicBinder,
        onPlaying: ((Music) -> Unit)? = null,
        onLoop: ((Int) -> Unit)? = null
    ) {
        this.binder = binder
        controller.apply {
            control.setOnClickListener {
                musicBroadCastManager.sendBroadcast(Intent(MUSIC_PLAY))
            }
            next?.setOnClickListener {
                musicBroadCastManager.sendBroadcast(Intent(MUSIC_NEXT))
            }
            previous?.setOnClickListener {
                musicBroadCastManager.sendBroadcast(Intent(MUSIC_PREVIOUS))
            }
            looper?.setOnClickListener {
                if (loop == 4) loop = 0
                this@MusicControllerIMP.binder?.setLoopMode(++loop)
                setLoopMode(loop, it)
                onLoop?.invoke(loop)
            }
            this@MusicControllerIMP.binder?.run {
                refresh()
                progress?.let {
                    it.progressListener = object : ColorfulProgressBar.Progress {
                        override fun getProgress(position: Long) {
                            setProgress(position)
                        }
                    }
                    onProgress().observe(lifecycle) { progress ->
                        it.barSeekTo(progress)
                    }
                }
                onPlayState().observe(lifecycle) {
                    isPlay = it
                }
                onMusicPlaying().observe(lifecycle) {
                    if (it != null) {
                        setDetail(it)
                        onPlaying?.invoke(it)
                    }
                }
            }
        }
    }

    fun refresh() {
        binder?.apply {
            controller.apply {
                if (progress != null) onProgress().value?.let { progress?.barSeekTo(it) }
                onPlayState().value?.let { isPlay = it }
                onMusicPlaying().value?.let { setDetail(it) }
            }
        }
    }

    fun onClick(func: () -> Unit) {
        controller.root.setOnClickListener {
            func.invoke()
        }
    }

    fun setLoopMode(mode: Int, targetView: View? = null) {
        loop = mode
        when (mode) {
            LOOP_LIST -> {
                controller.looper?.setImageResource(R.drawable.ic_round_repeat_24_white)
                showToast(targetView, R.string.loop_list.getString())
            }
            LOOP_SINGLE -> {
                controller.looper?.setImageResource(R.drawable.ic_round_repeat_one_24_white)
                showToast(targetView, R.string.loop_single.getString())
            }
            PLAY_LIST -> {
                controller.looper?.setImageResource(R.drawable.ic_round_playlist_play_24_white)
                showToast(targetView, R.string.play_list.getString())
            }
            NO_LOOP -> {
                controller.looper?.setImageResource(R.drawable.ic_round_block_24_white)
                showToast(targetView, R.string.no_loop.getString())
            }
            RANDOM -> {
                controller.looper?.setImageResource(R.drawable.ic_round_loop_24_white)
                showToast(targetView, R.string.random.getString())
            }
        }
        binder?.setLoopMode(mode)
    }

    fun refresh(music: Music, progress: Long) {
        binder?.init(music, progress)
        musicBroadCastManager.sendBroadcast(Intent(MUSIC_REFRESH))
    }

    fun refreshPlayer() {
        getPlayingMusic()?.let { setDetail(it) }
    }

    fun play(music: Music?) {
        binder?.play(music)
        musicBroadCastManager.sendBroadcast(Intent(MUSIC_PLAY_CUR))
    }

    fun setMusicList(list: List<Music>) {
        binder?.setPlayList(list)
        musicBroadCastManager.sendBroadcast(Intent(MUSIC_REFRESH))
    }

    fun getPlayingMusic() = binder?.onMusicPlaying()?.value

    fun getProgress() = controller.progress?.barDuration

    fun isPlaying() = binder?.isPlaying()

    fun setInterceptAlbumCover(intercept: Boolean) {
        controller.interceptAlbumCover = intercept
    }

    fun setOnBlurAlbumCover(block: (Bitmap?) -> Unit) {
        controller.onBlurAlbumCover(block)
    }

    private fun setDetail(it: Music) {
        controller.apply {
            post {
                if (cover != it.uri) cover = it.uri
                if (duration != it.duration) duration = it.duration
                setName(it.title)
                setDetail("${it.artist ?: "ARTIST"}·${it.album ?: "NONE"}")
            }
        }
    }

    private fun showToast(target: View?, msg: CharSequence) {
        if (target == null) return
        bubble.setText(msg).showBubble(target)
    }
}