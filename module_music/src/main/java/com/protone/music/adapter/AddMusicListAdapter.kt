package com.protone.music.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Animatable
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isGone
import com.protone.common.baseType.getDrawable
import com.protone.common.baseType.launchDefault
import com.protone.common.baseType.toStringMinuteTime
import com.protone.common.baseType.withMainContext
import com.protone.common.context.newLayoutInflater
import com.protone.common.entity.Music
import com.protone.common.utils.displayUtils.AnimationHelper
import com.protone.component.R
import com.protone.component.view.adapter.SelectListAdapter
import com.protone.music.databinding.MusicListLayoutBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class AddMusicListAdapter(
    context: Context,
    val bucket: String,
    val mode: String,
    val proxy: AddMusicListAdapterDataProxy
) : SelectListAdapter<MusicListLayoutBinding, Music, Any>(context) {

    init {
        multiChoose = mode == "ADD"
        if (multiChoose) launch(Dispatchers.Default) {
            selectList.addAll(proxy.getMusicWithMusicBucket(bucket))
            selectList.forEach {
                mList.indexOf(it).let { index ->
                    if (index != -1) notifyItemChangedCO(index)
                }
            }
        }
    }

    private val viewQueue = PriorityQueue<Int>()

    private var onBusy = false

    override fun setSelect(content: MusicListLayoutBinding, position: Int, isSelect: Boolean) {
        content.apply {
            clickAnimation(
                isSelect,
                musicListContainer,
                musicListInContainer,
                musicListPlayState,
                musicListName,
                musicListTime,
                musicListDetail,
                backgroundColor = R.color.transparent_black1,
                backgroundColorPressed = R.color.transparent_black,
                textsColor = R.color.white
            )
            if (isSelect) {
                musicListPlayState.setImageDrawable(R.drawable.load_animation.getDrawable())
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
        ).apply {
            binding.isLoad = true
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: Holder<MusicListLayoutBinding>, position: Int) {
        holder.binding.apply {
            mList[position].also { music ->
                setSelect(holder.binding, position, music in selectList)
                musicListContainer.setOnClickListener {
                    if (onBusy) return@setOnClickListener
                    onBusy = true
                    launchDefault {
                        if (mode == "SEARCH") {
                            proxy.play(music)
                            withMainContext { checkSelect(position, music) }
                            onBusy = false
                            return@launchDefault
                        }
                        viewQueue.add(position)
                        if (selectList.contains(music)) {
                            if (!multiChoose) return@launchDefault
                            proxy.deleteMusicWithMusicBucket(music.musicBaseId, bucket)
                            withMainContext { checkSelect(position, music) }
                            onBusy = false
                            return@launchDefault
                        }
                        withMainContext { checkSelect(position, music) }
                        musicListPlayState.drawable.let { d ->
                            when (d) {
                                is Animatable -> {
                                    withMainContext { d.start() }
                                    if (multiChoose) {
                                        proxy.insertMusicWithMusicBucket(music.musicBaseId, bucket)
                                            .takeIf { re -> re != -1L }
                                            ?.run { changeIconAni(musicListPlayState) }
                                            ?: run {
                                                selectList.remove(music)
                                                notifyItemChanged()
                                            }
                                    } else {
                                        changeIconAni(musicListPlayState)
                                    }
                                    d.stop()
                                }
                                else -> {
                                    selectList.remove(music)
                                    notifyItemChanged()
                                }
                            }
                        }
                        onBusy = false
                    }
                }

                musicListName.text = music.title
                if (music.artist != null && music.album != null) {
                    musicListDetail.text = "${music.artist} · ${music.album}"
                } else musicListDetail.isGone = true
                musicListTime.text = music.duration.toStringMinuteTime()
            }
        }
    }

    private suspend fun notifyItemChanged() {
        while (!viewQueue.isEmpty()) {
            val poll = viewQueue.poll()
            if (poll != null) {
                notifyItemChangedCO(poll)
            }
        }
    }

    private suspend fun changeIconAni(view: ImageView) {
        withMainContext {
            AnimationHelper.apply {
                animatorSet(scaleX(view, 0f), scaleY(view, 0f), doOnEnd = {
                    view.setImageDrawable(R.drawable.ic_baseline_check_24_white.getDrawable())
                    animatorSet(scaleX(view, 1f), scaleY(view, 1f), play = true)
                }, play = true)
            }
        }
    }

    interface AddMusicListAdapterDataProxy {
        suspend fun getMusicWithMusicBucket(bucket: String): Collection<Music>
        suspend fun insertMusicWithMusicBucket(musicBaseId: Long, bucket: String): Long
        fun deleteMusicWithMusicBucket(musicBaseId: Long, musicBucket: String)
        fun play(music: Music)
    }
}