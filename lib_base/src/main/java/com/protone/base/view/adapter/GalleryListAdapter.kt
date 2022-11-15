package com.protone.base.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.ImageView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.protone.base.databinding.GalleryListAdapterLayoutBinding
import com.protone.common.context.MApplication
import com.protone.common.entity.GalleryMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GalleryListAdapter(
    context: Context,
    private val useSelect: Boolean = true,
    private val combine: Boolean = false
) : SelectListAdapter<GalleryListAdapterLayoutBinding, GalleryMedia, GalleryListAdapter.GalleryListEvent>(
    context, true
) {
    private val medias: MutableList<GalleryMedia> = mutableListOf()

    enum class MediaStatus {
        UPDATED,
        INSERTED,
        DELETED
    }

    sealed class GalleryListEvent {
        object SelectAll : GalleryListEvent()
        object QuiteSelectAll : GalleryListEvent()
        data class NoticeDataUpdate(val item: MutableList<GalleryMedia>?) : GalleryListEvent()
        data class NoticeSelectChange(val item: GalleryMedia) : GalleryListEvent()
        data class RemoveMedia(val galleryMedia: GalleryMedia) : GalleryListEvent()
        data class NoticeListItemUpdate(val media: GalleryMedia) : GalleryListEvent()
        data class NoticeListItemDelete(val media: GalleryMedia) : GalleryListEvent()
        data class NoticeListItemInsert(val media: GalleryMedia) : GalleryListEvent()
    }

    fun setMedias(list: MutableList<GalleryMedia>) {
        medias.addAll(list)
    }

    private var itemLength = 0
    private var onSelectMod = false

    @SuppressLint("NotifyDataSetChanged")
    override suspend fun onEventIO(data: GalleryListEvent) {
        when (data) {
            is GalleryListEvent.QuiteSelectAll -> {
                if (!onSelectMod) return
                onSelectMod = false
                clearAllSelected()
                withContext(Dispatchers.Main) {
                    onSelectListener?.select(selectList)
                }
            }
            is GalleryListEvent.SelectAll -> {
                onSelectMod = true
                for (i in 0 until medias.size) {
                    selectList.add(medias[i])
                    notifyItemChangedCO(i)
                }
            }
            is GalleryListEvent.NoticeDataUpdate -> {
                if (data.item == null) return
                medias.clear()
                medias.addAll(data.item)
                notifyDataSetChangedCO()
            }
            is GalleryListEvent.NoticeSelectChange -> {
                val indexOf = medias.indexOf(data.item)
                if (indexOf != -1) {
                    onSelectMod = true
                    notifyItemChangedCO(indexOf)
                }
            }
            is GalleryListEvent.RemoveMedia -> {
                val index = medias.indexOf(data.galleryMedia)
                if (index != -1) {
                    medias.removeAt(index)
                    if (selectList.contains(data.galleryMedia)) selectList.remove(data.galleryMedia)
                    notifyItemRemovedCO(index)
                }
            }
            is GalleryListEvent.NoticeListItemUpdate -> {
                val index = medias.indexOf(data.media)
                if (index != -1) {
                    medias[index] = data.media
                    notifyItemChangedCO(index)
                }
            }
            is GalleryListEvent.NoticeListItemDelete -> {
                val index = medias.indexOf(data.media)
                if (index != -1) {
                    medias.removeAt(index)
                    notifyItemRemovedCO(index)
                }
            }
            is GalleryListEvent.NoticeListItemInsert -> {
                withContext(Dispatchers.Main) {
                    medias.add(0, data.media)
                    notifyItemInserted(0)
                }
            }
        }
    }

    override val select: (Holder<GalleryListAdapterLayoutBinding>, Boolean) -> Unit =
        { holder, select ->
            holder.binding.apply {
                checkSeen.isVisible = select
                checkCheck.isChecked = select
            }
        }

    override fun itemIndex(path: GalleryMedia): Int {
        return medias.indexOf(path)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        itemLength = MApplication.screenWidth / 4 - recyclerView.paddingEnd
        recyclerView.layoutAnimationListener = object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                recyclerView.suppressLayout(true)
            }

            override fun onAnimationEnd(animation: Animation?) {
                recyclerView.suppressLayout(false)
            }

            override fun onAnimationRepeat(animation: Animation?) = Unit

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Holder<GalleryListAdapterLayoutBinding> {
        return Holder(GalleryListAdapterLayoutBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        ).apply {
            imageView.updateLayoutParams {
                width = this@GalleryListAdapter.itemLength
                height = width
            }
            checkSeen.updateLayoutParams {
                width = this@GalleryListAdapter.itemLength
                height = width
            }
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        })
    }

    override fun onBindViewHolder(holder: Holder<GalleryListAdapterLayoutBinding>, position: Int) {
        setSelect(holder, medias[position] in selectList)
        holder.binding.videoIcon.isGone = !medias[position].isVideo && !combine
        holder.binding.imageView.let { image ->
            Glide.with(context).load(medias[position].thumbnailUri).into(image)
            image.setOnClickListener {
                if (onSelectMod) {
                    checkSelect(holder, medias[position])
                    onSelectListener?.select(medias[position])
                    onSelectListener?.select(selectList)
                } else onSelectListener?.openView(medias[position])
            }
            if (useSelect) {
                image.setOnLongClickListener {
                    onSelectMod = true
                    checkSelect(holder, medias[position])
                    onSelectListener?.select(medias[position])
                    onSelectListener?.select(selectList)
                    true
                }
            }
        }
    }

    fun noticeDataUpdate(item: MutableList<GalleryMedia>?) {
        emit(GalleryListEvent.NoticeDataUpdate(item))
    }

    fun selectAll() {
        emit(GalleryListEvent.SelectAll)
    }

    fun quitSelectMod() {
        emit(GalleryListEvent.QuiteSelectAll)
    }

    fun noticeSelectChange(item: GalleryMedia) {
        emit(GalleryListEvent.NoticeSelectChange(item))
    }

    fun removeMedia(galleryMedia: GalleryMedia) {
        emit(GalleryListEvent.RemoveMedia(galleryMedia))
    }

    fun noticeListItemUpdate(media: GalleryMedia) {
        emit(GalleryListEvent.NoticeListItemUpdate(media))
    }

    fun noticeListItemDelete(media: GalleryMedia) {
        emit(GalleryListEvent.NoticeListItemDelete(media))
    }

    fun noticeListItemInsert(media: GalleryMedia) {
        emit(GalleryListEvent.NoticeListItemInsert(media))
    }

    override fun getItemCount(): Int {
        return medias.size
    }

    private var onSelectListener: OnSelect? = null

    interface OnSelect {
        fun select(galleryMedia: GalleryMedia)
        fun select(galleryMedia: MutableList<GalleryMedia>)
        fun openView(galleryMedia: GalleryMedia)
    }

    fun setNewSelectList(list: MutableList<GalleryMedia>) {
        selectList = list
    }

    fun setOnSelectListener(listener: OnSelect?) {
        this.onSelectListener = listener
    }
}