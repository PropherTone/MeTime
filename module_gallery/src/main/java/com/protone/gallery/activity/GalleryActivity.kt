package com.protone.gallery.activity

import android.animation.ValueAnimator
import android.content.Intent
import androidx.activity.viewModels
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.whenCreated
import androidx.lifecycle.withCreated
import androidx.lifecycle.withStarted
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.android.material.tabs.TabLayoutMediator
import com.protone.common.R
import com.protone.common.baseType.bufferCollect
import com.protone.common.baseType.launchDefault
import com.protone.common.context.root
import com.protone.common.utils.ALL_GALLERY
import com.protone.common.utils.RouterPath
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryMainWire.CHOOSE_MEDIA
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryMainWire.CHOOSE_PHOTO
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryMainWire.CHOOSE_VIDEO
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryMainWire.GALLERY_DATA
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryMainWire.URI
import com.protone.common.utils.displayUtils.imageLoader.Image
import com.protone.common.utils.json.toJson
import com.protone.common.utils.json.toUriJson
import com.protone.component.BaseMediaActivity
import com.protone.component.BaseViewModel
import com.protone.component.database.userConfig
import com.protone.gallery.adapter.GalleryBucketAdapter
import com.protone.gallery.adapter.GalleryListAdapter
import com.protone.gallery.adapter.MyFragmentStateAdapter
import com.protone.gallery.component.GalleryBucketItemDecoration
import com.protone.gallery.databinding.GalleryActivityBinding
import com.protone.gallery.fragment.GalleryListFragment
import com.protone.gallery.viewModel.GalleryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlin.math.abs

@Route(path = RouterPath.GalleryRouterPath.Main)
class GalleryActivity :
    BaseMediaActivity<GalleryActivityBinding, GalleryViewModel, BaseViewModel.ViewEvent>(false) {
    override val viewModel: GalleryViewModel by viewModels()

    private var isInit = false

    override fun createView(): GalleryActivityBinding {
        return GalleryActivityBinding.inflate(layoutInflater, root, false).apply {
            activity = this@GalleryActivity
            root.fitStatuesBar()
        }
    }

    override suspend fun GalleryViewModel.init() {
        val chooseType =
            intent.getStringExtra(RouterPath.GalleryRouterPath.GalleryMainWire.CHOOSE_MODE) ?: ""

        if (chooseType.isNotEmpty()) {
            binding.galleryActionMenu.isVisible = false
            binding.galleryChooseConfirm.isGone = chooseType.isEmpty()
            binding.galleryChooseConfirm.setOnClickListener { confirm() }
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
                    is GalleryViewModel.GalleryEvent.OnNewGallery -> {
                        getBucketAdapter().insertBucket(it.gallery)
                    }
                    is GalleryViewModel.GalleryEvent.OnGalleryRemoved -> {
                        getBucketAdapter().deleteBucket(it.gallery)
                    }
                    is GalleryViewModel.GalleryEvent.OnGalleryUpdated -> {
                        viewModel.run {
                            getBucketAdapter().apply {
                                refreshBucket(it.gallery)
                                getBucket(ALL_GALLERY)?.let { gallery -> refreshBucket(gallery) }
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
                    viewModel.onGallerySelected(gallery)
                    Image.load(gallery.uri).with(this@GalleryActivity).into(binding.galleryAction)
                }
                deleteGalleryBucket {

                }
            }
            addItemDecoration(
                GalleryBucketItemDecoration(resources.getDimension(R.dimen.icon_padding).toInt())
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
                        it.connect(generateMailer(isVideo).onStart {
                            getBucket(ALL_GALLERY)?.let { viewModel.onGallerySelected(it) }
                            initializeSize++
                            if (initializeSize == fs.size) {
                                isInit = true
                            }
                        }, chooseData)
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
                binding.galleryTab.apply { addOnTabSelectedListener(viewModel) },
                binding.galleryPager
            ) { tab, position -> tab.setText(tabList[position]) }.attach()
        }
    }

    private fun confirm() {
        viewModel.chooseData.let { list ->
            if (list.size <= 0) return
            setResult(
                RESULT_OK,
                Intent().putExtra(URI, list[0].uri.toUriJson())
                    .putExtra(GALLERY_DATA, list[0].toJson())
            )
        }
        finish()
    }

    fun showBucket() {
        val progress = binding.motionRoot.progress
        ValueAnimator.ofFloat(progress, abs(progress - 1f)).apply {
            addUpdateListener {
                binding.motionRoot.progress = (it.animatedValue as Float)
            }
        }.start()
        launch {
            viewModel.sendListEvent(GalleryViewModel.GalleryListEvent.OnDrawerEvent(progress != 1f))
        }
    }

    fun showPop() {
        showPop(binding.galleryActionMenu, viewModel.chooseData.size <= 0)
    }

    private fun getBucketAdapter() = binding.galleryBucket.adapter as GalleryBucketAdapter

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

    }

}