package com.protone.ui.view.adapter

import android.content.Context
import android.view.ViewGroup
import com.protone.ui.R
import com.protone.ui.databinding.TpPlaylistAdapterLayoutBinding
import com.protone.common.context.newLayoutInflater
import com.protone.common.entity.Music

class TransparentPlayListAdapter(
    context: Context,
    onPlay: Music?,
    private val playList: MutableList<Music>
) : SelectListAdapter<TpPlaylistAdapterLayoutBinding, Music, Any>(context) {
    override val select: (holder: Holder<TpPlaylistAdapterLayoutBinding>, isSelect: Boolean) -> Unit =
        { holder, isSelect ->
            if (isSelect) {
                holder.binding.playListName.setBackgroundResource(R.drawable.round_background_tans_white_lite)
            } else {
                holder.binding.playListName.setBackgroundResource(R.drawable.round_background_fore_dark)
            }
        }

    init {
        onPlay?.let { selectList.add(it) }
    }

    override fun itemIndex(path: Music): Int = playList.indexOf(path)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Holder<TpPlaylistAdapterLayoutBinding> {
        val binding =
            TpPlaylistAdapterLayoutBinding.inflate(context.newLayoutInflater, parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder<TpPlaylistAdapterLayoutBinding>, position: Int) {
        playList[position].let { music ->
            setSelect(holder, selectList.contains(music))
            holder.binding.playListName.text = music.title
            holder.binding.playListName.setOnClickListener {
                if (selectList.contains(music)) return@setOnClickListener
                checkSelect(holder, music)
                onPlayListClkListener?.onClk(music)
            }
        }
    }

    override fun getItemCount(): Int = playList.size

    fun setOnPlay(music: Music) {
        val oldIndex = playList.indexOf(selectList.first())
        selectList.clear()
        notifyItemChanged(oldIndex)
        val newIndex = playList.indexOf(music)
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