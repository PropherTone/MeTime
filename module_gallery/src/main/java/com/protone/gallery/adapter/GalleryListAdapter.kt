package com.protone.gallery.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.ImageView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestBuilder
import com.protone.common.context.MApplication
import com.protone.common.entity.GalleryMedia
import com.protone.component.R
import com.protone.component.view.adapter.SelectListAdapter
import com.protone.gallery.databinding.GalleryListAdapterLayoutBinding

class GalleryListAdapter(
    context: Context,
    val glideLoader: RequestBuilder<Drawable>,
    private val useSelect: Boolean = true,
    private val combine: Boolean = false,
    private var itemCount: Int,
    private val preLoad: Int = 5
) : SelectListAdapter<GalleryListAdapterLayoutBinding, GalleryMedia, GalleryListAdapter.GalleryListEvent>(
    context, true
) {

    sealed class GalleryListEvent {
        object SelectAll : GalleryListEvent()
        object ExitSelectAll : GalleryListEvent()
        data class NoticeSelectChange(val item: GalleryMedia) : GalleryListEvent()
        data class NoticeListItemUpdate(val media: GalleryMedia) : GalleryListEvent()
        data class NoticeListItemDelete(val media: GalleryMedia) : GalleryListEvent()
        data class NoticeListItemInsert(val media: GalleryMedia) : GalleryListEvent()
        data class NoticeListInsert(val medias: List<GalleryMedia>) : GalleryListEvent()
    }

    var itemLength = 0
    var onSelectMod = false

    override suspend fun handleEventAsynchronous(data: GalleryListEvent) {
        when (data) {
            is GalleryListEvent.ExitSelectAll -> {
                if (!onSelectMod) return
                onSelectMod = false
                clearAllSelected()
            }
            is GalleryListEvent.SelectAll -> {
                onSelectMod = true
                selectList.addAll(mList)
                onSelectListener?.select(selectList.toList())
                notifyItemRangeChangedCO(0, mList.size, SELECT)
            }
            is GalleryListEvent.NoticeSelectChange -> {
                val indexOf = mList.indexOf(data.item)
                if (indexOf != -1) {
                    onSelectMod = true
                    notifyItemChangedCO(indexOf)
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
                    if (selectList.contains(data.media)) selectList.remove(data.media)
                    itemCount = mList.size
                    notifyItemRemovedCO(index)
                }
            }
            is GalleryListEvent.NoticeListItemInsert -> {
                mList.add(0, data.media)
                itemCount = mList.size
                notifyItemInsertedCO(0)
            }
            is GalleryListEvent.NoticeListInsert -> {
                mList.addAll(0, data.medias)
                itemCount = mList.size
                notifyItemRangeInsertedCO(0, data.medias.size)
            }
        }
    }

    override fun setSelect(
        content: GalleryListAdapterLayoutBinding,
        position: Int,
        isSelect: Boolean
    ) {
        content.apply {
            checkSeen.isVisible = isSelect
            checkCheck.isChecked = isSelect
        }
    }

    override fun itemIndex(path: GalleryMedia): Int {
        return mList.indexOf(path)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        if (itemLength == 0) {
            itemLength = MApplication.screenWidth / 4
        }
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
                height = this@GalleryListAdapter.itemLength
            }
            checkSeen.updateLayoutParams {
                height = this@GalleryListAdapter.itemLength
            }
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        })
    }

    override fun onBindViewHolder(holder: Holder<GalleryListAdapterLayoutBinding>, position: Int) {
        holder.binding.apply {
            if (position >= mList.size) {
                //TODO 实现加载效果
                imageView.setImageResource(R.drawable.none_state_background)
                return
            }
            setSelect(holder.binding, position, mList[position] in selectList)
            videoIcon.isGone = !mList[position].isVideo && !combine
            imageView.let { image ->
                glideLoader.load(mList[position].thumbnailUri)
                    .placeholder(R.drawable.none_state_background).into(image)
//                Image.load(mList[position].thumbnailUri)
//                    .with(context)
//                    .placeholder(R.drawable.none_state_background)
//                    .into(image)
                image.setOnClickListener {
                    val layoutPosition = holder.layoutPosition
                    mList[layoutPosition].let { media ->
                        if (onSelectMod) {
                            checkSelect(layoutPosition, media)
                            onSelectListener?.select(media)
                        } else onSelectListener?.openView(media, imageView)
                    }
                }
                if (useSelect) {
                    image.setOnLongClickListener {
                        val layoutPosition = holder.layoutPosition
                        mList[layoutPosition].let { media ->
                            onSelectMod = true
                            checkSelect(layoutPosition, media)
                            onSelectListener?.select(media)
                            onSelectListener?.onItemLongClick()
                        }
                        true
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = itemCount

    override fun setData(collection: Collection<GalleryMedia>) {
        if (collection.isEmpty()) return
        refreshVisiblePosition()
        super.setData(collection)
        if (mList.size > itemCount) {
            itemCount = mList.size
            notifyItemRangeInsertedChecked(mList.size - collection.size, collection.size)
            return
        }
        notifyItemRangeChangedChecked(
            when {
                firstPosition <= 0 -> 0
                firstPosition >= preLoad -> firstPosition - preLoad
                else -> 0
            },
            when {
                lastPosition >= itemCount || lastPosition == -1 -> itemCount
                lastPosition <= itemCount - preLoad -> lastPosition + preLoad
                else -> itemCount
            }
        )
    }

    fun select(position: Int) {
        if (position <= 0 || position >= mList.size) return
        mList[position].takeIf { !selectList.contains(it) }?.let {
            selectList.add(it)
            notifyItemChanged(position, SELECT)
            onSelectListener?.select(it)
        }
    }

    fun selectAll() {
        emit(GalleryListEvent.SelectAll)
    }

    fun exitSelectMod() {
        emit(GalleryListEvent.ExitSelectAll)
    }

    fun noticeSelectChange(item: GalleryMedia) {
        emit(GalleryListEvent.NoticeSelectChange(item))
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

    fun noticeListInsert(media: List<GalleryMedia>) {
        emit(GalleryListEvent.NoticeListInsert(media))
    }

    private var onSelectListener: OnSelect? = null

    interface OnSelect {
        fun select(media: GalleryMedia) {}
        fun select(medias: List<GalleryMedia>) {}
        fun openView(galleryMedia: GalleryMedia, elementView: View) {}
        fun onItemLongClick() {}
    }

    fun setNewSelectList(list: List<GalleryMedia>) {
        selectList.clear()
        selectList.addAll(list)
    }

    fun setOnSelectListener(listener: OnSelect?) {
        this.onSelectListener = listener
    }
}