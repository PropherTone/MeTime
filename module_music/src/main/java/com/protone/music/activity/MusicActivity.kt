package com.protone.music.activity

import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.view.isGone
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.protone.common.R
import com.protone.common.baseType.getDrawable
import com.protone.common.baseType.getString
import com.protone.common.baseType.toast
import com.protone.common.baseType.withDefaultContext
import com.protone.common.context.*
import com.protone.common.entity.MusicBucket
import com.protone.common.utils.ALL_MUSIC
import com.protone.common.utils.RouterPath
import com.protone.component.BaseMusicActivity
import com.protone.component.MusicControllerIMP
import com.protone.component.database.userConfig
import com.protone.component.view.customView.StatusImageView
import com.protone.component.view.customView.blurView.DefaultBlurController
import com.protone.component.view.customView.blurView.DefaultBlurEngine
import com.protone.component.view.customView.musicPlayer.getBitmap
import com.protone.music.adapter.MusicBucketAdapter
import com.protone.music.adapter.MusicListAdapter
import com.protone.music.databinding.MusicActivityBinding
import com.protone.music.viewModel.AddBucketViewModel
import com.protone.music.viewModel.MusicModel
import com.protone.music.viewModel.MusicModel.MusicEvent
import com.protone.music.viewModel.MusicModel.MusicViewEvent
import com.protone.music.viewModel.PickMusicViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

@Route(path = RouterPath.MusicRouterPath.Main)
class MusicActivity :
    BaseMusicActivity<MusicActivityBinding, MusicModel, MusicViewEvent>(true),
    StatusImageView.StateListener {

    private var doBlur = true
    private var playerFitTopH = 0

    override val viewModel: MusicModel by viewModels()

    override fun createView(): MusicActivityBinding {
        return MusicActivityBinding.inflate(layoutInflater, root, false).apply {
            runBlocking {
                blurredBucketCover.setBlurBitmap(
                    userConfig.lastMusicBucketCover.getBitmap(),
                    24,
                    10
                )
            }
            activity = this@MusicActivity
            musicBucketContainer.fitStatuesBar()
            mySmallMusicPlayer.interceptAlbumCover = true
            playerFitTopH = toolbar.minHeight + statuesBarHeight
            translatePlayerCover(true)
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
                appToolbar.paddingTop(appToolbar.paddingTop + statuesBarHeight)
                musicBucketContainer.botBlock = resources
                    .getDimensionPixelSize(R.dimen.model_icon_dimen).toFloat()
                musicShowBucket.setOnStateListener(this@MusicActivity)
                appToolbar.setExpanded(false, false)
            }
            appToolbar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
                //TODO(解决抖动问题)
                toolbar.progress = -verticalOffset / appBarLayout.totalScrollRange.toFloat()
            }
        }
    }

    override suspend fun MusicModel.init() {
        val controller = MusicControllerIMP(binding.mySmallMusicPlayer)
        controller.setOnBlurAlbumCover { binding.musicPlayerCover.setImageBitmap(it) }

        observeEvent(controller)
        observeMusicEvent()

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

    private fun MusicModel.observeEvent(controller: MusicControllerIMP) {
        onViewEvent {
            when (it) {
                is MusicViewEvent.PlayMusic -> withDefaultContext {
                    if (lastBucket != binding.musicBucketName.text) {
                        getMusicListAdapter()?.getPlayList()?.let { list ->
                            lastBucket = binding.musicBucketName.text.toString()
                            controller.setMusicList(list)
                        }
                    }
                    controller.play(it.music)
                }
                is MusicViewEvent.SetBucketCover -> getBucket(it.name)?.let { mb ->
                    userConfig.lastMusicBucketCover = mb.icon ?: ""
                    binding.apply {
                        if (mb.icon != null) {
                            mb.icon?.getBitmap()?.let { bm ->
                                musicBucketIcon.setImageBitmap(bm)
                                binding.blurredBucketCover.setBlurBitmap(bm, 24, 10)
                            }
                        } else {
                            blurredBucketCover.setImageDrawable(mySmallMusicPlayer.baseCoverDrawable)
                            musicBucketIcon.setImageDrawable(com.protone.component.R.drawable.ic_baseline_music_note_24.getDrawable())
                        }
                        musicBucketName.text = mb.name
                        musicBucketNamePhanton.text = mb.name
                        musicBucketMsg.text =
                            if (mb.date != null && mb.detail != null) "${mb.date} ${mb.detail}" else R.string.none.getString()
                    }
                }
                is MusicViewEvent.AddMusic -> {
                    if (it.bucket == ALL_MUSIC) {
                        R.string.bruh.getString().toast()
                        return@onViewEvent
                    }
                    startActivityForResult(
                        PickMusicActivity::class.intent.putExtra(
                            PickMusicViewModel.BUCKET_NAME,
                            it.bucket
                        )
                    ).run {
                        getBucketRefreshed(it.bucket)?.let { mb ->
                            refreshListAndBucket(mb)
                        }
                    }
                }
                is MusicViewEvent.Edit -> withDefaultContext {
                    if (it.bucket == ALL_MUSIC) {
                        R.string.bruh.getString().toast()
                        return@withDefaultContext
                    }
                    val ar = startActivityForResult(
                        AddBucketActivity::class.intent.putExtra(
                            AddBucketViewModel.BUCKET_NAME,
                            it.bucket
                        )
                    )
                    ar?.also { re ->
                        if (re.resultCode != RESULT_OK) return@also
                        re.data?.getStringExtra(AddBucketViewModel.BUCKET_NAME)?.let { name ->
                            sendViewEvent(MusicViewEvent.RefreshBucket(name))
                        }
                    }
                }
                is MusicViewEvent.Delete -> {
                    if (it.bucket == ALL_MUSIC) {
                        R.string.bruh.getString().toast()
                        return@onViewEvent
                    }
                    val musicBucket = tryDeleteMusicBucket(it.bucket)
                    if (musicBucket == null) R.string.failed_msg.getString().toast()
                }
                is MusicViewEvent.RefreshBucket -> {
                    getBucket(it.newName)?.let { bucket ->
                        getMusicBucketAdapter()?.refreshBucket(bucket)
                    }
                }
                is MusicViewEvent.AddMusicBucket -> {
                    val re = startActivityForResult(AddBucketActivity::class.intent)
                    when (re?.resultCode) {
                        RESULT_CANCELED -> R.string.cancel.getString().toast()
                    }
                }
                is MusicViewEvent.AddBucket ->
                    getBucket(it.bucket)?.let { mb -> getMusicBucketAdapter()?.addBucket(mb) }
                is MusicViewEvent.DeleteBucket ->
                    getBucket(it.bucket)?.let { mb -> getMusicBucketAdapter()?.deleteBucket(mb) }
                is MusicViewEvent.Locate -> {
                    binding.appToolbar.setExpanded(false, false)
                    getMusicListAdapter()?.getPlayingPosition()?.let { position ->
                        if (position != -1) binding.musicMusicList.smoothScrollToPosition(position)
                    }
                }
                is MusicViewEvent.Search ->
                    startActivity(
                        PickMusicActivity::class.intent
                            .putExtra(PickMusicViewModel.BUCKET_NAME, lastBucket)
                            .putExtra(PickMusicViewModel.MODE, PickMusicViewModel.SEARCH_MUSIC)
                    )
            }
        }
    }

    private fun MusicModel.observeMusicEvent() {
//        getMusicBucketAdapter()?.addBucket(musicBucket)
//        refreshListAndBucket(musicBucket)
//        getMusicBucketAdapter()?.deleteBucket(musicBucket)
        observeMusicEvent {
            when (it) {
                is MusicEvent.OnMusicBucketInsert -> {
                    getMusicBucketAdapter()?.addBucket(it.musicBucket)
                }
                is MusicEvent.OnMusicBucketUpdated -> {
                    getMusicBucketAdapter()?.refreshBucket(it.musicBucket)
                }
                is MusicEvent.OnMusicBucketDeleted -> {
                    getMusicBucketAdapter()?.deleteBucket(it.musicBucket)
                }
                is MusicEvent.OnMusicInsert -> {
                    getMusicListAdapter()?.addMusic(it.music)
                }
                is MusicEvent.OnMusicsInsert -> {}
                is MusicEvent.OnMusicDeleted -> {}
                is MusicEvent.OnMusicUpdated -> {}
                else -> Unit
            }
        }
    }

    private fun initMusicList() {
        binding.musicMusicList.apply {
            layoutManager = LinearLayoutManager(this@MusicActivity)
            adapter = MusicListAdapter(this@MusicActivity, mutableListOf()).apply {
                clickCallback = { sendViewEvent(MusicViewEvent.PlayMusic(it)) }
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
                setData(allMusicBucket)
                getBucket(lastBucket)?.let {
                    if (binding.musicBucketName.text != it.name) {
                        sendViewEvent(MusicViewEvent.SetBucketCover(it.name))
                        getMusicListAdapter()?.changeData(getCurrentMusicList(it))
                    }
                }
                musicBucketEventListener = object : MusicBucketAdapter.MusicBucketEvent {
                    override fun onBucketClicked(musicBucket: MusicBucket) {
                        launch {
                            hideBucket()
                            if (binding.musicBucketName.text == musicBucket.name) return@launch
                            sendViewEvent(MusicViewEvent.SetBucketCover(musicBucket.name))
                            switchMusicBucket(musicBucket)
                        }
                    }

                    override fun addMusic(bucket: String, position: Int) =
                        sendViewEvent(MusicViewEvent.AddMusic(bucket))

                    override fun delete(bucket: String, position: Int) =
                        sendViewEvent(MusicViewEvent.Delete(bucket))

                    override fun edit(bucket: String, position: Int) =
                        sendViewEvent(MusicViewEvent.Edit(bucket))
                }
            }
        }
    }

    private suspend fun MusicModel.refreshListAndBucket(musicBucket: MusicBucket) {
        getMusicBucketAdapter()?.refreshBucket(musicBucket)
        if (binding.musicBucketName.text == musicBucket.name) {
            getMusicListAdapter()?.changeData(getCurrentMusicList(musicBucket))
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
        sendViewEvent(MusicViewEvent.Edit(binding.musicBucketName.text.toString()))
    }

    fun sendDelete() {
        sendViewEvent(MusicViewEvent.Delete(binding.musicBucketName.text.toString()))
    }

    fun sendAddMusic() {
        sendViewEvent(MusicViewEvent.AddMusic(binding.musicBucketName.text.toString()))
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
                    translatePlayerCover(true)
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
                translatePlayerCover(false)
            }, onEnd = {
                musicBucketContainer.setWillMove(false)
                doBlur = false
            })
        }
    }

    override fun getSwapAnim(): Pair<Int, Int> {
        return Pair(
            com.protone.component.R.anim.card_bot_in,
            com.protone.component.R.anim.card_bot_out
        )
    }

    private fun MusicActivityBinding.translatePlayerCover(fitTop: Boolean) {
        TransitionManager.beginDelayedTransition(musicBucketContainer)
        musicPlayerCover.updateLayoutParams {
            if (fitTop) height += playerFitTopH
            else height -= playerFitTopH
        }
    }

}