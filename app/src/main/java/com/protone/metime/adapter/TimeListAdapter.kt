package com.protone.metime.adapter

import android.graphics.drawable.Drawable
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.protone.common.baseType.toDateString
import com.protone.common.context.newLayoutInflater
import com.protone.common.entity.GalleryMedia
import com.protone.component.view.adapter.BaseAdapter
import com.protone.component.view.customView.videoPlayer.DefaultVideoController
import com.protone.metime.databinding.TimePhotoCardLayoutBinding
import com.protone.metime.databinding.TimeVideoCardLayoutBinding

class TimeListAdapter(
    private val cardEvent: CardEvent,
    private val glideLoader: RequestBuilder<Drawable>
) : PagingDataAdapter<GalleryMedia, BaseAdapter.Holder<ViewDataBinding>>(
        object : DiffUtil.ItemCallback<GalleryMedia>() {
            override fun areItemsTheSame(oldItem: GalleryMedia, newItem: GalleryMedia): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: GalleryMedia, newItem: GalleryMedia): Boolean {
                return oldItem == newItem
            }
        }
    ) {

    companion object {
        private const val PHOTO = 0
        private const val VIDEO = 1
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position)?.let {
            if (it.isVideo) VIDEO else PHOTO
        } ?: PHOTO
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseAdapter.Holder<ViewDataBinding> = BaseAdapter.Holder(
        when (viewType) {
            VIDEO -> TimeVideoCardLayoutBinding.inflate(
                parent.context.newLayoutInflater,
                parent,
                false
            )
            else -> TimePhotoCardLayoutBinding.inflate(
                parent.context.newLayoutInflater,
                parent,
                false
            )
        }
    )

    override fun onBindViewHolder(
        holder: BaseAdapter.Holder<ViewDataBinding>,
        position: Int
    ): Unit = holder.binding.let { binding ->
        when (binding) {
            is TimePhotoCardLayoutBinding -> getItem(position)?.let { media ->
                glideLoader.load(media.uri)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.photo)
                binding.title.text = media.date.toDateString("yyyy/MM/dd")
                binding.timePhoto.setOnClickListener {
                    cardEvent.onPhotoClick(media)
                }
            }
            is TimeVideoCardLayoutBinding -> getItem(position)?.let { media ->
                binding.videoPlayer.controller = DefaultVideoController(binding.root.context)
                binding.videoPlayer.setPath(media.uri,glideLoader)
                binding.title.text = media.date.toDateString()
//                binding.videoPlayer.setFullScreen {
//                    cardEvent.onVideoClick(media)
//                }
            }
        }
    }

    interface CardEvent {
        fun onPhotoClick(media: GalleryMedia)
        fun onVideoClick(media: GalleryMedia)
    }

}