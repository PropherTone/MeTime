package com.protone.music.activity

import android.os.Bundle
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.marginBottom
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.protone.common.baseType.*
import com.protone.common.context.*
import com.protone.common.entity.MusicBucket
import com.protone.common.utils.ALL_MUSIC
import com.protone.common.utils.RouterPath
import com.protone.common.utils.displayUtils.Blur
import com.protone.common.utils.displayUtils.imageLoader.Image
import com.protone.common.utils.displayUtils.imageLoader.constant.DiskCacheStrategy
import com.protone.component.BaseMusicActivity
import com.protone.component.MusicControllerIMP
import com.protone.music.adapter.MusicBucketAdapter
import com.protone.music.adapter.MusicListAdapter
import com.protone.component.view.customView.StatusImageView
import com.protone.component.view.customView.blurView.DefaultBlurController
import com.protone.component.view.customView.blurView.DefaultBlurEngine
import com.protone.music.R
import com.protone.music.databinding.MusicActivityBinding
import com.protone.music.viewModel.AddBucketViewModel
import com.protone.music.viewModel.MusicModel
import com.protone.music.viewModel.PickMusicViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Route(path = RouterPath.MusicRouterPath.Main)
class MusicActivity :
    BaseMusicActivity<MusicActivityBinding, MusicModel, MusicModel.MusicEvent>(true),
    StatusImageView.StateListener {

    private var doBlur = true

    override val viewModel: MusicModel by lazy {
        ViewModelProvider(this).get(MusicModel::class.java).apply {
            onMusicDataEvent = object : MusicModel.OnMusicDataEvent {
                override suspend fun onNewMusicBucket(musicBucket: MusicBucket) {
                    getMusicBucketAdapter()?.addBucket(musicBucket)
                }

                override suspend fun onMusicBucketUpdated(musicBucket: MusicBucket) {
                    refreshListAndBucket(musicBucket)
                }

                override suspend fun onMusicBucketDeleted(musicBucket: MusicBucket) {
                    getMusicBucketAdapter()?.deleteBucket(musicBucket)
                }

            }
        }
    }

    override fun createView(): MusicActivityBinding {
        return MusicActivityBinding.inflate(layoutInflater, root, false).apply {
            activity = this@MusicActivity
            musicBucketContainer.fitStatuesBar()
            mySmallMusicPlayer.interceptAlbumCover = true
            musicPlayerCover.updateLayoutParams {
                height += (toolbar.minHeight + statuesBarHeight)
            }
            musicBucketContainer.initBlurTool(
                DefaultBlurController(
                    root as ViewGroup,
                    DefaultBlurEngine().also {
                        it.scaleFactor = 16f
                    })
            )
            musicBucketContainer.setForeColor(getColor(com.protone.component.R.color.foreDark))
            root.viewTreeObserver.addOnPreDrawListener {
                if (doBlur) musicBucketContainer.renderFrame()
                true
            }
            root.onGlobalLayout {
                actionBtnContainer.marginBottom(mySmallMusicPlayer.height + search.marginBottom)
                appToolbar.paddingTop(appToolbar.paddingTop + statuesBarHeight)
                musicBucketContainer.botBlock = mySmallMusicPlayer.measuredHeight.toFloat()
                musicShowBucket.setOnStateListener(this@MusicActivity)
                musicMusicList.setPadding(0, 0, 0, mySmallMusicPlayer.height)
                appToolbar.setExpanded(false, false)
            }
            appToolbar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
                toolbar.progress = -verticalOffset / appBarLayout.totalScrollRange.toFloat()
            }
        }
    }

    override suspend fun MusicModel.init() {
        val controller = MusicControllerIMP(binding.mySmallMusicPlayer)
        controller.setOnBlurAlbumCover {
            binding.musicPlayerCover.setImageBitmap(it)
        }

        onViewEvent {
            when (it) {
                is MusicModel.MusicEvent.PlayMusic -> withContext(Dispatchers.Default) {
                    if (lastBucket != binding.musicBucketName.text) {
                        getMusicListAdapter()?.getPlayList()?.let { list ->
                            lastBucket = binding.musicBucketName.text.toString()
                            controller.setMusicList(list)
                        }
                    }
                    controller.play(it.music)
                }
                is MusicModel.MusicEvent.SetBucketCover -> getBucket(it.name)?.let {
                    withContext(Dispatchers.Main) {
                        binding.apply {
                            if (it.icon != null) {
                                Image.load(it.icon).with(this@MusicActivity)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .overwrite(
                                        musicBucketIcon.measuredWidth,
                                        musicBucketIcon.measuredHeight
                                    ).into(musicBucketIcon)
                                it.icon?.toBitmap()?.let { bm ->
                                    binding.blurredBucketCover.setImageBitmap(Blur.blur(bm, 24, 10))
                                }
                            } else {
                                blurredBucketCover.setImageDrawable(mySmallMusicPlayer.baseCoverDrawable)
                                musicBucketIcon.setImageDrawable(com.protone.component.R.drawable.ic_baseline_music_note_24.getDrawable())
                            }
                            musicBucketName.text = it.name
                            musicBucketNamePhanton.text = it.name
                            musicBucketMsg.text =
                                if (it.date != null && it.detail != null) "${it.date} ${it.detail}" else com.protone.common.R.string.none.getString()
                        }
                    }
                }
                is MusicModel.MusicEvent.AddMusic -> {
                    if (it.bucket == ALL_MUSIC) {
                        com.protone.common.R.string.bruh.getString().toast()
                        return@onViewEvent
                    }
                    startActivityForResult(
                        PickMusicActivity::class.intent.putExtras {
                            putString(PickMusicViewModel.BUCKET_NAME, it.bucket)
                        }
                    ).run {
                        getBucketRefreshed(it.bucket)?.let { mb ->
                            refreshListAndBucket(mb)
                        }
                    }
                }
                is MusicModel.MusicEvent.Edit -> withDefaultContext {
                    if (it.bucket == ALL_MUSIC) {
                        com.protone.common.R.string.bruh.getString().toast()
                        return@withDefaultContext
                    }
                    val ar = startActivityForResult(
                        AddBucketActivity::class.intent.putExtras {
                            putString(AddBucketViewModel.BUCKET_NAME, it.bucket)
                        }
                    )
                    ar?.also { re ->
                        if (re.resultCode != RESULT_OK) return@also
                        re.data?.getStringExtra(AddBucketViewModel.BUCKET_NAME)?.let { name ->
                            sendViewEvent(MusicModel.MusicEvent.RefreshBucket(name))
                        }
                    }
                }
                is MusicModel.MusicEvent.Delete -> {
                    if (it.bucket == ALL_MUSIC) {
                        com.protone.common.R.string.bruh.getString().toast()
                        return@onViewEvent
                    }
                    val musicBucket = tryDeleteMusicBucket(it.bucket)
                    if (musicBucket == null) {
                        com.protone.common.R.string.failed_msg.getString().toast()
                    }
                }
                is MusicModel.MusicEvent.RefreshBucket -> {
                    getBucket(it.newName)?.let { bucket ->
                        getMusicBucketAdapter()?.refreshBucket(bucket)
                    }
                }
                is MusicModel.MusicEvent.AddMusicBucket -> {
                    val re = startActivityForResult(AddBucketActivity::class.intent)
                    when (re?.resultCode) {
                        RESULT_CANCELED -> com.protone.common.R.string.cancel.getString().toast()
                    }
                }
                is MusicModel.MusicEvent.AddBucket ->
                    getBucket(it.bucket)?.let { mb -> getMusicBucketAdapter()?.addBucket(mb) }
                is MusicModel.MusicEvent.DeleteBucket ->
                    getBucket(it.bucket)?.let { mb -> getMusicBucketAdapter()?.deleteBucket(mb) }
                is MusicModel.MusicEvent.Locate -> {
                    binding.appToolbar.setExpanded(false, false)
                    getMusicListAdapter()?.getPlayingPosition()?.let { position ->
                        if (position != -1) binding.musicMusicList.smoothScrollToPosition(position)
                    }
                }
                is MusicModel.MusicEvent.Search ->
                    startActivity(
                        PickMusicActivity::class.intent.putExtras {
                            putString(PickMusicViewModel.BUCKET_NAME, lastBucket)
                            putString(PickMusicViewModel.MODE, PickMusicViewModel.SEARCH_MUSIC)
                        }

                    )
            }
        }

        initMusicList()
        initMusicBucketList()
        bindMusicService {
            controller.getPlayingMusic()?.let { music ->
                getMusicListAdapter()?.playPosition(music)
            }
            controller.setBinder(this@MusicActivity, it, onPlaying = { music ->
                getMusicListAdapter()?.playPosition(music)
            })
            controller.onClick {
                binding.musicShowBucket.performClick()
            }
            binding.apply {
                mySmallMusicPlayer.coverSwitcher.setOnClickListener {
                    startActivity(MusicViewActivity::class.intent)
                }
                val location = intArrayOf(0, 0)
                musicBucketName.getLocationOnScreen(location)
                musicBucketNamePhanton.y = location[1].toFloat()
                musicFinish.getLocationOnScreen(location)
                musicFinishPhanton.y = location[1].toFloat()
                musicBucketNamePhanton.isGone = false
                musicFinishPhanton.isGone = false
            }
        }
    }


    private fun initMusicList() {
        binding.musicMusicList.apply {
            layoutManager = LinearLayoutManager(this@MusicActivity)
            adapter = MusicListAdapter(this@MusicActivity, mutableListOf()).apply {
                clickCallback = { sendViewEvent(MusicModel.MusicEvent.PlayMusic(it)) }
            }
        }
    }

    private suspend fun MusicModel.initMusicBucketList() {
        binding.musicBucket.apply {
            val allMusicBucket = getMusicBuckets()
            layoutManager = LinearLayoutManager(this@MusicActivity)
            adapter = MusicBucketAdapter(
                this@MusicActivity,
                getLastMusicBucket(allMusicBucket)
            ).apply {
                clickCallback = {
                    launch {
                        hideBucket()
                        if (binding.musicBucketName.text != it.name) {
                            sendViewEvent(MusicModel.MusicEvent.SetBucketCover(it.name))
                            switchMusicBucket(it)
                        }
                    }
                }
                setData(allMusicBucket)
                getBucket(lastBucket)?.let {
                    if (binding.musicBucketName.text != it.name) {
                        sendViewEvent(MusicModel.MusicEvent.SetBucketCover(it.name))
                        getMusicListAdapter()?.insertMusics(getCurrentMusicList(it))
                    }
                }
                musicBucketEventListener = object : MusicBucketAdapter.MusicBucketEvent {
                    override fun addMusic(bucket: String, position: Int) =
                        sendViewEvent(MusicModel.MusicEvent.AddMusic(bucket))

                    override fun delete(bucket: String, position: Int) =
                        sendViewEvent(MusicModel.MusicEvent.Delete(bucket))

                    override fun edit(bucket: String, position: Int) =
                        sendViewEvent(MusicModel.MusicEvent.Edit(bucket))
                }
            }
        }
    }

    private suspend fun MusicModel.refreshListAndBucket(musicBucket: MusicBucket) {
        getMusicBucketAdapter()?.refreshBucket(musicBucket)
        if (binding.musicBucketName.text == musicBucket.name) {
            getMusicListAdapter()?.insertMusics(getCurrentMusicList(musicBucket))
        }
    }

    private suspend fun MusicModel.switchMusicBucket(musicBucket: MusicBucket) {
        binding.musicMusicList.swapAdapter(
            MusicListAdapter(
                this@MusicActivity,
                getCurrentMusicList(musicBucket)
            ).apply {
                getMusicListAdapter()?.getPlayingMusic()?.let { selectList.add(it) }
                clickCallback = getMusicListAdapter()?.clickCallback
            }, true
        )
    }

    private fun getMusicBucketAdapter() = (binding.musicBucket.adapter as MusicBucketAdapter?)

    private fun getMusicListAdapter() = (binding.musicMusicList.adapter as MusicListAdapter?)

    private suspend fun hideBucket() = withContext(Dispatchers.Main) {
        binding.musicShowBucket.negative()
    }

    fun sendEdit() {
        sendViewEvent(MusicModel.MusicEvent.Edit(binding.musicBucketName.text.toString()))
    }

    fun sendDelete() {
        sendViewEvent(MusicModel.MusicEvent.Delete(binding.musicBucketName.text.toString()))
    }

    fun sendAddMusic() {
        sendViewEvent(MusicModel.MusicEvent.AddMusic(binding.musicBucketName.text.toString()))
    }

    override fun onActive() {
        binding.apply {
            appToolbar.setExpanded(false, false)
            doBlur = true
            var isDone = false
            musicBucketContainer.show(onStart = {
                musicBucketContainer.setWillMove(true)
                musicBucketNamePhanton.isGone = false
                musicFinishPhanton.isGone = false
            }, update = {
                if ((it?.animatedValue as Float) > 0.8f) {
                    if (isDone) return@show
                    isDone = true
                    TransitionManager.beginDelayedTransition(musicBucketContainer)
                    musicPlayerCover.updateLayoutParams {
                        height += (toolbar.minHeight + statuesBarHeight)
                    }
                }
            }, onEnd = {
                musicBucketContainer.setWillMove(false)
            })
        }
    }

    override fun onNegative() {
        binding.apply {
            musicBucketNamePhanton.isGone = true
            musicFinishPhanton.isGone = true
            musicBucketContainer.hide(onStart = {
                musicBucketContainer.setWillMove(true)
                TransitionManager.beginDelayedTransition(musicBucketContainer)
                musicPlayerCover.updateLayoutParams {
                    height -= (toolbar.minHeight + statuesBarHeight)
                }
            }, onEnd = {
                musicBucketContainer.setWillMove(false)
                doBlur = false
            })
        }
    }

}