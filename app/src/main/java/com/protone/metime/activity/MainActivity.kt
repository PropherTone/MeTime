package com.protone.metime.activity

import androidx.activity.viewModels
import androidx.core.view.isGone
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import com.protone.component.R
import com.protone.common.baseType.*
import com.protone.common.context.MApplication
import com.protone.common.context.clipOutLine
import com.protone.common.context.onGlobalLayout
import com.protone.common.context.root
import com.protone.common.entity.GalleryMedia
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
import com.protone.component.database.MediaAction
import com.protone.component.database.userConfig
import com.protone.component.toGallery
import com.protone.component.view.customView.musicPlayer.getBitmap
import com.protone.metime.adapter.TimeListAdapter
import com.protone.metime.component.TimeListItemDecoration
import com.protone.metime.databinding.MainActivityBinding
import com.protone.metime.viewModel.MainViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity :
    BaseMusicActivity<MainActivityBinding, MainViewModel, MainViewModel.MainViewEvent>() {
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
                    binding.userBack.setBlurBitmap(withDefaultContext { value.toBitmap() }, 10, 10)
                }
            }
            field = value
        }

    override fun createView(): MainActivityBinding {
        return MainActivityBinding.inflate(layoutInflater, root, false).apply {
            activity = this@MainActivity
            motionHeader.fitStatuesBarUsePadding()
            musicPlayer.clipOutLine(
                resources.getDimensionPixelSize(com.protone.metime.R.dimen.item_radius).toFloat()
            )
            root.onGlobalLayout {
                actionBtnContainer.also {
                    it.y = it.y + viewModel.btnH * 2
                    viewModel.btnY = it.y
                }
                musicPlayer.duration = userConfig.lastMusicProgress
            }
            mainHeader.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
                val headerProgress = -verticalOffset / appBarLayout.totalScrollRange.toFloat()
                actionBtnContainer.y = viewModel.btnY - (viewModel.btnH * headerProgress) * 2
            }
        }
    }

    override suspend fun MainViewModel.init() {
        val musicController = MusicControllerIMP(binding.musicPlayer)
        userIcon = userConfig.userIcon.also {
            musicController.setInterceptAlbumCover(it.isEmpty())
        }
        musicController.setOnBlurAlbumCover {
            binding.userBack.setImageBitmap(it)
        }
        musicController.onClick {
            startActivity(RouterPath.MusicRouterPath.MusicPlayer)
        }

        onLifecycleEvent {
            onResume {
                userName = userConfig.userName
                userIcon = userConfig.userIcon.also {
                    musicController.setInterceptAlbumCover(it.isEmpty())
                }
            }
        }

        initTimeList()

        musicController.bindMusicService()

        observeViewEvent()
    }

    private suspend fun MusicControllerIMP.bindMusicService() {
        bindMusicService {
            setBinder(this@MainActivity, it) { loopMode ->
                userConfig.musicLoopMode = loopMode
            }
            setLoopMode(userConfig.musicLoopMode)
            launchDefault {
                viewModel.getMusics(userConfig.lastMusicBucket)?.let { list ->
                    setMusicList(list)
                    if (isPlaying() == true) {
                        refreshPlayer()
                        return@let
                    }
                    refresh(
                        if (userConfig.lastMusic.isNotEmpty()) {
                            userConfig.lastMusic.toEntity(Music::class.java)
                        } else if (list.isNotEmpty()) list[0] else getEmptyMusic(),
                        userConfig.lastMusicProgress
                    )
                }
            }
        }
    }

    private fun observeViewEvent() {
        onViewEvent {
            when (it) {
                MainViewModel.MainViewEvent.Gallery ->
                    toGallery {
                        withTransition(R.anim.card_in_ltr, R.anim.card_out_ltr)
                    }
                MainViewModel.MainViewEvent.Note ->
                    if (userConfig.lockNote == "")
                        startActivity(RouterPath.NoteRouterPath.Main) {
                            withTransition(R.anim.card_in_rtl, R.anim.card_out_rtl)
                        }
                    else R.string.locked.getString().toast()
                MainViewModel.MainViewEvent.Music ->
                    if (userConfig.lockMusic == "") {
                        startActivity(RouterPath.MusicRouterPath.Main) {
                            withTransition(R.anim.card_top_in, R.anim.card_top_out)
                        }
                        userConfig.lastMusicBucketCover.getBitmap()
                    } else R.string.locked.getString().toast()
                MainViewModel.MainViewEvent.UserConfig -> {
                    startActivity(RouterPath.ConfigRouterPath.UserConfig)
                }
            }
        }
    }

    private fun initTimeList() {
        binding.timeList.apply {
            updateLayoutParams {
                height = MApplication.screenHeight
            }
            layoutManager = LinearLayoutManager(this@MainActivity)
            addItemDecoration(
                TimeListItemDecoration(
                    resources.getDimensionPixelSize(R.dimen.main_margin),
                    binding.actionBtnContainer.measuredHeight
                )
            )
            TimeListAdapter(object : TimeListAdapter.CardEvent {
                override fun onPhotoClick(media: GalleryMedia) {
                    startActivity(RouterPath.GalleryRouterPath.GalleryView) {
                        galleryViewPostcard(media.toJson(), false, isCustom = false, ALL_GALLERY)
                    }
                }

                override fun onVideoClick(media: GalleryMedia) {
                    startActivity(RouterPath.GalleryRouterPath.GalleryView) {
                        galleryViewPostcard(media.toJson(), true, isCustom = false, ALL_GALLERY)
                    }
                }
            }).let { timeListAdapter ->
                adapter = timeListAdapter
                launchDefault {
                    viewModel.getTimeMediaPager(2).collect {
                        timeListAdapter.submitData(it)
                    }
                }
                launchDefault observer@{
                    viewModel.observeGalleryData {
                        when (it) {
                            is MediaAction.GalleryDataAction.OnGalleryMediasInserted ->
                                if (timeListAdapter.itemCount <= 0) timeListAdapter.retry()
                            else -> Unit
                        }
                        this@observer.cancel()
                    }
                }
            }
        }
    }

}