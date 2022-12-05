package com.protone.music.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import com.protone.common.baseType.getDrawable
import com.protone.common.context.newLayoutInflater
import com.protone.common.entity.MusicBucket
import com.protone.common.utils.ALL_MUSIC
import com.protone.common.utils.displayUtils.AnimationHelper
import com.protone.common.utils.displayUtils.imageLoader.Image
import com.protone.common.utils.displayUtils.imageLoader.constant.DiskCacheStrategy
import com.protone.component.R
import com.protone.component.view.adapter.SelectListAdapter
import com.protone.music.databinding.MusicBucketAdapterLayoutBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class MusicBucketAdapter(context: Context, musicBucket: MusicBucket) :
    SelectListAdapter<MusicBucketAdapterLayoutBinding, MusicBucket, MusicBucketAdapter.MusicBucketAEvent>(
        context,
        true
    ) {

    sealed class MusicBucketAEvent {
        data class AddBucket(val musicBucket: MusicBucket) : MusicBucketAEvent()
        data class RefreshBucket(val bucket: MusicBucket) : MusicBucketAEvent()
        data class DeleteBucket(val musicBucket: MusicBucket) : MusicBucketAEvent()
    }

    init {
        selectList.add(musicBucket)
    }

    var musicBucketEventListener: MusicBucketEvent? = null

    var clickCallback: ((MusicBucket) -> Unit)? = null

    override suspend fun onEventIO(data: MusicBucketAEvent) {
        when (data) {
            is MusicBucketAEvent.AddBucket -> {
                withContext(Dispatchers.Main) {
                    mList.add(data.musicBucket)
                    notifyItemInserted(mList.size - 1)
                }
            }
            is MusicBucketAEvent.RefreshBucket -> {
                val index = mList.find {
                    it.name == data.bucket.name
                }.let { mList.indexOf(it) }
                if (index != -1 && index != 0) {
                    mList[index].apply {
                        name = data.bucket.name
                        icon = data.bucket.icon
                        size = data.bucket.size
                        detail = data.bucket.detail
                        date = data.bucket.date
                    }
                    notifyItemChangedCO(index)
                }
            }
            is MusicBucketAEvent.DeleteBucket -> {
                val index = mList.find {
                    it.name == data.musicBucket.name
                }.let { mList.indexOf(it) }
                if (index < 0) return
                withContext(Dispatchers.Main) {
                    mList.removeAt(index)
                    notifyItemRemoved(index)
                }
                selectList.clear()
                selectList.add(mList[0])
                clickCallback?.invoke(mList[0])
            }
        }
    }

    override val select: (
        MusicBucketAdapterLayoutBinding,
        Int,
        isSelect: Boolean
    ) -> Unit =
        { binding, _, isSelect ->
            binding.musicBucketBack.setBackgroundColor(
                context.resources.getColor(
                    if (isSelect) R.color.gray_1 else R.color.white,
                    context.theme
                )
            )
        }

    override fun itemIndex(path: MusicBucket): Int = mList.indexOf(path)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Holder<MusicBucketAdapterLayoutBinding> {
        return Holder(
            MusicBucketAdapterLayoutBinding.inflate(
                context.newLayoutInflater,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Holder<MusicBucketAdapterLayoutBinding>, position: Int) {
        setSelect(holder.binding, position, selectList.contains(mList[position]))
        holder.binding.apply {
            musicBucketName.text = mList[position].name
            musicBucketIcon.apply {
                when {
                    mList[position].icon != null -> {
                        loadIcon(this, iconPath = mList[position].icon)
                    }
                    else -> {
                        loadIcon(
                            this,
                            drawable = R.drawable.ic_baseline_music_note_24.getDrawable()
                        )
                    }
                }
            }
            musicBucketNum.text = mList[position].size.toString()

            musicBucketBack.setOnClickListener {
                if (!selectList.contains(mList[position]))
                    checkSelect(position, mList[position])
                clickCallback?.invoke(mList[holder.layoutPosition])
            }

            fun closeMusicBucketBack() {
                AnimationHelper.translationX(
                    musicBucketBack,
                    -musicBucketBack.measuredWidth.toFloat(),
                    0f,
                    200,
                    play = true,
                    doOnStart = {
                        musicBucketBack.isVisible = true
                    }
                )
            }

            if (mList[holder.layoutPosition].name != ALL_MUSIC) musicBucketAction.setOnClickListener {
                when (musicBucketBack.isVisible) {
                    true -> {
                        AnimationHelper.translationX(
                            musicBucketBack,
                            0f,
                            -musicBucketBack.measuredWidth.toFloat(),
                            200,
                            play = true,
                            doOnEnd = {
                                musicBucketBack.isVisible = false
                            }
                        )
                    }
                    false -> {
                        closeMusicBucketBack()
                    }
                }
            }
            musicBucketEdit.setOnClickListener {
                musicBucketEventListener?.edit(mList[holder.layoutPosition].name, position)
                closeMusicBucketBack()
            }
            musicBucketDelete.setOnClickListener {
                musicBucketEventListener?.delete(mList[holder.layoutPosition].name, position)
                closeMusicBucketBack()
            }
            musicBucketAddList.setOnClickListener {
                closeMusicBucketBack()
                musicBucketEventListener?.addMusic(
                    mList[holder.layoutPosition].name,
                    position
                )
            }
        }
    }

    private fun loadIcon(
        imageView: ImageView,
        iconPath: String? = null,
        drawable: Drawable? = null
    ) {
        Image.run { if (iconPath != null) load(iconPath) else load(drawable) }
            .with(context)
            .skipMemoryCache()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .placeholder(R.drawable.ic_baseline_music_note_24)
            .overwrite(imageView.measuredWidth, imageView.measuredHeight)
            .into(imageView)
    }

    fun deleteBucket(musicBucket: MusicBucket): Boolean {
        emit(MusicBucketAEvent.DeleteBucket(musicBucket))
        return true
    }

    fun addBucket(musicBucket: MusicBucket) {
        emit(MusicBucketAEvent.AddBucket(musicBucket))
    }

    fun refreshBucket(bucket: MusicBucket) {
        emit(MusicBucketAEvent.RefreshBucket(bucket))
    }

    interface MusicBucketEvent {
        fun addMusic(bucket: String, position: Int)
        fun delete(bucket: String, position: Int)
        fun edit(bucket: String, position: Int)
    }
}