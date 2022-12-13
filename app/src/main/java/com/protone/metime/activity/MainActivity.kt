package com.protone.metime.activity

import androidx.activity.viewModels
import androidx.core.view.isGone
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.protone.common.R
import com.protone.common.baseType.*
import com.protone.common.context.intent
import com.protone.common.context.onGlobalLayout
import com.protone.common.context.root
import com.protone.common.entity.GalleryMedia
import com.protone.component.database.dao.DatabaseBridge
import com.protone.component.database.userConfig
import com.protone.common.entity.Music
import com.protone.common.entity.getEmptyMusic
import com.protone.common.utils.ALL_GALLERY
import com.protone.common.utils.RouterPath
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryViewWire.galleryViewPostcard
import com.protone.common.utils.displayUtils.imageLoader.Image
import com.protone.common.utils.displayUtils.imageLoader.constant.DiskCacheStrategy
import com.protone.common.utils.json.toEntity
import com.protone.common.utils.json.toJson
import com.protone.common.utils.todayDate
import com.protone.component.BaseMusicActivity
import com.protone.component.MusicControllerIMP
import com.protone.component.service.WorkService
import com.protone.metime.adapter.TimeListAdapter
import com.protone.metime.databinding.MainActivityBinding
import com.protone.metime.viewModel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity :
    BaseMusicActivity<MainActivityBinding, MainViewModel, MainViewModel.MainViewEvent>(true) {
    override val viewModel: MainViewModel by viewModels()

    private var userName: String? = null
        set(value) {
            if (value == null) return
            binding.userWelcome.text = if (value == "") getString(R.string.welcome_msg) else value
            binding.userDate.text = todayDate("yyyy/MM/dd")
            field = value
        }
        get() = binding.userWelcome.text.toString()

    private var userIcon: String? = null
        set(value) {
            binding.userIcon.isGone = value == null || value.isEmpty()
            if (value == null) return
            if (field == value) return
            if (value.isNotEmpty()) {
                Image.load(value)
                    .with(this)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(binding.userIcon)
                launch {
                    binding.userBack.setImageBitmap(viewModel.loadBlurIcon(value))
                }
            }
            field = value
        }

    override fun createView(): MainActivityBinding {
        return MainActivityBinding.inflate(layoutInflater, root, false).apply {
            activity = this@MainActivity
            toolMotion.fitStatuesBarUsePadding()
            root.onGlobalLayout {
                actionBtnContainer.also {
                    it.y = it.y + viewModel.btnH * 2
                    viewModel.btnY = it.y
                }
                musicPlayer.duration = userConfig.lastMusicProgress
            }
        }
    }

    override suspend fun MainViewModel.init() {
        val musicController = MusicControllerIMP(binding.musicPlayer)
        musicController.setOnBlurAlbumCover {
            binding.userBack.setImageBitmap(it)
        }
        musicController.onClick {
            startActivity(RouterPath.MusicRouterPath.MusicPlayer)
        }
        userIcon = userConfig.userIcon.also {
            musicController.setInterceptAlbumCover(it.isEmpty())
        }

        onLifecycleEvent {
            onResume {
                userName = userConfig.userName
                userIcon = userConfig.userIcon.also {
                    musicController.setInterceptAlbumCover(it.isEmpty())
                }
            }
            onFinish {
                userConfig.lastMusicProgress = musicController.getProgress() ?: 0L
                userConfig.lastMusic = musicController.getPlayingMusic()?.toJson() ?: ""
            }
        }

        initTimeList()

        bindMusicService {
            musicController.setBinder(this@MainActivity, it) { loopMode ->
                userConfig.musicLoopMode = loopMode
            }
            musicController.setLoopMode(userConfig.musicLoopMode)
            launchDefault {
                getMusics(userConfig.lastMusicBucket)?.let { list ->
                    list as MutableList<Music>
                    musicController.setMusicList(list)
                    musicController.refresh(
                        if (userConfig.lastMusic.isNotEmpty())
                            userConfig.lastMusic.toEntity(Music::class.java)
                        else if (list.isNotEmpty()) list[0] else getEmptyMusic(),
                        userConfig.lastMusicProgress
                    )
                }
            }
        }

        onViewEvent {
            when (it) {
                MainViewModel.MainViewEvent.Gallery ->
                    startActivity(RouterPath.GalleryRouterPath.Main)
                MainViewModel.MainViewEvent.Note ->
                    if (userConfig.lockNote == "")
                        startActivity(RouterPath.NoteRouterPath.Main)
                    else R.string.locked.getString().toast()
                MainViewModel.MainViewEvent.Music ->
                    if (userConfig.lockMusic == "")
                        startActivity(RouterPath.MusicRouterPath.Main)
                    else R.string.locked.getString().toast()
                MainViewModel.MainViewEvent.UserConfig -> {
                    startActivity(RouterPath.ConfigRouterPath.UserConfig)
                }
            }
        }
    }

    private fun initTimeList() {
        binding.timeList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = TimeListAdapter(object : TimeListAdapter.CardEvent {
                override fun onPhotoClick(media: GalleryMedia) {
                    startActivity(RouterPath.GalleryRouterPath.GalleryView) {
                        galleryViewPostcard(media.toJson(), false, ALL_GALLERY)
                    }
                }

                override fun onVideoClick(media: GalleryMedia) {
                    startActivity(RouterPath.GalleryRouterPath.GalleryView) {
                        galleryViewPostcard(media.toJson(), true, ALL_GALLERY)
                    }
                }
            })
        }
    }
}