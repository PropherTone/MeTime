package com.protone.music.activity

import android.text.method.ScrollingMovementMethod
import androidx.activity.viewModels
import androidx.core.view.updateLayoutParams
import androidx.databinding.ObservableField
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.alibaba.android.arouter.facade.annotation.Route
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
import com.protone.component.R
import com.protone.component.BaseMusicActivity
import com.protone.component.MusicControllerIMP
import com.protone.component.database.userConfig
import com.protone.component.view.customView.StatusImageView
import com.protone.component.view.customView.musicPlayer.getBitmap
import com.protone.music.adapter.MusicBucketAdapter
import com.protone.music.adapter.MusicListAdapter
import com.protone.music.databinding.MusicActivityBinding
import com.protone.music.viewModel.AddBucketViewModel
import com.protone.music.viewModel.MusicModel
import com.protone.music.viewModel.MusicModel.MusicEvent
import com.protone.music.viewModel.MusicModel.MusicViewEvent
import com.protone.music.viewModel.PickMusicViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private typealias musicR = com.protone.music.R.drawable

@Route(path = RouterPath.MusicRouterPath.Main)
class MusicActivity : BaseMusicActivity<MusicActivityBinding, MusicModel, MusicViewEvent>(),
    StatusImageView.StateListener {

    override val viewModel: MusicModel by viewModels()

    internal class BindingViewModel {
        lateinit var activity: MusicActivity
        val isContainerOpen = ObservableField(true)
        lateinit var binding: MusicActivityBinding

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

    override fun createView(): MusicActivityBinding {
        return MusicActivityBinding.inflate(layoutInflater, root, false).apply {
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

            val barHeight = statuesBarHeight
            musicFinish.y += barHeight
            musicModelContainer.fitStatuesBar()
            musicBucketContainer.fitStatuesBar()
            mySmallMusicPlayer.interceptAlbumCover = true
            viewModel.playerFitTopH = barHeight
            translatePlayerCoverToFit(true)
            musicBucketMsg.movementMethod = ScrollingMovementMethod.getInstance()

            root.onGlobalLayout {
                musicBucketContainer.botBlock =
                    resources.getDimensionPixelSize(R.dimen.model_icon_dimen).toFloat()
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
                is MusicViewEvent.OnBucketSelect -> {
                    if (currentBucket == it.musicBucket.name) {
                        binding.musicShowBucket.negative()
                        return@onViewEvent
                    }
                    currentBucket = it.musicBucket.name
                    binding.onMusicBucketSelected(it.musicBucket)
                    switchMusicBucket(it.musicBucket)
                    binding.musicShowBucket.negative()
                }
                is MusicViewEvent.OnBucketRefresh ->
                    onMusicBucketRefresh(it.musicBucket, it.state)
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

    private suspend fun initMusicBucketList() {
        binding.musicBucket.apply {
            layoutManager = LinearLayoutManager(this@MusicActivity)
            adapter = MusicBucketAdapter(this@MusicActivity).apply {
                setData(viewModel.getMusicBuckets())
                musicBucketEventListener = object : MusicBucketAdapter.MusicBucketEvent {
                    override fun onBucketClicked(musicBucket: MusicBucket) =
                        sendViewEvent(MusicViewEvent.OnBucketSelect(musicBucket))

                    override fun addMusic(bucket: String, position: Int) =
                        sendViewEvent(MusicViewEvent.AddMusic(bucket))

                    override fun delete(bucket: String, position: Int) =
                        sendViewEvent(MusicViewEvent.Delete(bucket))

                    override fun edit(bucket: String, position: Int) =
                        sendViewEvent(MusicViewEvent.Edit(bucket))

                    override fun onSelectedBucketRefresh(bucket: MusicBucket, state: Int) =
                        sendViewEvent(MusicViewEvent.OnBucketRefresh(bucket, state))
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

    private suspend fun switchMusicBucket(musicBucket: MusicBucket) {
        binding.musicMusicList.swapAdapter(
            MusicListAdapter(
                this@MusicActivity,
                viewModel.getCurrentMusicList(musicBucket)
            ).apply {
                getMusicListAdapter()?.getPlayingMusic()?.let { selectList.add(it) }
                clickCallback = getMusicListAdapter()?.clickCallback
            }, false
        )
    }

    private suspend fun MusicActivityBinding.onMusicBucketSelected(bucket: MusicBucket) {
        viewModel.getBucket(bucket.name)?.let { mb ->
            changeIcon(mb.icon)
            changeName(mb.name)
            changeMsg(mb.detail)
            musicBucketTime.text = mb.date
        }
    }

    private suspend fun onMusicBucketRefresh(bucket: MusicBucket, state: Int) {
        when (state) {
            MusicBucket.ALL -> {
                changeIcon(bucket.icon)
                changeName(bucket.name)
                changeMsg(bucket.detail)
            }
            MusicBucket.DETAIL or MusicBucket.COVER -> {
                changeIcon(bucket.icon)
                changeMsg(bucket.detail)
            }
            MusicBucket.DETAIL or MusicBucket.NAME -> {
                changeName(bucket.name)
                changeMsg(bucket.detail)
            }
            MusicBucket.NAME or MusicBucket.COVER -> {
                changeName(bucket.name)
                changeIcon(bucket.icon)
            }
            MusicBucket.COVER -> changeIcon(bucket.icon)
            MusicBucket.DETAIL -> changeMsg(bucket.detail)
            MusicBucket.NAME -> changeName(bucket.name)
        }
    }

    private fun changeName(name: String) {
        binding.musicBucketName.text = name
    }

    private fun changeMsg(msg: String?) {
        binding.musicBucketMsg.text = if (msg != null) "$msg" else R.string.none.getString()
    }

    private suspend fun changeIcon(icon: String?) {
        binding.apply {
            userConfig.lastMusicBucketCover = icon ?: ""
            icon?.getBitmap()?.let { bm ->
                musicBucketIcon.setImageBitmap(bm)
                blurredBucketCover.setBlurBitmap(bm, 24, 10)
            } ?: run {
                blurredBucketCover.setImageDrawable(mySmallMusicPlayer.baseCoverDrawable)
                musicBucketIcon.setImageDrawable(musicR.ic_music_note_24_white.getDrawable())
            }
        }
    }

    private fun getMusicBucketAdapter() = (binding.musicBucket.adapter as MusicBucketAdapter?)

    private fun getMusicListAdapter() = (binding.musicMusicList.adapter as MusicListAdapter?)

    private fun MusicActivityBinding.translatePlayerCoverToFit(fitTop: Boolean) {
        TransitionManager.beginDelayedTransition(musicBucketContainer)
        musicPlayerCover.updateLayoutParams {
            if (fitTop) height += viewModel.playerFitTopH
            else height -= viewModel.playerFitTopH
        }
    }

    override fun onActive() {
        binding.apply {
            musicBucketContainer.enableRender()
            var isDone = false
            musicBucketContainer.show(onStart = {
                binding.model?.isContainerOpen?.set(true)
                musicBucketContainer.setWillMove(true)
            }, update = {
                if ((it?.animatedValue as Float) > 0.8f) {
                    if (isDone) return@show
                    isDone = true

                    translatePlayerCoverToFit(true)
                }
            }, onEnd = {
                musicBucketContainer.setWillMove(false)

            })
        }
    }

    override fun onNegative() {
        binding.apply {
            if (musicBucketName.text == null || musicBucketName.text.isEmpty()) {
                launch { getMusicBucketAdapter()?.setSelect(viewModel.lastBucket) }
                return
            }
            musicBucketContainer.hide(onStart = {
                musicBucketContainer.setWillMove(true)
                translatePlayerCoverToFit(false)
                binding.model?.isContainerOpen?.set(false)
            }, onEnd = {
                musicBucketContainer.setWillMove(false)
                musicBucketContainer.disableRender()
            })
        }
    }

    override fun getSwapAnim(): Pair<Int, Int> {
        return Pair(R.anim.card_bot_in, R.anim.card_bot_out)
    }

}