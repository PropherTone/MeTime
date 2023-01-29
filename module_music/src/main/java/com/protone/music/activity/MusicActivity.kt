package com.protone.music.activity

import androidx.activity.viewModels
import androidx.core.view.isGone
import androidx.core.view.updateLayoutParams
import androidx.databinding.ObservableField
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.protone.common.R
import com.protone.common.baseType.getDrawable
import com.protone.common.baseType.getString
import com.protone.common.baseType.toast
import com.protone.common.baseType.withDefaultContext
import com.protone.common.context.intent
import com.protone.common.context.onGlobalLayout
import com.protone.common.context.root
import com.protone.common.context.statuesBarHeight
import com.protone.common.entity.Music
import com.protone.common.entity.MusicBucket
import com.protone.common.utils.ALL_MUSIC
import com.protone.common.utils.RouterPath
import com.protone.component.BaseMusicActivity
import com.protone.component.MusicControllerIMP
import com.protone.component.database.userConfig
import com.protone.component.view.customView.StatusImageView
import com.protone.component.view.customView.musicPlayer.getBitmap
import com.protone.music.adapter.MusicBucketAdapter
import com.protone.music.adapter.MusicListAdapter
import com.protone.music.databinding.MusicActivityLayoutBinding
import com.protone.music.viewModel.AddBucketViewModel
import com.protone.music.viewModel.MusicModel
import com.protone.music.viewModel.MusicModel.MusicEvent
import com.protone.music.viewModel.MusicModel.MusicViewEvent
import com.protone.music.viewModel.PickMusicViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Route(path = RouterPath.MusicRouterPath.Main)
class MusicActivity : BaseMusicActivity<MusicActivityLayoutBinding, MusicModel, MusicViewEvent>(),
    StatusImageView.StateListener {

    override val viewModel: MusicModel by viewModels()

    internal class BindingViewModel {
        lateinit var binding: MusicActivityLayoutBinding
        lateinit var activity: MusicActivity
        val isBucketOpen: ObservableField<Boolean> = ObservableField()

        fun search() {
            activity.sendViewEvent(MusicViewEvent.Search)
        }

        fun locateMusic() {
            activity.sendViewEvent(MusicViewEvent.Locate)
        }

        fun addMusicBucket() {
            activity.sendViewEvent(MusicViewEvent.AddMusicBucket)
        }

        fun sendEdit() {
            activity.sendViewEvent(MusicViewEvent.Edit(binding.musicBucketName.text.toString()))
        }

        fun sendDelete() {
            activity.sendViewEvent(MusicViewEvent.Delete(binding.musicBucketName.text.toString()))
        }

        fun sendAddMusic() {
            activity.sendViewEvent(MusicViewEvent.AddMusic(binding.musicBucketName.text.toString()))
        }

        fun finish() {
            activity.finish()
        }
    }

    override fun createView(): MusicActivityLayoutBinding {
        return MusicActivityLayoutBinding.inflate(layoutInflater, root, false).apply {
            runBlocking {
                blurredBucketCover.setBlurBitmap(
                    userConfig.lastMusicBucketCover.getBitmap(),
                    24,
                    10
                )
            }

            model = BindingViewModel().also {
                it.activity = this@MusicActivity
                it.binding = this
            }

            musicBucketContainer.fitStatuesBar()
            mySmallMusicPlayer.interceptAlbumCover = true
            viewModel.playerFitTopH = musicFinish.minimumHeight + statuesBarHeight
            translatePlayerCoverToFit(true)

            root.onGlobalLayout {
                musicBucketContainer.botBlock = resources
                    .getDimensionPixelSize(R.dimen.model_icon_dimen).toFloat()
                musicShowBucket.setOnStateListener(this@MusicActivity)
            }
        }
    }

    override suspend fun MusicModel.init() {
        val controller = MusicControllerIMP(binding.mySmallMusicPlayer)
        controller.setOnBlurAlbumCover { binding.musicPlayerCover.setImageBitmap(it) }

        observeEvent(controller)
        observeMusicEvent()

        initMusicBucketList()
        initMusicList()
        onLifecycleEvent {
            onResume {
                if (isInit) isInit = true
            }
            onPause {
                isInit = false
            }
        }
        isInit = true
        bindMusicService {
            controller.getPlayingMusic()?.let { music ->
                getMusicListAdapter()?.playPosition(music)
            }
            controller.setBinder(this@MusicActivity, it, onPlaying = { music ->
                getMusicListAdapter()?.playPosition(music)
            })
            binding.apply {
                controller.onClick { musicShowBucket.performClick() }
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
                is MusicViewEvent.AddMusic -> {
                    if (it.bucket == ALL_MUSIC) {
                        R.string.bruh.getString().toast()
                        return@onViewEvent
                    }
                    startActivity(
                        PickMusicActivity::class.intent.putExtra(
                            PickMusicViewModel.BUCKET_NAME,
                            it.bucket
                        )
                    )
                }
                is MusicViewEvent.Edit -> withDefaultContext {
                    if (it.bucket == ALL_MUSIC) {
                        R.string.bruh.getString().toast()
                        return@withDefaultContext
                    }
                    startActivity(
                        AddBucketActivity::class.intent.putExtra(
                            AddBucketViewModel.BUCKET_NAME,
                            it.bucket
                        )
                    )
                }
                is MusicViewEvent.Delete -> {
                    if (it.bucket == ALL_MUSIC) {
                        R.string.bruh.getString().toast()
                        return@onViewEvent
                    }
                    val musicBucket = tryDeleteMusicBucket(it.bucket)
                    if (musicBucket == null) R.string.failed_msg.getString().toast()
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

        fun updateBucket(music: Music, isInsert: Boolean) {
            getMusicBucketAdapter()?.getSelectedBucket()?.takeIf { mb ->
                mb.name == binding.musicBucketName.text
            }?.let { mb ->
                val corm = if (isInsert) 1 else -1
                if (mb.name != ALL_MUSIC) getDefaultBucket().also { default ->
                    default.size = default.size + corm
                    getMusicBucketAdapter()?.refreshBucket(default)
                }
                mb.size = mb.size + corm
                getMusicBucketAdapter()?.refreshBucket(mb)
                getMusicListAdapter()?.apply {
                    if (isInsert) addMusic(music)
                    else removeMusic(music)
                }
            }
        }

        observeMusicEvent observeFun@{
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
                is MusicEvent.OnMusicsInsert -> {
                    if (currentBucket == ALL_MUSIC) {
                        getBucket(ALL_MUSIC)?.let { mb ->
                            mb.size += it.musics.size
                            getMusicBucketAdapter()?.refreshBucket(mb)
                            getMusicListAdapter()?.addMusics(it.musics)
                        }
                    } else if (currentBucket == it.bucketName) {
                        getMusicListAdapter()?.addMusics(it.musics)
                    }
                }
                is MusicEvent.OnMusicDeleted -> {
                    if (currentBucket == ALL_MUSIC) {
                        getBucket(ALL_MUSIC)?.let { mb ->
                            mb.size -= 1
                            getMusicBucketAdapter()?.refreshBucket(mb)
                            getMusicListAdapter()?.removeMusic(it.music)
                        }
                    }
                }
                is MusicEvent.OnMusicUpdated -> {
                    TODO("Update on musics will not happen")
                }
                is MusicEvent.OnMusicInsertToBucket -> {
                    val music = getMusicById(it.musicID) ?: return@observeFun
                    updateBucket(music, true)
                }
                is MusicEvent.OnMusicRemoveFromBucket -> {
                    val music = getMusicById(it.musicID) ?: return@observeFun
                    updateBucket(music, false)
                }
                else -> Unit
            }
        }
    }

    private suspend fun MusicModel.initMusicBucketList() {
        binding.musicBucket.apply {
            layoutManager = LinearLayoutManager(this@MusicActivity)
            adapter = MusicBucketAdapter(this@MusicActivity).apply {
                setData(getMusicBuckets())
                musicBucketEventListener = object : MusicBucketAdapter.MusicBucketEvent {
                    override fun onBucketClicked(musicBucket: MusicBucket) {
                        launch {
                            if (currentBucket == musicBucket.name) {
                                binding.musicShowBucket.negative()
                                return@launch
                            }
                            currentBucket = musicBucket.name
                            binding.onMusicBucketSelected(musicBucket)
                            switchMusicBucket(musicBucket)
                            binding.musicShowBucket.negative()
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

    private fun initMusicList() {
        binding.musicMusicList.apply {
            layoutManager = LinearLayoutManager(this@MusicActivity)
            adapter = MusicListAdapter(this@MusicActivity, mutableListOf()).apply {
                clickCallback = { sendViewEvent(MusicViewEvent.PlayMusic(it)) }
            }
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
            }, false
        )
    }

    private suspend fun MusicActivityLayoutBinding.onMusicBucketSelected(bucket: MusicBucket) {
        viewModel.getBucket(bucket.name)?.let { mb ->
            userConfig.lastMusicBucketCover = mb.icon ?: ""
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

    private fun getMusicBucketAdapter() = (binding.musicBucket.adapter as MusicBucketAdapter?)

    private fun getMusicListAdapter() = (binding.musicMusicList.adapter as MusicListAdapter?)

    fun MusicActivityLayoutBinding.translatePlayerCoverToFit(fitTop: Boolean) {
        TransitionManager.beginDelayedTransition(musicBucketContainer)
        musicPlayerCover.updateLayoutParams {
            if (fitTop) height += viewModel.playerFitTopH
            else height -= viewModel.playerFitTopH
        }
    }

    override fun onActive() {
        binding.model?.isBucketOpen?.set(true)
    }

    override fun onNegative() {
        binding.apply {
            if (musicBucketName.text == null || musicBucketName.text.isEmpty()) {
                launch { getMusicBucketAdapter()?.setSelect(viewModel.lastBucket) }
                return
            }
            model?.isBucketOpen?.set(false)
        }
    }

    override fun getSwapAnim(): Pair<Int, Int> {
        return Pair(
            com.protone.component.R.anim.card_bot_in,
            com.protone.component.R.anim.card_bot_out
        )
    }

}