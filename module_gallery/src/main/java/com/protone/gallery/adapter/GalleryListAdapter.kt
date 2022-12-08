package com.protone.gallery.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.ImageView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.protone.common.entity.GalleryMedia
import com.protone.common.utils.displayUtils.imageLoader.Image
import com.protone.component.view.adapter.SelectListAdapter
import com.protone.gallery.databinding.GalleryListAdapterLayoutBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GalleryListAdapter(
    context: Context,
    private val useSelect: Boolean = true,
    private val combine: Boolean = false,
    private var itemCount: Int,
    private val preLoad: Int = 5
) : SelectListAdapter<GalleryListAdapterLayoutBinding, GalleryMedia, GalleryListAdapter.GalleryListEvent>(
    context, true
) {

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

    private var itemLength = 0
    private var onSelectMod = false

    private var layoutManager: LinearLayoutManager? = null

    @SuppressLint("NotifyDataSetChanged")
    override suspend fun handleEventAsynchronous(data: GalleryListEvent) {
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
                for (i in 0 until mList.size) {
                    selectList.add(mList[i])
                    notifyItemChangedCO(i)
                }
            }
            is GalleryListEvent.NoticeDataUpdate -> {
                if (data.item == null) return
                notifyListChangedCO(data.item)
            }
            is GalleryListEvent.NoticeSelectChange -> {
                val indexOf = mList.indexOf(data.item)
                if (indexOf != -1) {
                    onSelectMod = true
                    notifyItemChangedCO(indexOf)
                }
            }
            is GalleryListEvent.RemoveMedia -> {
                val index = mList.indexOf(data.galleryMedia)
                if (index != -1) {
                    mList.removeAt(index)
                    if (selectList.contains(data.galleryMedia)) selectList.remove(data.galleryMedia)
                    notifyItemRemovedCO(index)
                }
            }
            is GalleryListEvent.NoticeListItemUpdate -> {
                val index = mList.indexOf(data.media)
                if (index != -1) {
                    mList[index] = data.media
                    notifyItemChangedCO(index)
                }
            }
            is GalleryListEvent.NoticeListItemDelete -> {
                val index = mList.indexOf(data.media)
                if (index != -1) {
                    mList.removeAt(index)
                    notifyItemRemovedCO(index)
                }
            }
            is GalleryListEvent.NoticeListItemInsert -> {
                withContext(Dispatchers.Main) {
                    mList.add(0, data.media)
                    notifyItemInserted(0)
                }
            }
        }
    }

    override val select: (GalleryListAdapterLayoutBinding, Int, Boolean) -> Unit =
        { binding, _, select ->
            binding.apply {
                checkSeen.isVisible = select
                checkCheck.isChecked = select
            }
        }

    override fun itemIndex(path: GalleryMedia): Int {
        return mList.indexOf(path)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        layoutManager =
            recyclerView.layoutManager.takeIf { it is LinearLayoutManager } as LinearLayoutManager
        itemLength = (recyclerView.width - recyclerView.paddingEnd - recyclerView.paddingStart) / 4
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
        if (position >= mList.size) {
            //TODO 实现加载效果
            holder.binding.imageView.setImageResource(com.protone.component.R.drawable.main_background)
            return
        }
        setSelect(holder.binding, position, mList[position] in selectList)
        holder.binding.videoIcon.isGone = !mList[position].isVideo && !combine
        holder.binding.imageView.let { image ->
            Image.load(mList[position].thumbnailUri).with(context).into(image)
            image.setOnClickListener {
                if (onSelectMod) {
                    checkSelect(position, mList[position])
                    onSelectListener?.select(mList[position])
                    onSelectListener?.select(selectList)
                } else onSelectListener?.openView(mList[position])
            }
            if (useSelect) {
                image.setOnLongClickListener {
                    onSelectMod = true
                    checkSelect(position, mList[position])
                    onSelectListener?.select(mList[position])
                    onSelectListener?.select(selectList)
                    true
                }
            }
        }
    }

    override fun getItemCount(): Int = itemCount

    override fun setData(collection: Collection<GalleryMedia>) {
        super.setData(collection)
        if (mList.size > itemCount) {
            itemCount = mList.size
        }
        layoutManager?.let {
            notifyItemRangeChanged(
                it.findFirstVisibleItemPosition().let { first ->
                    if (first <= 0) 0
                    else if (first >= preLoad) first - preLoad
                    else first
                },
                it.findLastVisibleItemPosition().let { last ->
                    if (last >= itemCount) itemCount
                    else if (last <= itemCount - preLoad) last + preLoad
                    else last
                }
            )
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