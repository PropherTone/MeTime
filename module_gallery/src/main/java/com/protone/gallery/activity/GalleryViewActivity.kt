package com.protone.gallery.activity

import androidx.activity.viewModels
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.protone.common.baseType.*
import com.protone.common.context.intent
import com.protone.common.context.root
import com.protone.common.entity.GalleryMedia
import com.protone.common.utils.ALL_GALLERY
import com.protone.common.utils.RouterPath
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryViewWire.CUSTOM_GALLERY
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryViewWire.GALLERY
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryViewWire.IS_VIDEO
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryViewWire.MEDIA
import com.protone.common.utils.RouterPath.NoteRouterPath.NoteViewWire.noteViewPostcard
import com.protone.common.utils.json.toEntity
import com.protone.common.utils.json.toJson
import com.protone.common.utils.json.toUri
import com.protone.common.utils.onResult
import com.protone.component.activity.BaseMediaActivity
import com.protone.component.R
import com.protone.component.tools.ViewEventHandle
import com.protone.component.tools.ViewEventHandler
import com.protone.component.view.adapter.BaseAdapter
import com.protone.component.view.adapter.CatoListAdapter
import com.protone.gallery.adapter.MediaViewAdapter
import com.protone.gallery.adapter.NoteLinkListAdapter
import com.protone.gallery.databinding.GalleryViewActivityBinding
import com.protone.gallery.fragment.GalleryViewFragment
import com.protone.gallery.viewModel.GalleryViewViewModel
import kotlinx.coroutines.launch

@Route(path = RouterPath.GalleryRouterPath.GalleryView)
class GalleryViewActivity : BaseMediaActivity<
        GalleryViewActivityBinding,
        GalleryViewViewModel,
        GalleryViewViewModel.GalleryViewEvent>(),
    ViewEventHandle<GalleryViewViewModel.GalleryViewEvent> by ViewEventHandler() {
    override val viewModel: GalleryViewViewModel by viewModels()

    override fun createView(): GalleryViewActivityBinding {
        postponeEnterTransition()
        return GalleryViewActivityBinding.inflate(layoutInflater, root, false).apply {
            activity = this@GalleryViewActivity
            galleryVCover.fitStatuesBarUsePadding()
            popLayout.galleryIntoBox.isGone = true
            popLayout.gallerySelectAll.isGone = true
            next.setOnClickListener {
                galleryVView.setCurrentItem(1 + galleryVView.currentItem, true)
            }
            previous.setOnClickListener {
                galleryVView.setCurrentItem(galleryVView.currentItem - 1, true)
            }
        }
    }

    override suspend fun GalleryViewViewModel.init() {
        intent.extras.let {
            val targetGallery = it?.getString(GALLERY) ?: ALL_GALLERY
            val isVideo = it?.getBoolean(IS_VIDEO) ?: false
            val isCustom = it?.getBoolean(CUSTOM_GALLERY) ?: false
            initGalleryData(targetGallery, isVideo, isCustom)
        }

        val mediaIndex = getMediaIndex()
        binding.initViewPager(mediaIndex, galleryMedias)
        binding.initList()
        setMediaInfo(mediaIndex)
        setInfo()
        observeEvent()
    }

    private fun GalleryViewActivityBinding.initViewPager(
        position: Int,
        data: MutableList<GalleryMedia>
    ) {
        galleryVView.apply {
            adapter = MediaViewAdapter(
                this@GalleryViewActivity,
                Glide.with(this@GalleryViewActivity)
            ).apply {
                setData(data)
                onSingleTap = {
                    galleryVCover.isVisible = !galleryVCover.isVisible
                    if (galleryVCover.isVisible) {
                        sendViewEvent(GalleryViewViewModel.GalleryViewEvent.SetNote)
                    }
                }
            }
//            adapter = object : FragmentStateAdapter(this@GalleryViewActivity) {
//                override fun getItemCount(): Int = data.size
//                override fun getItemViewType(position: Int): Int = position
//                override fun createFragment(position: Int): Fragment =
//                    GalleryViewFragment(data[position], singleClick = {
//                        galleryVCover.isVisible = !galleryVCover.isVisible
//                        if (galleryVCover.isVisible) {
//                            sendViewEvent(GalleryViewViewModel.GalleryViewEvent.SetNote)
//                        }
//                    })
//            }
            galleryVCover.isVisible = false

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    viewModel.curPosition = position
                    viewModel.setMediaInfo(position)
                    super.onPageSelected(position)
                }

                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)
                    if (state == ViewPager2.SCREEN_STATE_OFF && galleryVCover.isVisible) {
                        sendViewEvent(GalleryViewViewModel.GalleryViewEvent.SetNote)
                    }
                }
            })
            setCurrentItem(position, false)
        }
    }

    private fun GalleryViewActivityBinding.initList() {
        galleryVLinks.apply {
            layoutManager = LinearLayoutManager(context).also {
                it.orientation = LinearLayoutManager.HORIZONTAL
            }
            adapter = NoteLinkListAdapter(this@GalleryViewActivity, listOf()).also {
                it.startNote = {
                    startActivity(RouterPath.NoteRouterPath.NoteView) { noteViewPostcard(it) }
                }
            }
        }
        galleryVCatoContainer.apply {
            layoutManager = LinearLayoutManager(context).also {
                it.orientation = LinearLayoutManager.HORIZONTAL
            }
            adapter = CatoListAdapter(this@GalleryViewActivity,
                Glide.with(this@GalleryViewActivity).asDrawable(),
                object : CatoListAdapter.CatoListDataProxy {
                    override suspend fun getMedia(cate: String): GalleryMedia? =
                        viewModel.getMediaByUri(cate.toUri())
                }).also {
                it.setItemClick { cate ->
                    launch {
                        val media = viewModel.getMediaByUri(cate.toUri())
                        startActivity(
                            GalleryViewActivity::class.intent
                                .putExtra(MEDIA, media?.toJson())
                                .putExtra(IS_VIDEO, media?.isVideo ?: false)
                        )
                    }
                }
            }
        }
    }

    private fun GalleryViewViewModel.observeEvent() {
        onViewEvent(this@GalleryViewActivity, this@GalleryViewActivity) {
            when (it) {
                GalleryViewViewModel.GalleryViewEvent.SetNote -> setInfo()
                GalleryViewViewModel.GalleryViewEvent.Share -> shareGalleryMedia(prepareSharedMedia())
            }
        }
    }

    private suspend fun GalleryViewViewModel.setInfo() = withDefaultContext {
        getSignedMedia()?.let {
            refreshCate(it.cate)
            setNotes(getNotesWithGallery(it.mediaId))
        }
    }

    private fun GalleryViewViewModel.setMediaInfo(position: Int) {
        if (position >= 0 && position < galleryMedias.size) {
            galleryMedias[position].let { m ->
                binding.setMediaInfo(
                    m.name,
                    m.date.toDateString("yyyy/MM/dd").toString(),
                    m.size.getStorageSize(),
                    m.path ?: m.uri.toString()
                )
            }
        }
    }

    private suspend fun GalleryViewViewModel.getMediaIndex() = onResult { co ->
        val galleryMedia = intent.getStringExtra(MEDIA)?.toEntity(GalleryMedia::class.java)
        val index = galleryMedias.indexOf(galleryMedia)
        curPosition = index
        co.resumeWith(Result.success(index))
    }

    private fun GalleryViewActivityBinding.setMediaInfo(
        title: String,
        time: String,
        size: String,
        type: String,
    ) {
        galleryVTitle.text = title

        galleryVTime.text = String.format(
            R.string.time.getString(),
            time.ifEmpty { R.string.none.getString() })

        galleryVSize.text = String.format(
            R.string.size.getString(),
            size.ifEmpty { R.string.none.getString() })

        galleryVPath.text = String.format(
            R.string.location.getString(),
            type.ifEmpty { R.string.none.getString() })
    }

    private suspend fun setNotes(notes: List<String>) = withMainContext {
        if (binding.galleryVLinks.adapter is NoteLinkListAdapter)
            (binding.galleryVLinks.adapter as NoteLinkListAdapter).notifyListChangedCO(notes)
    }

    private suspend fun refreshCate(cate: List<String>?) = withMainContext {
        (binding.galleryVCatoContainer.adapter as CatoListAdapter).refresh(cate)
    }

    fun showPop() {
        showPop(binding.galleryVAction)
    }

    override fun popDelete() {
        tryDelete(mutableListOf(viewModel.getCurrentMedia())) {
            if (it.size == 1) {
                val index = viewModel.galleryMedias.indexOf(it[0])
                if (index != -1) {
                    viewModel.galleryMedias.removeAt(index)
                    (binding.galleryVView.adapter as BaseAdapter<*, *, *>?)
                        ?.notifyItemRemovedChecked(index)
                }
            }
        }
    }

    override fun popMoveTo() {
        launch {
            moveTo(
                binding.galleryVAction,
                mutableListOf(viewModel.getCurrentMedia())
            ) { _, _ -> }
        }
    }

    override fun popRename() {
        tryRename(mutableListOf(viewModel.getCurrentMedia()))
    }

    override fun popSetCate() {
        addCate(mutableListOf(viewModel.getCurrentMedia()))
    }

    fun popSelectAll() = Unit

    fun popIntoBox() = Unit
}