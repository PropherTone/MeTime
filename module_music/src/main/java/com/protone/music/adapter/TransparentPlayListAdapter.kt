package com.protone.music.adapter

import android.content.Context
import android.view.ViewGroup
import com.protone.common.context.newLayoutInflater
import com.protone.common.entity.Music
import com.protone.component.R
import com.protone.component.view.adapter.SelectListAdapter
import com.protone.music.databinding.TpPlaylistAdapterLayoutBinding

class TransparentPlayListAdapter(
    context: Context,
    onPlay: Music?,
    playList: List<Music>
) : SelectListAdapter<TpPlaylistAdapterLayoutBinding, Music, Any>(context) {
    override fun setSelect(
        content: TpPlaylistAdapterLayoutBinding,
        position: Int,
        isSelect: Boolean
    ) {
        if (isSelect) {
            content.playListName.setBackgroundResource(R.drawable.round_background_tans_white_lite)
        } else {
            content.playListName.setBackgroundResource(R.drawable.round_background_fore_dark)
        }
    }

    init {
        mList.addAll(playList)
        onPlay?.let { selectList.add(it) }
    }

    override fun itemIndex(path: Music): Int = mList.indexOf(path)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Holder<TpPlaylistAdapterLayoutBinding> {
        val binding =
            TpPlaylistAdapterLayoutBinding.inflate(context.newLayoutInflater, parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder<TpPlaylistAdapterLayoutBinding>, position: Int) {
        mList[position].let { music ->
            setSelect(holder.binding, position, selectList.contains(music))
            holder.binding.playListName.text = music.title
            holder.binding.playListName.setOnClickListener {
                if (selectList.contains(music)) return@setOnClickListener
                checkSelect(position, music)
                onPlayListClkListener?.onClk(music)
            }
        }
    }

    fun setOnPlay(music: Music) {
        val oldIndex = mList.indexOf(selectList.first())
        selectList.clear()
        notifyItemChanged(oldIndex)
        val newIndex = mList.indexOf(music)
        if (newIndex != -1) {
            selectList.add(music)
            notifyItemChanged(newIndex)
        }
    }

    var onPlayListClkListener: OnPlayListClk? = null

    interface OnPlayListClk {
        fun onClk(music: Music)
    }
}