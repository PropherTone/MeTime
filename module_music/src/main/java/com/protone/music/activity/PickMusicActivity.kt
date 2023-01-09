package com.protone.music.activity

import android.content.Intent
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.protone.common.baseType.getString
import com.protone.common.baseType.toast
import com.protone.common.context.MUSIC_PLAY_CUR
import com.protone.common.context.linkInput
import com.protone.common.context.root
import com.protone.common.entity.Music
import com.protone.common.utils.ALL_MUSIC
import com.protone.common.utils.RouterPath
import com.protone.common.utils.SearchModel
import com.protone.component.BaseMusicActivity
import com.protone.component.BaseViewModel
import com.protone.component.broadcast.musicBroadCastManager
import com.protone.component.service.MusicBinder
import com.protone.component.view.customView.blurView.DefaultBlurController
import com.protone.component.view.customView.blurView.DefaultBlurEngine
import com.protone.music.adapter.AddMusicListAdapter
import com.protone.music.databinding.PickMusicActivityBinding
import com.protone.music.viewModel.PickMusicViewModel
import com.protone.music.viewModel.PickMusicViewModel.Companion.ADD_BUCKET
import com.protone.music.viewModel.PickMusicViewModel.Companion.SEARCH_MUSIC
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Route(path = RouterPath.MusicRouterPath.Pick)
class PickMusicActivity :
    BaseMusicActivity<PickMusicActivityBinding, PickMusicViewModel, BaseViewModel.ViewEvent>(true) {
    override val viewModel: PickMusicViewModel by viewModels()

    override fun createView(): PickMusicActivityBinding {
        return PickMusicActivityBinding.inflate(layoutInflater, root, false).apply {
            activity = this@PickMusicActivity
            searchContainer.fitStatuesBarUsePadding()
            linkInput(addMBList, addMBSearch)
            actionBtnContainer.initBlurTool(
                DefaultBlurController(
                    root as ViewGroup,
                    DefaultBlurEngine()
                )
            )
                .apply {
                    setBlurRadius(24f)
                }
            root.viewTreeObserver.addOnPreDrawListener {
                actionBtnContainer.renderFrame()
                true
            }
        }
    }

    override suspend fun PickMusicViewModel.init() {
        val mode = intent?.extras?.getString(PickMusicViewModel.MODE)

        val bucket = when (mode) {
            PickMusicViewModel.PICK_MUSIC -> ALL_MUSIC
            else -> intent?.extras?.getString(PickMusicViewModel.BUCKET_NAME)
        }

        if (bucket != null) {
            if (!mode.isNullOrEmpty() && mode == SEARCH_MUSIC) {
                bindMusicService { initSeen(bucket, mode, it) }
            } else {
                initSeen(bucket, mode ?: ADD_BUCKET)
            }
        } else {
            com.protone.common.R.string.no_data.getString().toast()
            finish()
        }

        val searchModel = SearchModel(binding.addMBSearch) {
            query(getInput())
        }

        onFinish = {
            searchModel.destroy()
        }
    }

    fun confirm() {
        val selectList = getSelectList()
        if (selectList != null && selectList.isNotEmpty()) {
            setResult(RESULT_OK, Intent().apply {
                data = selectList.first.uri
            })
        } else com.protone.common.R.string.cancel.getString().toast()
        finish()
    }

    private fun PickMusicViewModel.query(input: String) {
        launch(Dispatchers.Default) {
            refreshList(filterData(input))
        }
    }

    private suspend fun refreshList(list: MutableList<Music>) = withContext(Dispatchers.Main) {
        binding.addMBList.adapter.let {
            if (it is AddMusicListAdapter) {
                binding.addMBList.swapAdapter(
                    newAdapter(
                        it.bucket,
                        it.mode,
                        it.adapterDataBaseProxy,
                        list
                    ), true
                )
            }
        }
    }

    private suspend fun initSeen(
        bucket: String,
        mode: String,
        binder: MusicBinder? = null
    ) {
        binding.addMBConfirm.also {
            it.isGone = mode == ADD_BUCKET || mode == SEARCH_MUSIC
            binding.addMBLeave.isGone = !it.isGone
        }
        binding.addMBList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = newAdapter(
                bucket,
                mode,
                object : AddMusicListAdapter.AddMusicListAdapterDataProxy {
                    override suspend fun getMusicWithMusicBucket(bucket: String): Collection<Music> =
                        viewModel.getMusicWithMusicBucket(bucket)

                    override fun deleteMusicWithMusicBucket(
                        musicBaseId: Long,
                        musicBucket: String
                    ) = viewModel.deleteMusicWithMusicBucket(musicBaseId, musicBucket)

                    override fun play(music: Music) {
                        binder?.play(music)
                        musicBroadCastManager.sendBroadcast(Intent(MUSIC_PLAY_CUR))
                    }

                    override suspend fun insertMusicWithMusicBucket(
                        musicBaseId: Long,
                        bucket: String
                    ): Long = viewModel.insertMusicWithMusicBucket(musicBaseId, bucket)
                },
                viewModel.getMusics()
            )
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    super.getItemOffsets(outRect, view, parent, state)
                    if (parent.getChildLayoutPosition(view) >= state.itemCount - 1) {
                        outRect.bottom =
                            resources.getDimensionPixelSize(com.protone.common.R.dimen.action_icon_p)
                    }
                }
            })
        }
        getList()?.let { viewModel.data.addAll(it) }
    }

    private fun newAdapter(
        bucket: String,
        mode: String,
        proxy: AddMusicListAdapter.AddMusicListAdapterDataProxy,
        musics: MutableList<Music>
    ) = AddMusicListAdapter(
        this,
        bucket,
        mode,
        proxy
    ).apply { setData(musics) }

    private fun getList(): MutableList<Music>? = binding.addMBList.adapter.let {
        if (it is AddMusicListAdapter) {
            return it.mList
        } else null
    }

    private fun getSelectList() = binding.addMBList.adapter.let {
        if (it is AddMusicListAdapter) {
            it.selectList
        } else null
    }
}