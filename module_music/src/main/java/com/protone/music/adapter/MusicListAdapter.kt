package com.protone.music.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.core.view.isGone
import com.protone.common.baseType.toStringMinuteTime
import com.protone.common.context.newLayoutInflater
import com.protone.common.entity.Music
import com.protone.component.R
import com.protone.component.view.adapter.SelectListAdapter
import com.protone.music.databinding.MusicListLayoutBinding

class MusicListAdapter(context: Context, musicList: MutableList<Music>) :
    SelectListAdapter<MusicListLayoutBinding, Music, MusicListAdapter.MusicListEvent>(
        context,
        true
    ) {

    sealed class MusicListEvent {
        data class PlayPosition(val music: Music) : MusicListEvent()
        data class InsertMusics(val musics: Collection<Music>) : MusicListEvent()
    }

    init {
        mList.addAll(musicList)
    }

    var clickCallback: ((Music) -> Unit?)? = null

    private var playPosition = -1

    override val select: (MusicListLayoutBinding, Int, isSelect: Boolean) -> Unit =
        { binding, _, isSelect ->
            binding.apply {
                clickAnimation(
                    isSelect,
                    musicListContainer,
                    musicListInContainer,
                    musicListPlayState,
                    musicListName,
                    musicListTime,
                    musicListDetail,
                    dispatch = false,
                    backgroundColor = R.color.foreDark,
                    textsColor = R.color.white
                )
            }
        }

    override suspend fun handleEventAsynchronous(data: MusicListEvent) {
        when (data) {
            is MusicListEvent.PlayPosition -> {
                if (mList.size <= 0) return
                if (mList.contains(data.music)) {
                    clearAllSelected()
                    selectList.add(data.music)
                    playPosition = mList.indexOf(data.music)
                    notifyItemChangedCO(playPosition)
                }
            }
            is MusicListEvent.InsertMusics -> {
                if (data.musics.isEmpty()) return
                val oldSize = mList.size - 1
                mList.addAll(data.musics)
                notifyItemRangeInsertedCO(oldSize, mList.size - 1)
            }
        }
    }

    override fun itemIndex(path: Music): Int = mList.indexOf(path)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Holder<MusicListLayoutBinding> {
        return Holder(
            MusicListLayoutBinding.inflate(
                context.newLayoutInflater,
                parent,
                false
            )
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: Holder<MusicListLayoutBinding>, position: Int) {
        holder.binding.apply {
            mList[position].let { music ->
                setSelect(holder.binding, position, selectList.contains(music))
                musicListContainer.setOnClickListener {
                    if (selectList.contains(music)) return@setOnClickListener
                    if (playPosition == holder.layoutPosition) return@setOnClickListener
                    clearAllSelected()
                    itemClickChange(
                        R.color.blue_2,
                        R.color.white,
                        musicListContainer,
                        musicListInContainer,
                        arrayOf(
                            musicListName,
                            musicListTime,
                            musicListDetail
                        ),
                        true
                    )
                    clickCallback?.invoke(music)
                }
                musicListName.text = music.title
                if (music.artist != null && music.album != null) {
                    musicListDetail.text = "${music.artist} · ${music.album}"
                } else musicListDetail.isGone = true
                musicListTime.text = music.duration.toStringMinuteTime()
            }
        }
    }

    fun getPlayingPosition(): Int {
        if (mList.size <= 0) return -1
        return mList.indexOf(selectList.getOrNull(0) ?: mList[0])
    }

    fun getPlayingMusic(): Music? = selectList.getOrNull(0)

    fun getPlayList() = mList.toMutableList()

    fun playPosition(music: Music) {
        if (selectList.contains(music)) return
        emit(MusicListEvent.PlayPosition(music))
    }

    fun insertMusics(musics: Collection<Music>) {
        emit(MusicListEvent.InsertMusics(musics))
    }

}