package com.protone.music.activity

import android.text.method.ScrollingMovementMethod
import androidx.activity.viewModels
import androidx.core.view.updateLayoutParams
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
import com.protone.component.activity.BaseMusicActivity
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

    internal class BindingModel(
        val activity: MusicActivity,
        val binding: MusicActivityBinding,
        val viewModel: MusicModel
    ) {

        fun addBucket() {
            viewModel.addMusicBucket()
        }

        fun doAdd() {
            activity.doAddMusicToBucket(binding.musicBucketName.text.toString())
        }

        fun doEdit() {
            activity.doEditBucket(binding.musicBucketName.text.toString())
        }

        fun doDelete() {
            viewModel.tryDeleteMusicBucket(binding.musicBucketName.text.toString())
        }

        fun search() {
            activity.startActivity(
                PickMusicActivity::class.intent
                    .putExtra(PickMusicViewModel.BUCKET_NAME, viewModel.lastBucket)
                    .putExtra(PickMusicViewModel.MODE, PickMusicViewModel.SEARCH_MUSIC)
            )
        }

        fun locate() {
            activity.getMusicListAdapter()?.getPlayingPosition()?.let { position ->
                if (position != -1) binding.musicMusicList.smoothScrollToPosition(position)
            }
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

            model = BindingModel(this@MusicActivity, this, viewModel)

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

        initList()
        onLifecycleEvent {
            onResume { if (!isInit) isInit = true }
            onPause { isInit = false }
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

    fun doAddMusicToBucket(bucket: String) {
        if (bucket == ALL_MUSIC) {
            R.string.bruh.getString().toast()
            return
        }
        startActivity(
            PickMusicActivity::class.intent
                .putExtra(PickMusicViewModel.BUCKET_NAME, bucket)
                .putExtra(PickMusicViewModel.MODE, PickMusicViewModel.ADD_BUCKET)
        )
    }

    fun doEditBucket(bucket: String) {
        if (bucket == ALL_MUSIC) {
            R.string.bruh.getString().toast()
            return
        }
        startActivity(
            AddBucketActivity::class.intent.putExtra(
                AddBucketViewModel.BUCKET_NAME,
                bucket
            )
        )
    }

    private fun MusicModel.observeEvent(controller: MusicControllerIMP) {
        onViewEvent(this@MusicActivity, this@MusicActivity) {
            when (it) {
                is MusicViewEvent.InitList -> binding.initLists(it.list)
                is MusicViewEvent.PlayMusic -> binding.playMusic(controller, it.music)
                is MusicViewEvent.DoAddMusic -> doAddMusicToBucket(it.musicBucket.name)
                is MusicViewEvent.DoEdit -> doEditBucket(it.musicBucket.name)
                is MusicViewEvent.AddMusicBucket -> doAddMusicBucket()
                is MusicViewEvent.OnBucketSelect -> binding.selectBucket(it.musicBucket, it.list)
                is MusicViewEvent.OnBucketRefresh -> onMusicBucketRefresh(it.musicBucket, it.state)
                is MusicViewEvent.OnSelectedBucketRemoved -> {
                    if (isContainerOpen.get() == false) {
                        getMusicBucketAdapter()?.setSelect(viewModel.lastBucket)
                        return@onViewEvent
                    }
                    binding.musicBucketName.text = ""
                }
            }
        }
    }

    private fun MusicModel.observeMusicEvent() {
        observeMusicEvent observeFun@{
            when (it) {
                is MusicEvent.OnMusicBucketDataChanged -> {
                    getMusicListAdapter()?.notifyListChangedCO(it.musics)
                }
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
                    getMusicListAdapter()?.addMusics(it.musics)
                }
                is MusicEvent.OnMusicDeleted -> {
                    getMusicListAdapter()?.removeMusic(it.music)
                }
                is MusicEvent.OnMusicUpdated -> {
                    TODO("Update on musics will not happen")
                }
                else -> Unit
            }
        }
    }

    private suspend fun MusicActivityBinding.playMusic(
        controller: MusicControllerIMP,
        music: Music
    ) = withDefaultContext {
        if (viewModel.lastBucket != musicBucketName.text) {
            getMusicListAdapter()?.getPlayList()?.let { list ->
                viewModel.lastBucket = musicBucketName.text.toString()
                controller.setMusicList(list)
            }
        }
        controller.play(music)
    }

    private suspend fun doAddMusicBucket() {
        val re = startActivityForResult(AddBucketActivity::class.intent)
        when (re?.resultCode) {
            RESULT_CANCELED -> R.string.cancel.getString().toast()
        }
    }

    private suspend fun MusicActivityBinding.selectBucket(
        musicBucket: MusicBucket,
        list: List<Music>
    ) {
        if (viewModel.currentBucket == musicBucket.name) {
            musicShowBucket.negative()
            return
        }
        viewModel.currentBucket = musicBucket.name

        changeIcon(musicBucket.icon)
        changeName(musicBucket.name)
        changeMsg(musicBucket.detail)
        musicBucketTime.text = musicBucket.date

        musicMusicList.swapAdapter(
            MusicListAdapter(this@MusicActivity, list).apply {
                getMusicListAdapter()?.getPlayingMusic()?.let { selectList.add(it) }
                clickCallback = getMusicListAdapter()?.clickCallback
            }, false
        )
        musicShowBucket.negative()
    }

    private fun MusicActivityBinding.initLists(list: List<MusicBucket>) {
        musicBucket.apply {
            layoutManager = LinearLayoutManager(this@MusicActivity)
            adapter = MusicBucketAdapter(this@MusicActivity).apply {
                setData(list)
                musicBucketEventListener = viewModel.getBucketEventListener()
            }
        }
        musicMusicList.apply {
            layoutManager = LinearLayoutManager(this@MusicActivity)
            adapter = MusicListAdapter(this@MusicActivity, mutableListOf()).apply {
                clickCallback = { viewModel.sendViewEvent(MusicViewEvent.PlayMusic(it)) }
            }
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
        if (viewModel.isContainerOpen.get() == true) return
        binding.apply {
            musicBucketContainer.enableRender()
            var isDone = false
            musicBucketContainer.show(onStart = {
                viewModel.isContainerOpen.set(true)
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
        if (viewModel.isContainerOpen.get() == false) return
        binding.apply {
            if (musicBucketName.text == null || musicBucketName.text.isEmpty()) {
                launch { getMusicBucketAdapter()?.setSelect(viewModel.lastBucket) }
                return
            }
            musicBucketContainer.hide(onStart = {
                musicBucketContainer.setWillMove(true)
                translatePlayerCoverToFit(false)
                viewModel.isContainerOpen.set(false)
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