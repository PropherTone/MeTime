package com.protone.metime.activity

import androidx.activity.viewModels
import androidx.core.view.isGone
import com.protone.common.context.onGlobalLayout
import com.protone.common.context.root
import com.protone.component.BaseActivity
import com.protone.common.utils.todayDate
import com.protone.common.R
import com.protone.common.baseType.*
import com.protone.common.context.intent
import com.protone.common.database.userConfig
import com.protone.common.entity.Music
import com.protone.common.entity.getEmptyMusic
import com.protone.common.utils.displayUtils.imageLoader.Image
import com.protone.common.utils.displayUtils.imageLoader.constant.GlideConfigConstant
import com.protone.common.utils.json.toEntity
import com.protone.common.utils.json.toJson
import com.protone.metime.databinding.MainActivityBinding
import com.protone.metime.viewModel.MainViewModel
import com.protone.seenn.service.WorkService
import com.protone.component.BaseMusicActivity
import com.protone.component.MusicControllerIMP
import com.protone.worker.database.dao.DatabaseHelper
import com.protone.worker.database.userConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity :
    BaseMusicActivity<MainActivityBinding, MainViewModel, MainViewModel.MainViewEvent>(true) {
    override val viewModel: MainViewModel by viewModels()

    private var userName: String? = null
        set(value) {
            if (value == null) return
            binding.userWelcome.text =
                if (value == "") getString(R.string.welcome_msg) else value
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
                    .addConfig(GlideConfigConstant.diskCacheStrategy(GlideConfigConstant.DiskCacheStrategy.NONE))
                    .into(this, binding.userIcon)
                launchIO {
                    val loadBlurIcon = viewModel.loadBlurIcon(value)
                    withMainContext {
                        binding.userBack.setImageBitmap(loadBlurIcon)
                    }
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
            startActivity(MusicViewActivity::class.intent)
        }

        onResume = {
            userName = userConfig.userName
            userIcon = userConfig.userIcon.also {
                musicController.setInterceptAlbumCover(it.isEmpty())
            }
        }

        onFinish = {
            userConfig.lastMusicProgress = musicController.getProgress() ?: 0L
            userConfig.lastMusic =
                musicController.getPlayingMusic()?.toJson() ?: ""
            DatabaseHelper.instance.shutdownNow()
            stopService(WorkService::class.intent)
        }

        refreshModelList()
        bindMusicService {
            musicController.setBinder(this@MainActivity, it) { loopMode ->
                userConfig.musicLoopMode = loopMode
            }
            musicController.setLoopMode(userConfig.musicLoopMode)
            launch(Dispatchers.Default) {
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
                    startActivity(GalleryActivity::class.intent)
                MainViewModel.MainViewEvent.Note ->
                    if (userConfig.lockNote == "")
                        startActivity(NoteActivity::class.intent)
                    else R.string.locked.getString().toast()
                MainViewModel.MainViewEvent.Music ->
                    if (userConfig.lockMusic == "")
                        startActivity(MusicActivity::class.intent)
                    else R.string.locked.getString().toast()
                MainViewModel.MainViewEvent.UserConfig ->
                    startActivity(UserConfigActivity::class.intent)
            }
        }
    }

    private suspend fun MainViewModel.refreshModelList() {
        getPhotoInToday()?.let { media ->
            Image.load(media.uri).into(this@MainActivity, binding.photoCardPhoto)
            binding.photoCardTitle.text = media.date.toDateString("yyyy/MM/dd")
            binding.timePhoto.setOnClickListener {
                startActivity(GalleryViewActivity::class.intent.apply {
                    putExtra(GalleryViewViewModel.MEDIA, media.toJson())
                    putExtra(GalleryViewViewModel.IS_VIDEO, false)
                    putExtra(GalleryViewViewModel.GALLERY, R.string.all_gallery.getString())
                })
            }
        }
        getVideoInToday()?.let { media ->
            binding.videoPlayer.setVideoPath(media.uri)
            binding.videoCardTitle.text = media.date.toDateString()
            binding.videoPlayer.setFullScreen {
                startActivity(GalleryViewActivity::class.intent.apply {
                    putExtra(GalleryViewViewModel.MEDIA, media.toJson())
                    putExtra(GalleryViewViewModel.IS_VIDEO, true)
                    putExtra(GalleryViewViewModel.GALLERY, R.string.all_gallery.getString())
                })
            }
        }
    }
}