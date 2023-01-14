package com.protone.music.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import com.protone.common.baseType.getString
import com.protone.common.baseType.toStringMinuteTime
import com.protone.common.context.newLayoutInflater
import com.protone.common.entity.Music
import com.protone.music.R
import com.protone.component.view.adapter.SelectListAdapter
import com.protone.music.databinding.MusicListLayoutBinding

class MusicListAdapter(context: Context, musicList: Collection<Music>) :
    SelectListAdapter<MusicListLayoutBinding, Music, MusicListAdapter.MusicListEvent>(
        context,
        true
    ) {

    sealed class MusicListEvent {
        data class PlayPosition(val music: Music) : MusicListEvent()
        data class ChangeData(val musics: Collection<Music>) : MusicListEvent()
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
                    backgroundColor = com.protone.component.R.color.foreDark,
                    textsColor = com.protone.component.R.color.white
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
            is MusicListEvent.ChangeData -> {
                if (data.musics.isEmpty()) return
                val oldSize = mList.size
                mList.clear()
                notifyItemRangeRemoved(0, oldSize)
                mList.addAll(data.musics)
                notifyItemRangeInsertedChecked(0, mList.size)
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
                        com.protone.component.R.color.blue_2,
                        com.protone.component.R.color.white,
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
                musicListDetail.text =
                    " ${music.artist ?: R.string.unknown_artist.getString()} · ${music.album ?: R.string.unknown_album.getString()}"
                musicListTime.text = music.duration.toStringMinuteTime()
            }
        }
    }

    fun getPlayingPosition(): Int {
        if (mList.size <= 0) return -1
        return mList.indexOf(selectList.first ?: mList[0])
    }

    fun getPlayingMusic(): Music? = try {
        selectList.first
    } catch (e: NoSuchElementException) {
        null
    }

    fun getPlayList() = mList.toMutableList()

    fun playPosition(music: Music) {
        if (selectList.contains(music)) return
        emit(MusicListEvent.PlayPosition(music))
    }

    fun changeData(musics: Collection<Music>) {
        emit(MusicListEvent.ChangeData(musics))
    }

    fun addMusic(music: Music) {
        mList.add(0, music)
        notifyItemInserted(0)
    }

    fun addMusics(musics: Collection<Music>) {
        if (musics.isEmpty()) return
        mList.addAll(0, musics)
        notifyItemRangeInserted(0, musics.size)
    }

    fun removeMusic(music: Music) {
        mList.indexOf(music).takeIf {
            it != -1
        }?.let {
            mList.removeAt(it)
            notifyItemRemoved(it)
        }
    }

}