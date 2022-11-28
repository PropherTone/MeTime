package com.protone.gallery.activity

import android.content.Intent
import androidx.activity.viewModels
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.android.material.tabs.TabLayoutMediator
import com.protone.common.R
import com.protone.common.context.root
import com.protone.component.database.userConfig
import com.protone.common.utils.RouterPath
import com.protone.common.utils.json.toJson
import com.protone.common.utils.json.toUriJson
import com.protone.component.BaseMediaActivity
import com.protone.component.BaseViewModel
import com.protone.gallery.adapter.MyFragmentStateAdapter
import com.protone.gallery.databinding.GalleryActivityBinding
import com.protone.gallery.fragment.GalleryFragment
import com.protone.gallery.viewModel.GalleryViewModel
import com.protone.gallery.viewModel.GalleryViewModel.Companion.CHOOSE_MEDIA
import com.protone.gallery.viewModel.GalleryViewModel.Companion.CHOOSE_PHOTO
import com.protone.gallery.viewModel.GalleryViewModel.Companion.CHOOSE_VIDEO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Route(path = RouterPath.GalleryRouterPath.Main)
class GalleryActivity :
    BaseMediaActivity<GalleryActivityBinding, GalleryViewModel, BaseViewModel.ViewEvent>(false) {
    override val viewModel: GalleryViewModel by viewModels()

    @JvmField
    @Autowired(name = RouterPath.GalleryRouterPath.GalleryMainWire.CHOOSE_MODE)
    var chooseType: String? = null

    override fun createView(): GalleryActivityBinding {
        return GalleryActivityBinding.inflate(layoutInflater, root, false).apply {
            activity = this@GalleryActivity
            root.fitStatuesBar()
            initPop()
        }
    }

    override suspend fun GalleryViewModel.init() {
        val chooseType = this@GalleryActivity.chooseType ?: ""

        if (chooseType.isNotEmpty()) {
            binding.galleryActionMenu.isVisible = false
            binding.galleryChooseConfirm.isGone = chooseType.isEmpty()
            binding.galleryChooseConfirm.setOnClickListener { confirm() }
        }
        initPager(chooseType)
    }

    private suspend fun GalleryViewModel.initPager(
        chooseType: String = "",
    ) = withContext(Dispatchers.Main) {
        val combine = userConfig.combineGallery || chooseType == CHOOSE_MEDIA
        binding.galleryPager.adapter = MyFragmentStateAdapter(
            this@GalleryActivity,
            mutableListOf<Fragment>().also { fs ->
                val lock = userConfig.lockGallery.isNotEmpty()
                when (chooseType) {
                    CHOOSE_PHOTO ->
                        fs.add(GalleryFragment(false, lock, false) { f -> setMailer(frag1 = f) })
                    CHOOSE_VIDEO ->
                        fs.add(GalleryFragment(true, lock, false) { f -> setMailer(frag2 = f) })
                    else -> {
                        fs.add(GalleryFragment(false, lock, combine) { f -> setMailer(frag1 = f) })
                        if (!combine) fs.add(GalleryFragment(
                            true,
                            lock,
                            false
                        ) { f -> setMailer(frag2 = f) })
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
        viewModel.chooseData?.let { list ->
            if (list.size <= 0) return
            setResult(
                RESULT_OK,
                Intent().putExtra(GalleryViewModel.URI, list[0].uri.toUriJson())
                    .putExtra(GalleryViewModel.Gallery_DATA, list[0].toJson())
            )
        }
        finish()
    }

    fun showPop() {
        showPop(binding.galleryActionMenu, (viewModel.chooseData?.size ?: 0) <= 0)
    }

    override fun popDelete() {
        viewModel.chooseData?.let {
            tryDelete(it) {}
        }
    }

    override fun popMoveTo() {
        viewModel.chooseData?.let {
            if (it.size <= 0) return
            moveTo(binding.galleryBar, it[0].isVideo, it) { target, list ->
                viewModel.addBucket(target, list)
            }
        }
    }

    override fun popRename() {
        viewModel.chooseData?.let {
            if (it.size <= 0) return
            tryRename(it)
        }
    }

    override fun popSelectAll() {
        viewModel.selectAll()
    }

    override fun popSetCate() {
        viewModel.chooseData?.let { list ->
            if (list.size <= 0) return
            addCate(list)
        }
    }

    override fun popIntoBox() {
        viewModel.intoBox()
    }

}