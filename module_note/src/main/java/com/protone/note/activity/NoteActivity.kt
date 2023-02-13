package com.protone.note.activity

import android.animation.ValueAnimator
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.protone.common.baseType.getString
import com.protone.common.context.intent
import com.protone.common.context.root
import com.protone.common.entity.Note
import com.protone.common.entity.NoteDir
import com.protone.common.utils.RouterPath
import com.protone.common.utils.RouterPath.NoteRouterPath.NoteEditWire.NOTE_DIR
import com.protone.component.R
import com.protone.component.activity.BaseActivity
import com.protone.component.view.dialog.titleDialog
import com.protone.note.adapter.NoteListListAdapter
import com.protone.note.adapter.NoteTypeListAdapter
import com.protone.note.databinding.NoteActivityBinding
import com.protone.note.viewModel.NoteViewModel
import kotlin.math.abs

@Route(path = RouterPath.NoteRouterPath.Main)
class NoteActivity :
    BaseActivity<NoteActivityBinding, NoteViewModel, NoteViewModel.NoteViewEvent>() {

    override val viewModel: NoteViewModel by viewModels()

    internal inner class ViewEventModel {
        fun handleBucketEvent() {
            this@NoteActivity.handleBucketEvent()
        }

        fun finish() {
            this@NoteActivity.finish()
        }

        fun addBucket() {
            this@NoteActivity.addBucket()
        }

        fun refresh() {
            this@NoteActivity.refresh()
        }
    }

    override fun createView(): NoteActivityBinding {
        return NoteActivityBinding.inflate(layoutInflater, root, false).apply {
            root.fitStatuesBar()
            model = ViewEventModel()
        }
    }

    override suspend fun NoteViewModel.init() {
        initList()
        refreshListData()
        collectNoteEvent()
        onViewEvent(this@NoteActivity, this@NoteActivity) {
            when (it) {
                is NoteViewModel.NoteViewEvent.RefreshList -> refreshList(it.notes, it.buckets)
                is NoteViewModel.NoteViewEvent.OnTypeSelected -> refreshNoteList(it.notes)
                is NoteViewModel.NoteViewEvent.OnNotesUpdated ->
                    getNoteListAdapter()?.submitList(it.notes)
                is NoteViewModel.NoteViewEvent.OnBucketInserted ->
                    getNoteTypeAdapter()?.insertNoteDir(it.bucket)
            }
        }
    }

    fun addBucket() {
        titleDialog(getString(R.string.add_dir), "") { re ->
            viewModel.addBucket(re)
        }
    }

    fun refresh() {
        throw RuntimeException("Well done!You let the app crashed!")
    }

    fun handleBucketEvent() {
        val progress = binding.noteContainer.progress
        ValueAnimator.ofFloat(progress, abs(progress - 1f)).apply {
            addUpdateListener {
                binding.noteContainer.progress = (it.animatedValue as Float)
            }
        }.start()
    }

    private fun refreshList(notes: List<Note>, buckets: List<NoteDir>) {
        refreshNoteList(notes)
        refreshNoteType(buckets)
    }

    private fun initList() {
        binding.apply {
            noteList.also {
                it.layoutManager = LinearLayoutManager(this@NoteActivity)
                it.adapter = NoteListListAdapter(
                    this@NoteActivity,
                    Glide.with(this@NoteActivity).asDrawable()
                ) {
                    onNote { note ->
                        startActivity(
                            NoteViewActivity::class.intent
                                .putExtra(RouterPath.NoteRouterPath.NoteViewWire.NOTE_NAME, note)
                        )
                    }
                    onDelete { note ->
                        titleDialog(R.string.delete.getString(), R.string.delete.getString()) {
                            viewModel.deleteNote(note)
                        }
                    }
                }
            }
            noteBucketList.also {
                it.layoutManager = LinearLayoutManager(this@NoteActivity)
                it.adapter = NoteTypeListAdapter(this@NoteActivity) {
                    addNote { note ->
                        startActivity(NoteEditActivity::class.intent.putExtra(NOTE_DIR, note))
                    }
                    onTypeSelected { dir ->
                        viewModel.onTypeSelected(dir)
                    }
                    deleteNoteDir { dir ->
                        viewModel.deleteNoteDir(dir)
                    }
                }
            }
        }
    }

    private fun refreshNoteList(list: List<Note>) {
        getNoteListAdapter()?.submitList(list)
    }

    private fun refreshNoteType(list: List<NoteDir>) {
        getNoteTypeAdapter()?.setNoteTypeList(list)
    }

    private fun getNoteListAdapter(): NoteListListAdapter? {
        return (binding.noteList.adapter as NoteListListAdapter?)
    }

    private fun getNoteTypeAdapter(): NoteTypeListAdapter? {
        return (binding.noteBucketList.adapter as NoteTypeListAdapter?)
    }

    override fun getSwapAnim(): Pair<Int, Int> {
        return Pair(R.anim.card_in_ltr, R.anim.card_out_ltr)
    }
}