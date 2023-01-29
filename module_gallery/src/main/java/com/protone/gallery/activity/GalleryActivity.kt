package com.protone.gallery.activity

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
import android.view.ViewAnimationUtils
import androidx.activity.viewModels
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.protone.common.R
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
import com.protone.common.utils.displayUtils.imageLoader.Image
import com.protone.common.utils.displayUtils.imageLoader.constant.Transition
import com.protone.common.utils.json.toJson
import com.protone.common.utils.json.toUriJson
import com.protone.component.BaseMediaActivity
import com.protone.component.BaseViewModel
import com.protone.component.database.userConfig
import com.protone.component.view.dialog.titleDialog
import com.protone.gallery.adapter.GalleryBucketAdapter
import com.protone.gallery.adapter.MyFragmentStateAdapter
import com.protone.gallery.component.GalleryBucketItemDecoration
import com.protone.gallery.databinding.GalleryActivityBinding
import com.protone.gallery.fragment.GalleryListFragment
import com.protone.gallery.viewModel.GalleryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.hypot

@Route(path = RouterPath.GalleryRouterPath.Main)
class GalleryActivity :
    BaseMediaActivity<GalleryActivityBinding, GalleryViewModel, BaseViewModel.ViewEvent>() {
    override val viewModel: GalleryViewModel by viewModels()

    private var isInit = false

    private var onSelectMode = false
        set(value) {
            if (field == value) return
            binding.finish.setImageResource(
                if (value) com.protone.component.R.drawable.ic_round_close_24_white
                else com.protone.component.R.drawable.ic_round_arrow_left_white_24
            )
            field = value
        }

    override fun createView(): GalleryActivityBinding {
        return GalleryActivityBinding.inflate(layoutInflater, root, false).apply {
            activity = this@GalleryActivity
            root.fitStatuesBar()
        }
    }

    override suspend fun GalleryViewModel.init() {
        val chooseType =
            intent.getStringExtra(RouterPath.GalleryRouterPath.GalleryMainWire.CHOOSE_MODE) ?: ""

        val notEmpty = chooseType.isNotEmpty()
        if (notEmpty) {
            binding.galleryActionMenu.isVisible = false
            binding.galleryChooseConfirm.isGone = !notEmpty
            binding.galleryChooseConfirm.setOnClickListener {
                chooseData.let { list ->
                    if (list.size <= 0) return@let
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
        observeSelectData { onSelectMode = it }
        launchDefault {
            galleryFlow.bufferCollect {
                while (!isInit) delay(20L)
                when (it) {
                    is GalleryViewModel.GalleryEvent.OnNewGallery -> {
                        getBucketAdapter().insertBucket(it.gallery)
                    }
                    is GalleryViewModel.GalleryEvent.OnGalleryRemoved -> {
                        getBucketAdapter().apply {
                            viewModel.getBucket(ALL_GALLERY)?.let { g -> setSelected(g) }
                            deleteBucket(it.gallery)
                        }
                    }
                    is GalleryViewModel.GalleryEvent.OnGalleryUpdated -> {
                        viewModel.run {
                            getBucketAdapter().apply {
                                if (it.gallery.name == rightGallery) {
                                    refreshSelectGallery(it.gallery, it.itemState, false)
                                }
                                refreshBucket(it.gallery, it.itemState)
                                getBucket(ALL_GALLERY)?.let { gallery ->
                                    refreshBucket(gallery, it.itemState)
                                }
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
            adapter = GalleryBucketAdapter(context) {
                selectBucket { gallery ->
                    launch {
                        setSelectGallery(gallery)
                    }
                }
                deleteGalleryBucket {
                    viewModel.deleteGalleryBucket(it)
                }
            }
            addItemDecoration(
                GalleryBucketItemDecoration(
                    resources.getDimension(com.protone.gallery.R.dimen.bucket_margin).toInt()
                )
            )
        }
    }

    private fun GalleryViewModel.initPager(chooseType: String = "") {
        val combine = userConfig.combineGallery || chooseType == CHOOSE_MEDIA
        binding.galleryPager.adapter = MyFragmentStateAdapter(
            this@GalleryActivity,
            mutableListOf<Fragment>().also { fs ->
                var initializeSize = 0
                fun generateFragment(isVideo: Boolean) {
                    GalleryListFragment().also {
                        it.connect(chooseType.isEmpty(), generateMailer(isVideo).onSubscription {
                            getBucket(ALL_GALLERY)?.let { gallery ->
                                getBucketAdapter().setSelected(gallery)
                                setSelectGallery(gallery)
                            }
                            if (++initializeSize == fs.size) {
                                isInit = true
                            }
                        }, dataFlow)
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
                if (combine) arrayOf(R.string.model_gallery)
                else arrayOf(R.string.photo, R.string.video)
            }
        }.let { tabList ->
            TabLayoutMediator(
                binding.galleryTab.apply {
                    addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                        override fun onTabSelected(tab: TabLayout.Tab?) {
                            tab?.text?.let { if (onTabChanged(it)) launch { onGalleryTabSwapped() } }
                        }

                        override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
                        override fun onTabReselected(tab: TabLayout.Tab?) = Unit
                    })
                },
                binding.galleryPager
            ) { tab, position -> tab.setText(tabList[position]) }.attach()
        }
    }

    fun finishEvent() {
        if (onSelectMode) quiteSelectMode() else finish()
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
        showPop(binding.galleryActionMenu, viewModel.chooseData.size <= 0)
    }

    fun toSearch() {
        val gallery = viewModel.rightGallery
        IntentDataHolder.put(getGalleryData())
        startActivity(GallerySearchActivity::class.intent.also { intent ->
            intent.putExtra("gallery", gallery)
        })
    }

    fun addBucket() {
        titleDialog(R.string.user_name.getString(), "") {
            if (it.isNotEmpty()) viewModel.addBucket(it)
            else R.string.enter.getString().toast()
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
        refreshSelectGallery(gallery)
    }

    private suspend fun refreshSelectGallery(
        gallery: Gallery,
        itemState: ItemState = ItemState.ALL_CHANGED,
        doAni: Boolean = true
    ): Unit = withMainContext {
        binding.apply {
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
    }

    private fun loadGalleyUri(uri: Uri?) {
        Image.load(uri)
            .with(this@GalleryActivity)
            .error(com.protone.component.R.drawable.ic_baseline_image_24_white)
            .transition(Transition.CrossFade)
            .into(binding.galleryAction)
    }

    private fun getReveal(): Animator? {
        val mX = binding.galleryDetail.measuredWidth / 2
        val mY = binding.galleryDetail.measuredHeight / 2
        val radius = hypot(mX.toDouble(), mY.toDouble()).toFloat()
        return ViewAnimationUtils.createCircularReveal(
            binding.galleryDetail, mX,
            mY, 0f, radius
        ).also { it.duration = 500L }
    }

    private fun quiteSelectMode() {
        viewModel.quiteSelect()
        onSelectMode = false
    }

    private fun getBucketAdapter() = binding.galleryBucket.adapter as GalleryBucketAdapter

    private fun getGalleryData() = ((binding.galleryPager.adapter as MyFragmentStateAdapter)
        .getFragment(viewModel.rightMailer) as GalleryListFragment?)
        ?.getGalleryData()

    override fun getSwapAnim(): Pair<Int, Int>? {
        if (!binding.galleryChooseConfirm.isGone) return null
        return Pair(
            com.protone.component.R.anim.card_in_rtl,
            com.protone.component.R.anim.card_out_rtl
        )
    }

    override fun onBackPressed() {
        if (!doOnBackPressed()) {
            if (onSelectMode) {
                quiteSelectMode()
                return
            }
        } else return
        super.onBackPressed()
    }

    override fun popDelete() {
        tryDelete(viewModel.chooseData) {}
    }

    override fun popMoveTo() {
        viewModel.chooseData.let {
            if (it.size <= 0) return
            moveTo(binding.galleryActionMenu, it[0].isVideo, it) { _, _ -> }
        }
    }

    override fun popRename() {
        viewModel.chooseData.let {
            if (it.size <= 0) return
            tryRename(it)
        }
    }

    override fun popSelectAll() {
        viewModel.selectAll()
    }

    override fun popSetCate() {
        viewModel.chooseData.let { list ->
            if (list.size <= 0) return
            addCate(list)
        }
    }

    override fun popIntoBox() {
        IntentDataHolder.put(
            if (viewModel.chooseData.isEmpty()) getGalleryData() ?: viewModel.chooseData
            else viewModel.chooseData
        )
        startActivity(PictureBoxActivity::class.intent)
    }

}