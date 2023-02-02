package com.protone.music.activity

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.transition.TransitionManager
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.protone.common.context.root
import com.protone.common.context.statuesBarHeight
import com.protone.component.database.userConfig
import com.protone.common.entity.Music
import com.protone.component.BaseMusicActivity
import com.protone.component.BaseViewModel
import com.protone.component.MusicControllerIMP
import com.protone.common.context.activities
import com.protone.common.utils.RouterPath
import com.protone.music.adapter.TransparentPlayListAdapter
import com.protone.component.R
import com.protone.component.view.customView.blurView.DefaultBlurController
import com.protone.component.view.customView.blurView.DefaultBlurEngine
import com.protone.music.adapter.MusicItemDecoration
import com.protone.music.databinding.MusicViewActivityBinding
import com.protone.music.viewModel.MusicViewModel

@Route(path = RouterPath.MusicRouterPath.MusicPlayer)
class MusicViewActivity :
    BaseMusicActivity<MusicViewActivityBinding, MusicViewModel, BaseViewModel.ViewEvent>() {
    override val viewModel: MusicViewModel by viewModels()

    override fun createView(): MusicViewActivityBinding {
        return MusicViewActivityBinding.inflate(layoutInflater, root, false).apply {
            activity = this@MusicViewActivity
            toolBar.layoutParams = toolBar.layoutParams.apply { height = statuesBarHeight }
            pop.initBlurTool(DefaultBlurController(root as ViewGroup, DefaultBlurEngine())).apply {
                setMaskColor(Color.BLACK)
                setMaskXfMode(PorterDuff.Mode.SCREEN)
                setBlurRadius(16f)
            }
            root.viewTreeObserver.addOnPreDrawListener {
                if (!pop.isGone) {
                    pop.renderFrame()
                }
                true
            }
        }
    }

    override suspend fun MusicViewModel.init() {
        val musicController = MusicControllerIMP(binding.musicPlayer)
        bindMusicService { binder ->
            musicController.setBinder(this@MusicViewActivity, binder) {
                userConfig.musicLoopMode = it
            }
            musicController.refresh()
            musicController.setLoopMode(userConfig.musicLoopMode)
            initPlayList(
                binder.getPlayList(), musicController.getPlayingMusic(),
                object : TransparentPlayListAdapter.OnPlayListClk {
                    override fun onClk(music: Music) {
                        if (musicController.getPlayingMusic() == music) {
                            return
                        }
                        musicController.play(music)
                    }
                })
            musicController.binder?.onMusicPlaying()?.observe(this@MusicViewActivity) {
                (binding.playList.adapter as TransparentPlayListAdapter).setOnPlay(it)
            }
        }
    }

    private fun initPlayList(
        playList: List<Music>, onPlay: Music?,
        listener: TransparentPlayListAdapter.OnPlayListClk
    ) {
        binding.playList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = TransparentPlayListAdapter(context, onPlay, playList).also {
                it.onPlayListClkListener = listener
            }
            addItemDecoration(MusicItemDecoration(resources.getDimensionPixelSize(R.dimen.small_elevation)))
        }
    }

    fun showPop() {
        TransitionManager.beginDelayedTransition(binding.root as ViewGroup?)
        binding.pop.isGone = !binding.pop.isGone
    }

    override suspend fun doFinish() {
        if (activities.size == 1 && activities.contains(this)) {
            startActivity(Intent(this, Class.forName("com.protone.metime.activity.SplashActivity")))
        }
    }
}