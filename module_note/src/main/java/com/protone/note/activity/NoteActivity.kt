package com.protone.note.activity

import android.animation.ValueAnimator
import android.transition.TransitionManager
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.protone.common.R
import com.protone.common.baseType.getString
import com.protone.common.baseType.toast
import com.protone.common.context.intent
import com.protone.common.context.putExtras
import com.protone.common.context.root
import com.protone.component.database.MediaAction
import com.protone.common.entity.Note
import com.protone.common.entity.NoteDir
import com.protone.common.utils.RouterPath
import com.protone.component.BaseActivity
import com.protone.component.view.dialog.titleDialog
import com.protone.note.adapter.NoteListListAdapter
import com.protone.note.adapter.NoteTypeListAdapter
import com.protone.note.databinding.NoteActivityBinding
import com.protone.note.viewModel.NoteViewModel
import com.protone.note.viewModel.NoteEditViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs

@Route(path = RouterPath.NoteRouterPath.Main)
class NoteActivity :
    BaseActivity<NoteActivityBinding, NoteViewModel, NoteViewModel.NoteViewEvent>(true) {
    override val viewModel: NoteViewModel by viewModels()

    override fun createView(): NoteActivityBinding {
        return NoteActivityBinding.inflate(layoutInflater, root, false).apply {
            root.fitStatuesBar()
            activity = this@NoteActivity
        }
    }

    override suspend fun NoteViewModel.init() {
        initList()

        refreshList()

        collectNoteEvent {
            when (it) {
                is MediaAction.NoteDataAction.OnNoteDeleted -> deleteNoteCache(it.note)
                else -> Unit
            }
        }

        watchNotes {
            getNoteListAdapter()?.submitList(it)
        }

        onViewEvent {
            when (it) {
                NoteViewModel.NoteViewEvent.RefreshList -> refreshList()
                NoteViewModel.NoteViewEvent.AddBucket -> addBucket()
                NoteViewModel.NoteViewEvent.Refresh -> refresh()
                NoteViewModel.NoteViewEvent.HandleBucketEvent -> handleBucketEvent()
            }
        }
    }

    private fun addBucket() {
        titleDialog(getString(R.string.add_dir), "") { re ->
            if (re.isNotEmpty()) {
                launch {
                    viewModel.insertNoteDir(re, "").let { pair ->
                        if (pair.first) {
                            getNoteTypeAdapter()?.insertNoteDir(pair.second)
                        } else {
                            R.string.failed_msg.getString().toast()
                        }
                    }
                }
            } else {
                R.string.enter.getString().toast()
            }
        }
    }

    private fun refresh() {
        handleBucketEvent()
        TransitionManager.beginDelayedTransition(binding.root as ViewGroup)
        sendViewEvent(NoteViewModel.NoteViewEvent.RefreshList)
    }

    private fun handleBucketEvent() {
        val progress = binding.noteContainer.progress
        ValueAnimator.ofFloat(progress, abs(progress - 1f)).apply {
            addUpdateListener {
                binding.noteContainer.progress = (it.animatedValue as Float)
            }
        }.start()
    }

    private suspend fun refreshList() {
        refreshNoteList(viewModel.getNoteList(NoteDir(R.string.all.getString(), null)))
        refreshNoteType(viewModel.queryAllNoteType())
    }

    private fun initList() {
        binding.apply {
            noteList.also {
                it.layoutManager = LinearLayoutManager(this@NoteActivity)
                it.adapter = NoteListListAdapter(this@NoteActivity) {
                    onNote {
                        startActivity(NoteViewActivity::class.intent.putExtras {
                            putString(RouterPath.NoteRouterPath.NoteViewWire.NOTE_NAME, it)
                        })
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
                    addNote {
                        startActivity(NoteEditActivity::class.intent.putExtras {
                            putString(NoteEditViewModel.NOTE_DIR, it)
                        })
                    }
                    onTypeSelected {
                        launch {
                            refreshNoteList(viewModel.getNoteList(it))
                        }
                    }
                    deleteNoteDir {
                        viewModel.deleteNoteDir(it)
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
}