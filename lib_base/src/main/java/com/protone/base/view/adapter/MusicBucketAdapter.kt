package com.protone.base.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.protone.base.R
import com.protone.base.databinding.MusicBucketAdapterLayoutBinding
import com.protone.common.baseType.getDrawable
import com.protone.common.baseType.getString
import com.protone.common.context.newLayoutInflater
import com.protone.common.entity.MusicBucket
import com.protone.common.utils.displayUtils.AnimationHelper
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

    var musicBuckets: MutableList<MusicBucket> = mutableListOf()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field.clear()
            field.addAll(value)
            notifyDataSetChanged()
        }

    var musicBucketEventListener: MusicBucketEvent? = null

    var clickCallback: ((MusicBucket) -> Unit)? = null

    override suspend fun onEventIO(data: MusicBucketAEvent) {
        when (data) {
            is MusicBucketAEvent.AddBucket -> {
                withContext(Dispatchers.Main) {
                    musicBuckets.add(data.musicBucket)
                    notifyItemInserted(musicBuckets.size - 1)
                }
            }
            is MusicBucketAEvent.RefreshBucket -> {
                val index = musicBuckets.find {
                    it.name == data.bucket.name
                }.let { musicBuckets.indexOf(it) }
                if (index != -1 && index != 0) {
                    musicBuckets[index].apply {
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
                val index = musicBuckets.find {
                    it.name == data.musicBucket.name
                }.let { musicBuckets.indexOf(it) }
                if (index < 0) return
                withContext(Dispatchers.Main) {
                    musicBuckets.removeAt(index)
                    notifyItemRemoved(index)
                }
                selectList.clear()
                selectList.add(musicBuckets[0])
                clickCallback?.invoke(musicBuckets[0])
            }
        }
    }

    override val select: (
        holder: Holder<MusicBucketAdapterLayoutBinding>,
        isSelect: Boolean
    ) -> Unit =
        { holder, isSelect ->
            holder.binding.musicBucketBack.setBackgroundColor(
                context.resources.getColor(
                    if (isSelect) R.color.gray_1 else R.color.white,
                    context.theme
                )
            )
        }

    override fun itemIndex(path: MusicBucket): Int = musicBuckets.indexOf(path)

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
        setSelect(holder, selectList.contains(musicBuckets[position]))
        holder.binding.apply {
            musicBucketName.text = musicBuckets[position].name
            musicBucketIcon.apply {
                when {
                    musicBuckets[position].icon != null -> {
                        loadIcon(this, iconPath = musicBuckets[position].icon)
                    }
                    else -> {
                        loadIcon(
                            this,
                            drawable = R.drawable.ic_music_note.getDrawable()
                        )
                    }
                }
            }
            musicBucketNum.text = musicBuckets[position].size.toString()

            musicBucketBack.setOnClickListener {
                if (!selectList.contains(musicBuckets[position]))
                    checkSelect(holder, musicBuckets[position])
                clickCallback?.invoke(musicBuckets[holder.layoutPosition])
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

            if (musicBuckets[holder.layoutPosition].name != R.string.all_music.getString()) musicBucketAction.setOnClickListener {
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
                musicBucketEventListener?.edit(musicBuckets[holder.layoutPosition].name, position)
                closeMusicBucketBack()
            }
            musicBucketDelete.setOnClickListener {
                musicBucketEventListener?.delete(musicBuckets[holder.layoutPosition].name, position)
                closeMusicBucketBack()
            }
            musicBucketAddList.setOnClickListener {
                closeMusicBucketBack()
                musicBucketEventListener?.addMusic(
                    musicBuckets[holder.layoutPosition].name,
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
        Glide.with(context).asDrawable().apply {
            (if (iconPath != null) load(iconPath) else load(drawable))
                .transition(DrawableTransitionOptions.withCrossFade())
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.drawable.ic_music_note)
                .override(imageView.measuredWidth, imageView.measuredHeight)
                .into(imageView)
        }
    }

    override fun getItemCount(): Int = musicBuckets.size

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