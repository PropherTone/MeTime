package com.protone.music.adapter

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.protone.common.baseType.getColor
import com.protone.common.baseType.getDrawable
import com.protone.common.context.newLayoutInflater
import com.protone.common.entity.MusicBucket
import com.protone.common.utils.ALL_MUSIC
import com.protone.common.utils.displayUtils.imageLoader.Image
import com.protone.common.utils.displayUtils.imageLoader.constant.DiskCacheStrategy
import com.protone.component.view.adapter.SelectListAdapter
import com.protone.music.R
import com.protone.music.databinding.MusicBucketAdapterLayoutBinding

class MusicBucketAdapter(context: Context) :
    SelectListAdapter<MusicBucketAdapterLayoutBinding, MusicBucket, MusicBucketAdapter.MusicBucketAEvent>(
        context, true
    ) {

    sealed class MusicBucketAEvent {
        data class AddBucket(val musicBucket: MusicBucket) : MusicBucketAEvent()
        data class RefreshBucket(val bucket: MusicBucket) : MusicBucketAEvent()
        data class DeleteBucket(val musicBucket: MusicBucket) : MusicBucketAEvent()
        data class SelectBucket(val bucketName: String) : MusicBucketAEvent()
    }

    var musicBucketEventListener: MusicBucketEvent? = null

    override suspend fun handleEventAsynchronous(data: MusicBucketAEvent) {
        when (data) {
            is MusicBucketAEvent.AddBucket -> {
                mList.add(data.musicBucket)
                notifyItemInsertedChecked(mList.size - 1)
            }
            is MusicBucketAEvent.RefreshBucket -> {
                mList.indexOfFirst { it.musicBucketId == data.bucket.musicBucketId }
                    .takeIf { it != -1 }
                    ?.let {
                        val bucket = mList[it]
                        mList[it] = data.bucket
                        if (selectList.contains(bucket)) {
                            selectList.clear()
                            selectList.add(data.bucket)
                        }
                        notifyItemChangedChecked(
                            it,
                            bucket.getChangeState(data.bucket, MusicBucket.DETAIL)
                        )
                        musicBucketEventListener?.onSelectedBucketRefresh(
                            data.bucket,
                            bucket.getChangeState(data.bucket)
                        )
                    }
            }
            is MusicBucketAEvent.DeleteBucket -> {
                val index = mList.find {
                    it.name == data.musicBucket.name
                }.let { mList.indexOf(it) }
                if (index < 0) return
                val isSelected = selectList.contains(mList[index])
                mList.removeAt(index)
                notifyItemRemovedChecked(index)
                if (isSelected) {
                    selectList.clear()
                    selectList.add(mList[0])
                    checkSelect(0, mList[0])
                    musicBucketEventListener?.onBucketClicked(mList[0])
                }
            }
            is MusicBucketAEvent.SelectBucket -> {
                mList.find { it.name == data.bucketName }?.let {
                    musicBucketEventListener?.onBucketClicked(it)
                }
            }
        }
    }

    override fun setSelect(
        content: MusicBucketAdapterLayoutBinding,
        position: Int,
        isSelect: Boolean
    ) {
        content.back.setBackgroundColor(
            (if (isSelect) R.color.bucket_selected else R.color.bucket_normal).getColor()
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
            changeTime(bucket.date)
            changeSize(bucket.size)

            root.setOnClickListener {
                val newData = mList[holder.layoutPosition]
                if (!selectList.contains(newData)) checkSelect(position, newData)
                musicBucketEventListener?.onBucketClicked(newData)
            }

            if (bucket.name == ALL_MUSIC) {
                musicBucketAction.isGone = true
            } else musicBucketAction.setOnClickListener {
                closeMusicBucketBoard()
            }
            musicBucketEdit.setOnClickListener {
                val layoutPosition = holder.layoutPosition
                musicBucketEventListener?.edit(mList[layoutPosition], layoutPosition)
                closeMusicBucketBoard()
            }
            musicBucketDelete.setOnClickListener {
                val layoutPosition = holder.layoutPosition
                musicBucketEventListener?.delete(mList[layoutPosition], layoutPosition)
                closeMusicBucketBoard()
            }
            musicBucketAddList.setOnClickListener {
                closeMusicBucketBoard()
                val layoutPosition = holder.layoutPosition
                musicBucketEventListener?.addMusic(mList[layoutPosition], layoutPosition)
            }
        }
    }

    override fun onFailedToRecycleView(holder: Holder<MusicBucketAdapterLayoutBinding>): Boolean {
        return true
    }

    private fun MusicBucketAdapterLayoutBinding.closeMusicBucketBoard() {
        if (!root.isVisible) return
        if (musicBucketBoard.progress != 0f && musicBucketBoard.progress != 1f) return
        val isOpen = musicBucketBoard.progress == 1f
        ValueAnimator.ofFloat(if (isOpen) 1f else 0f, if (isOpen) 0f else 1f).apply {
            addUpdateListener {
                musicBucketBoard.progress = it.animatedValue as Float
            }
        }.start()
    }

    private fun MusicBucketAdapterLayoutBinding.changeIcon(musicBucket: MusicBucket) {
        musicBucketIcon.also {
            when {
                musicBucket.icon != null -> loadIcon(it, iconPath = musicBucket.icon)
                musicBucket.tempIcon != null -> loadIcon(it, iconPath = musicBucket.tempIcon)
                else -> loadIcon(it, drawable = R.drawable.ic_album.getDrawable())
            }
        }
    }

    private fun MusicBucketAdapterLayoutBinding.changeName(name: String) {
        musicBucketName.text = name
    }

    private fun MusicBucketAdapterLayoutBinding.changeTime(time: String?) {
        if (time == null) return
        musicBucketTime.text = time
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
            .placeholder(R.drawable.ic_album)
            .overwrite(imageView.measuredWidth, imageView.measuredHeight)
            .into(imageView)
    }

    fun setSelect(bucketName: String) {
        emit(MusicBucketAEvent.SelectBucket(bucketName))
    }

    fun getSelectedBucket() = try {
        selectList.first
    } catch (e: NoSuchElementException) {
        null
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
        fun addMusic(musicBucket: MusicBucket, position: Int)
        fun delete(musicBucket: MusicBucket, position: Int)
        fun edit(musicBucket: MusicBucket, position: Int)
        fun onSelectedBucketRefresh(bucket: MusicBucket, state: Int)
    }

}