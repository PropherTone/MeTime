package com.protone.gallery.activity

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
import android.view.ViewAnimationUtils
import androidx.activity.viewModels
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.protone.common.baseType.*
import com.protone.common.context.intent
import com.protone.common.context.root
import com.protone.common.entity.Gallery
import com.protone.common.entity.Gallery.ItemState
import com.protone.common.utils.ALL_GALLERY
import com.protone.common.utils.IntentDataHolder
import com.protone.common.utils.RouterPath
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryMainWire.CHOOSE_MEDIA
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryMainWire.CHOOSE_PHOTO
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryMainWire.CHOOSE_VIDEO
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryMainWire.GALLERY_DATA
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryMainWire.URI
import com.protone.common.utils.json.toJson
import com.protone.common.utils.json.toUriJson
import com.protone.component.BaseViewModel
import com.protone.component.activity.BaseMediaActivity
import com.protone.component.database.userConfig
import com.protone.component.toPictureBox
import com.protone.component.view.dialog.titleDialog
import com.protone.gallery.R
import com.protone.gallery.adapter.GalleryBucketAdapter
import com.protone.gallery.adapter.GalleryListStateAdapter
import com.protone.gallery.component.GalleryBucketItemDecoration
import com.protone.gallery.databinding.GalleryActivityBinding
import com.protone.gallery.fragment.GalleryListFragment
import com.protone.gallery.viewModel.GalleryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.hypot

private typealias componentDrawable = com.protone.component.R.drawable
private typealias componentString = com.protone.component.R.string
private typealias componentAni = com.protone.component.R.anim

@Route(path = RouterPath.GalleryRouterPath.Main)
class GalleryActivity :
    BaseMediaActivity<GalleryActivityBinding, GalleryViewModel, BaseViewModel.ViewEvent>() {
    override val viewModel: GalleryViewModel by viewModels()

    internal inner class BindModel {

        fun finishEvent() {
            if (onSelectMode) exitSelectMode() else finish()
        }

        fun showBucket() {
            val progress = binding.motionRoot.progress
            if (progress != 0f && progress != 1f) return
            ValueAnimator.ofFloat(progress, abs(progress - 1f)).apply {
                addUpdateListener {
                    binding.motionRoot.progress = (it.animatedValue as Float)
                }
            }.start()
            viewModel.drawerStateChanged(progress != 1f)
        }

        fun showPop() {
            showPop(binding.galleryActionMenu, viewModel.selectedMedias.size <= 0)
        }

        fun toSearch() {
            launch {
                IntentDataHolder.put(viewModel.getRightGalleryMedias())
                startActivity(GallerySearchActivity::class.intent.also { intent ->
                    intent.putExtra("gallery", viewModel.rightGallery)
                })
            }
        }

        fun addBucket() {
            titleDialog(componentString.user_name.getString(), "") {
                viewModel.addBucket(it)
            }
        }

    }

    private var isInit = false

    private var onSelectMode = false
        set(value) {
            if (field == value) return
            binding.finish.setImageResource(
                if (value) componentDrawable.ic_round_close_24_white
                else componentDrawable.ic_round_arrow_left_white_24
            )
            field = value
        }

    override fun createView(): GalleryActivityBinding {
        return GalleryActivityBinding.inflate(layoutInflater, root, false).apply {
            model = BindModel()
            root.fitStatuesBar()
        }
    }

    override suspend fun GalleryViewModel.init() {
        val chooseType =
            intent.getStringExtra(RouterPath.GalleryRouterPath.GalleryMainWire.CHOOSE_MODE) ?: ""

        val notEmpty = chooseType.isNotEmpty()
        if (notEmpty) binding.apply {
            galleryActionMenu.isVisible = false
            galleryChooseConfirm.isGone = true
            galleryChooseConfirm.setOnClickListener {
                getSelectedMedias().ifNotEmpty { list ->
                    setResult(
                        RESULT_OK,
                        Intent().putExtra(URI, list.last().uri.toUriJson())
                            .putExtra(GALLERY_DATA, list.last().toJson())
                    )
                }
                finish()
            }
        }

        observeEvent()
        sortData()
        initList()
        initPager(chooseType)
    }

    private fun GalleryViewModel.observeEvent() {
        launchDefault {
            galleryFlow.bufferCollect {
                while (!isInit) delay(20L)
                when (it) {
                    GalleryViewModel.GalleryEvent.OnSelectedMode -> onSelectMode = true
                    GalleryViewModel.GalleryEvent.ExitSelectedMode -> onSelectMode = false
                    is GalleryViewModel.GalleryEvent.OnNewGallery -> {
                        getBucketAdapter().insertBucket(it.gallery)
                    }
                    is GalleryViewModel.GalleryEvent.OnGalleryRemoved -> {
                        getBucketAdapter().apply {
                            getBucket(ALL_GALLERY)?.let { g -> setSelected(g) }
                            deleteBucket(it.gallery)
                        }
                    }
                    is GalleryViewModel.GalleryEvent.OnGalleryUpdated -> {
                        getBucketAdapter().apply {
                            if (it.gallery.name == rightGallery) {
                                binding.refreshSelectGallery(it.gallery, it.itemState, false)
                            }
                            refreshBucket(it.gallery, it.itemState)
                            getBucket(ALL_GALLERY)?.let { gallery ->
                                refreshBucket(gallery, it.itemState)
                            }
                        }
                    }
                    is GalleryViewModel.GalleryEvent.OnNewGalleries -> {
                        getBucketAdapter().insertBucket(it.galleries)
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun initList() {
        binding.galleryBucket.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = GalleryBucketAdapter(context, Glide.with(this@GalleryActivity).asDrawable()) {
                selectBucket { gallery ->
                    launch { setSelectGallery(gallery) }
                }
                deleteGalleryBucket { viewModel.deleteGalleryBucket(it) }
            }
            addItemDecoration(
                GalleryBucketItemDecoration(resources.getDimension(R.dimen.bucket_margin).toInt())
            )
        }
    }

    private fun initPager(chooseType: String = "") {
        val combine = userConfig.combineGallery || chooseType == CHOOSE_MEDIA
        binding.galleryPager.adapter = GalleryListStateAdapter(
            this@GalleryActivity,
            mutableListOf<GalleryListFragment>().also { fs ->
                var initializeSize = 0
                fun generateFragment(isVideo: Boolean) {
                    GalleryListFragment().also {
                        it.connect(
                            chooseType.isEmpty(),
                            viewModel.generateMailer(isVideo).onSubscription {
                                viewModel.getBucket(ALL_GALLERY)?.let { gallery ->
                                    getBucketAdapter().setSelected(gallery)
                                    setSelectGallery(gallery)
                                }
                                if (++initializeSize == fs.size) isInit = true
                            },
                            viewModel.selectedMedias
                        )
                        fs.add(it)
                    }
                }

                when (chooseType) {
                    CHOOSE_PHOTO -> generateFragment(false)
                    CHOOSE_VIDEO -> generateFragment(true)
                    else -> {
                        generateFragment(false)
                        if (!combine) generateFragment(true)
                    }
                }
            }
        )
        when (chooseType) {
            CHOOSE_PHOTO -> arrayOf(R.string.photo)
            CHOOSE_VIDEO -> arrayOf(R.string.video)
            else -> {
                if (combine) arrayOf(componentString.model_gallery)
                else arrayOf(R.string.photo, R.string.video)
            }
        }.let { tabList ->
            TabLayoutMediator(
                binding.galleryTab.apply {
                    addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                        override fun onTabSelected(tab: TabLayout.Tab?) {
                            tab?.text?.let { if (viewModel.onTabChanged(it)) launch { onGalleryTabSwapped() } }
                        }

                        override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
                        override fun onTabReselected(tab: TabLayout.Tab?) = Unit
                    })
                },
                binding.galleryPager
            ) { tab, position -> tab.setText(tabList[position]) }.attach()
        }
    }

    private suspend fun onGalleryTabSwapped() {
        viewModel.drawerStateChanged(binding.motionRoot.progress == 1f)
        getBucketAdapter().setData(viewModel.getGalleryData())
        viewModel.getSelectedBucket()?.let { gallery ->
            getBucketAdapter().setSelected(gallery)
            setSelectGallery(gallery)
        }
    }

    private suspend fun setSelectGallery(gallery: Gallery) {
        viewModel.onGallerySelected(gallery, binding.motionRoot.progress == 1f)
        binding.refreshSelectGallery(gallery)
    }

    private suspend fun GalleryActivityBinding.refreshSelectGallery(
        gallery: Gallery,
        itemState: ItemState = ItemState.ALL_CHANGED,
        doAni: Boolean = true
    ): Unit = withMainContext {
        when (itemState) {
            ItemState.ALL_CHANGED -> {
                loadGalleyUri(gallery.uri)
                galleryName.text = gallery.name
                galleryItemNumber.text = gallery.size.toString()
            }
            ItemState.SIZE_CHANGED -> galleryItemNumber.text = gallery.size.toString()
            ItemState.URI_CHANGED -> loadGalleyUri(gallery.uri)
        }
        if (doAni) getReveal()?.start()
    }

    private fun GalleryActivityBinding.loadGalleyUri(uri: Uri?) {
        Glide.with(this@GalleryActivity)
            .load(uri)
            .error(componentDrawable.ic_baseline_image_24_white)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(galleryAction)
    }

    private fun GalleryActivityBinding.getReveal(): Animator? {
        val mX = galleryDetail.measuredWidth / 2
        val mY = galleryDetail.measuredHeight / 2
        val radius = hypot(mX.toDouble(), mY.toDouble()).toFloat()
        return ViewAnimationUtils.createCircularReveal(
            galleryDetail, mX,
            mY, 0f, radius
        ).also { it.duration = 500L }
    }

    private fun exitSelectMode() {
        viewModel.exitSelect()
        onSelectMode = false
    }

    private fun getBucketAdapter() = binding.galleryBucket.adapter as GalleryBucketAdapter

    override fun getSwapAnim(): Pair<Int, Int>? {
        if (!binding.galleryChooseConfirm.isGone) return null
        return Pair(componentAni.card_in_rtl, componentAni.card_out_rtl)
    }

    override fun onBackPressed() {
        if (!doOnBackPressed()) {
            if (onSelectMode) {
                exitSelectMode()
                return
            }
        } else return
        super.onBackPressed()
    }

    override fun popDelete() {
        viewModel.apply {
            if (removeMediasFromCustomGallery(getSelectedMedias())) return
            tryDelete(getSelectedMedias()) {}
        }
    }

    override fun popMoveTo() {
        viewModel.getSelectedMedias().ifNotEmpty {
            moveTo(binding.galleryActionMenu, it) { _, _ -> }
        }
    }

    override fun popRename() {
        viewModel.getSelectedMedias().ifNotEmpty { tryRename(it) }
    }

    override fun popSelectAll() {
        viewModel.selectAll()
    }

    override fun popSetCate() {
        viewModel.getSelectedMedias().ifNotEmpty { addCate(it) }
    }

    override fun popIntoBox() {
        launch {
            viewModel.getSelectedMedias().let {
                toPictureBox(it.ifEmpty { viewModel.getRightGalleryMedias() ?: it })
            }
        }
    }

}