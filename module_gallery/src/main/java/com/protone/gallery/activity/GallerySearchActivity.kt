package com.protone.gallery.activity

import android.transition.TransitionManager
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isGone
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.protone.component.R
import com.protone.common.baseType.getString
import com.protone.common.baseType.launchDefault
import com.protone.common.baseType.launchMain
import com.protone.common.baseType.toast
import com.protone.common.context.intent
import com.protone.common.context.linkInput
import com.protone.common.context.root
import com.protone.common.entity.GalleryMedia
import com.protone.common.utils.ALL_GALLERY
import com.protone.common.utils.RouterPath
import com.protone.common.utils.SearchModel
import com.protone.common.utils.json.toJson
import com.protone.component.activity.BaseMediaActivity
import com.protone.component.BaseViewModel
import com.protone.component.toGalleryView
import com.protone.gallery.adapter.GalleryListAdapter
import com.protone.gallery.component.GalleryItemDecoration
import com.protone.gallery.databinding.GallerySearchActivityBinding
import com.protone.gallery.viewModel.GallerySearchViewModel
import kotlinx.coroutines.launch

@Route(path = RouterPath.GalleryRouterPath.Search)
class GallerySearchActivity : BaseMediaActivity<
        GallerySearchActivityBinding,
        GallerySearchViewModel,
        BaseViewModel.ViewEvent>(),
    GalleryListAdapter.OnSelect, GallerySearchViewModel.OnQuery {
    override val viewModel: GallerySearchViewModel by viewModels()

    override fun createView(): GallerySearchActivityBinding {
        return GallerySearchActivityBinding.inflate(layoutInflater, root, false).apply {
            activity = this@GallerySearchActivity
            root.fitStatuesBar()
        }
    }

    override suspend fun GallerySearchViewModel.init() {
        val searchModel = SearchModel(binding.inputSearch) {
            query(getInput())
        }
        onQueryListener = this@GallerySearchActivity
        val gainListData = getGainListData<GalleryMedia>()
        if (gainListData == null) {
            R.string.none.getString().toast()
        } else {
            data.addAll(gainListData)
            initList()
        }

        onLifecycleEvent {
            onFinish {
                searchModel.destroy()
            }
        }
    }

    private fun newAdapter(list: MutableList<GalleryMedia>) = GalleryListAdapter(
        this@GallerySearchActivity, Glide.with(this).asDrawable(), true, itemCount = 0
    ).also {
        it.setData(list)
        it.multiChoose = true
        it.setNewSelectList(viewModel.selectList)
        it.setOnSelectListener(this@GallerySearchActivity)
    }

    private fun initList() {
        binding.apply {
            linkInput(scroll, inputSearch)
            fun RecyclerView.initRecyclerView() {
                apply {
                    layoutManager = GridLayoutManager(context, 4)
                    adapter = newAdapter(mutableListOf())
                    addItemDecoration(GalleryItemDecoration(paddingEnd))
                    linkInput(this, inputSearch)
                }
            }
            resultGalleries.initRecyclerView()
            resultCato.initRecyclerView()
            resultNotes.initRecyclerView()
            filterGallery.setOnClickListener {
                listGone(binding.resultGalleries)
            }
            filterCato.setOnClickListener {
                listGone(binding.resultCato)
            }
            filterNote.setOnClickListener {
                listGone(binding.resultNotes)
            }
        }
    }

    private fun listGone(view: View) {
        TransitionManager.beginDelayedTransition(binding.resultContainer)
        view.isGone = !view.isGone
    }

    fun showPop() {
        showPop(binding.actionMenu)
    }

    override fun select(media: GalleryMedia) {
        binding.apply {
            (resultGalleries.adapter as GalleryListAdapter).noticeSelectChange(media)
            (resultCato.adapter as GalleryListAdapter).noticeSelectChange(media)
            (resultNotes.adapter as GalleryListAdapter).noticeSelectChange(media)
        }
    }

    override fun select(medias: List<GalleryMedia>) {
        if (medias.isEmpty()) {
            binding.apply {
                (resultGalleries.adapter as GalleryListAdapter).exitSelectMod()
                (resultCato.adapter as GalleryListAdapter).exitSelectMod()
                (resultNotes.adapter as GalleryListAdapter).exitSelectMod()
            }
        }
    }

    override fun openView(galleryMedia: GalleryMedia, elementView: View) {
        toGalleryView(
            galleryMedia.toJson(),
            galleryMedia.isVideo,
            false,
            intent.extras?.getString("gallery") ?: ALL_GALLERY
        )
    }

    override fun onItemLongClick() {

    }

    override fun popDelete() {
        tryDelete(viewModel.selectList) {
            binding.apply {
                if (it.size == 1) {
                    (resultGalleries.adapter as GalleryListAdapter).noticeListItemDelete(it[0])
                    (resultCato.adapter as GalleryListAdapter).noticeListItemDelete(it[0])
                    (resultNotes.adapter as GalleryListAdapter).noticeListItemDelete(it[0])
                } else if (it.size > 1) {
                    it.forEach { gm ->
                        (resultGalleries.adapter as GalleryListAdapter).noticeListItemDelete(gm)
                        (resultCato.adapter as GalleryListAdapter).noticeListItemDelete(gm)
                        (resultNotes.adapter as GalleryListAdapter).noticeListItemDelete(gm)
                    }
                }
            }
        }
    }

    override fun popMoveTo() {
        launch {
            moveTo(binding.toolbar, viewModel.selectList) { _, _ -> }
        }
    }

    override fun popRename() {
        tryRename(viewModel.selectList)
    }

    override fun popSetCate() {
        addCate(viewModel.selectList)
    }

    fun popSelectAll() {
        binding.apply {
            if (!resultGalleries.isGone) {
                (resultGalleries.adapter as GalleryListAdapter).selectAll()
            }
            if (!resultCato.isGone) {
                (resultCato.adapter as GalleryListAdapter).selectAll()
            }
            if (!resultNotes.isGone) {
                (resultNotes.adapter as GalleryListAdapter).selectAll()
            }
        }
    }

    fun popIntoBox() {
        launchDefault {
            viewModel.apply {
                putGainIntentData(selectList)
                startActivity(PictureBoxActivity::class.intent)
            }
        }
    }

    override fun onGalleryResult(list: MutableList<GalleryMedia>) {
        if (list.isEmpty()) return
        launchMain {
            if (binding.resultGalleries.isGone) {
                listGone(binding.resultGalleries)
            }
            if (binding.resultGalleries.adapter is GalleryListAdapter) {
                binding.resultGalleries.swapAdapter(newAdapter(list), false)
            }
        }
    }

    override fun onCatoResult(list: MutableList<GalleryMedia>) {
        if (list.isEmpty()) return
        launchMain {
            if (binding.resultCato.isGone) {
                listGone(binding.resultCato)
            }
            if (binding.resultCato.adapter is GalleryListAdapter) {
                binding.resultCato.swapAdapter(newAdapter(list), false)
            }
        }
    }

    override fun onNoteResult(list: MutableList<GalleryMedia>) {
        if (list.isEmpty()) return
        launchMain {
            if (binding.resultNotes.isGone) {
                listGone(binding.resultNotes)
            }
            if (binding.resultNotes.adapter is GalleryListAdapter) {
                binding.resultNotes.swapAdapter(newAdapter(list), false)
            }
        }
    }

}