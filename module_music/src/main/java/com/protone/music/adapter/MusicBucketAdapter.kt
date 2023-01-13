package com.protone.music.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import com.protone.common.baseType.getDrawable
import com.protone.common.baseType.withMainContext
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
        context, true
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

    override suspend fun handleEventAsynchronous(data: MusicBucketAEvent) {
        when (data) {
            is MusicBucketAEvent.AddBucket -> {
                withContext(Dispatchers.Main) {
                    mList.add(data.musicBucket)
                    notifyItemInsertedChecked(mList.size - 1)
                }
            }
            is MusicBucketAEvent.RefreshBucket -> {
                mList.indexOfFirst {
                    it.name == data.bucket.name
                }.takeIf {
                    it != -1 && it != 0
                }?.let {
                    val bucket = mList[it]
                    var payloads = MusicBucket.ALL xor MusicBucket.DETAIL
                    if (bucket.name == data.bucket.name) {
                        payloads = payloads xor MusicBucket.NAME
                    }
                    if (bucket.icon == data.bucket.icon) {
                        payloads = payloads xor MusicBucket.COVER
                    }
                    if (bucket.size == data.bucket.size) {
                        payloads = payloads xor MusicBucket.SIZE
                    }
                    mList[it] = data.bucket
                    notifyItemChangedChecked(it, payloads)
                }
            }
            is MusicBucketAEvent.DeleteBucket -> {
                val index = mList.find {
                    it.name == data.musicBucket.name
                }.let { mList.indexOf(it) }
                if (index < 0) return
                withMainContext {
                    mList.removeAt(index)
                    notifyItemRemovedChecked(index)
                }
                selectList.clear()
                selectList.add(mList[0])
                musicBucketEventListener?.onBucketClicked(mList[0])
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

    override fun onBindViewHolder(
        holder: Holder<MusicBucketAdapterLayoutBinding>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            holder.binding.apply {
                val bucket = mList[holder.layoutPosition]
                when (payloads.first()) {
                    MusicBucket.ALL -> {
                        changeIcon(bucket)
                        changeName(bucket.name)
                        changeSize(bucket.size)
                    }
                    MusicBucket.SIZE or MusicBucket.COVER -> {
                        changeIcon(bucket)
                        changeSize(bucket.size)
                    }
                    MusicBucket.SIZE or MusicBucket.NAME -> {
                        changeSize(bucket.size)
                        changeName(bucket.name)
                    }
                    MusicBucket.NAME or MusicBucket.COVER -> {
                        changeName(bucket.name)
                        changeIcon(bucket)
                    }
                    MusicBucket.COVER -> changeIcon(bucket)
                    MusicBucket.SIZE -> changeSize(bucket.size)
                    MusicBucket.NAME -> changeName(bucket.name)
                }
            }
        }
        super.onBindViewHolder(holder, position, payloads)
    }

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
        val bucket = mList[holder.layoutPosition]
        setSelect(holder.binding, position, selectList.contains(bucket))
        holder.binding.apply {
            changeIcon(bucket)
            changeName(bucket.name)
            changeSize(bucket.size)

            musicBucketBack.setOnClickListener {
                if (!selectList.contains(bucket)) checkSelect(position, bucket)
                musicBucketEventListener?.onBucketClicked(bucket)
            }

            if (bucket.name != ALL_MUSIC) musicBucketAction.setOnClickListener {
                if (musicBucketBack.isVisible) {
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
                    return@setOnClickListener
                }
                closeMusicBucketBack()
            }
            musicBucketEdit.setOnClickListener {
                musicBucketEventListener?.edit(bucket.name, position)
                closeMusicBucketBack()
            }
            musicBucketDelete.setOnClickListener {
                musicBucketEventListener?.delete(bucket.name, position)
                closeMusicBucketBack()
            }
            musicBucketAddList.setOnClickListener {
                closeMusicBucketBack()
                musicBucketEventListener?.addMusic(bucket.name, position)
            }
        }
    }

    override fun onFailedToRecycleView(holder: Holder<MusicBucketAdapterLayoutBinding>): Boolean {
        return true
    }

    private fun MusicBucketAdapterLayoutBinding.closeMusicBucketBack() {
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

    private fun MusicBucketAdapterLayoutBinding.changeIcon(musicBucket: MusicBucket) {
        musicBucketIcon.also {
            when {
                musicBucket.icon != null -> loadIcon(it, iconPath = musicBucket.icon)
                else -> loadIcon(
                    it,
                    drawable = R.drawable.ic_baseline_music_note_24.getDrawable()
                )
            }
        }
    }

    private fun MusicBucketAdapterLayoutBinding.changeName(name: String) {
        musicBucketName.text = name
    }

    private fun MusicBucketAdapterLayoutBinding.changeSize(size: Int) {
        musicBucketNum.text = size.toString()
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
        fun onBucketClicked(musicBucket: MusicBucket)
        fun addMusic(bucket: String, position: Int)
        fun delete(bucket: String, position: Int)
        fun edit(bucket: String, position: Int)
    }

}