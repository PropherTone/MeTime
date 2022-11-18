package com.protone.seenn.activity

import android.net.Uri
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.protone.api.baseType.getString
import com.protone.api.baseType.toast
import com.protone.api.context.intent
import com.protone.api.context.root
import com.protone.api.entity.Note
import com.protone.api.json.toJson
import com.protone.seenn.databinding.NoteViewActivityBinding
import com.protone.seenn.service.MusicBinder
import com.protone.ui.customView.richText.RichNoteImageLoader
import com.protone.ui.customView.richText.RichNoteView
import com.protone.worker.R
import com.protone.worker.viewModel.GalleryViewViewModel
import com.protone.worker.viewModel.NoteEditViewModel
import com.protone.worker.viewModel.NoteViewViewModel
import kotlinx.coroutines.launch

class NoteViewActivity :
    BaseActivity<NoteViewActivityBinding, NoteViewViewModel, NoteViewViewModel.NoteViewEvent>(true) {
    override val viewModel: NoteViewViewModel by viewModels()

    private var binder: MusicBinder? = null

    override fun createView(): NoteViewActivityBinding {
        return NoteViewActivityBinding.inflate(layoutInflater, root, false).apply {
            activity = this@NoteViewActivity
            noteEditRichNote.setImageLoader(RichNoteImageLoader())
        }
    }

    override suspend fun NoteViewViewModel.init() {
        bindMusicService { binder = it }

        intent.getStringExtra(NoteViewViewModel.NOTE_NAME)?.let {
            noteQueue.offer(it)
            initSeen(noteQueue.poll())
        }

        onViewEvent {
            when (it) {
                NoteViewViewModel.NoteViewEvent.Next -> initSeen(viewModel.noteQueue.poll())
                NoteViewViewModel.NoteViewEvent.Edit -> edit()
            }
        }
    }

    override fun finish() {
        if (viewModel.noteQueue.isNotEmpty()) {
            sendViewEvent(NoteViewViewModel.NoteViewEvent.Next)
        } else super.finish()
    }

    private suspend fun NoteViewViewModel.initSeen(noteName: String?) {
        if (noteName == null) return
        val note = getNoteByName(noteName)
        if (note != null) {
            initNote(note, object : RichNoteView.IRichListenerImp() {

                override fun play(uri: Uri, progress: Long) {
                    launch {
                        getMusicByUri(uri)?.let {
                            setMusicDuration(it.duration)
                            binder?.play(it)
                            binder?.setProgress(progress)
                        }
                        binder?.onProgress()?.observe(this@NoteViewActivity) {
                            setMusicProgress(it)
                        }
                    }
                }

                override fun pause() {
                    binder?.pause()
                }

                override fun jumpTo(note: String) {
                    noteQueue.offer(note)
                    sendViewEvent(NoteViewViewModel.NoteViewEvent.Next)
                }

                override fun open(uri: Uri, name: String, isVideo: Boolean) {
                    launch {
                        val collect = filterMedia(uri, isVideo)
                        if (collect != null && collect.size > 0) {
                            startActivity(GalleryViewActivity::class.intent.apply {
                                putExtra(GalleryViewViewModel.MEDIA, collect[0].toJson())
                                putExtra(GalleryViewViewModel.IS_VIDEO, isVideo)
                            })
                        } else R.string.none.getString().toast()
                    }
                }

            })
        } else R.string.come_up_unknown_error.getString().toast()
    }

    private suspend fun edit() = viewModel.apply {
        val re = startActivityForResult(
            NoteEditActivity::class.intent.also { intent ->
                intent.putExtra(
                    NoteEditViewModel.NOTE,
                    this@NoteViewActivity.intent.getStringExtra(NoteViewViewModel.NOTE_NAME)
                )
            }
        )
        if (re == null) {
            R.string.none.getString().toast()
            return@apply
        }
        if (re.resultCode != RESULT_OK) return@apply
        intent.getStringExtra(NoteViewViewModel.NOTE_NAME)?.let { name ->
            noteQueue.remove(name)
            noteQueue.offer(name)
            initSeen(noteQueue.poll())
        }
    }

    private suspend fun initNote(note: Note, listener: RichNoteView.IRichListener) {
        binding.apply {
            noteEditTitle.text = note.title
            Glide.with(this@NoteViewActivity)
                .asDrawable()
                .load(note.imagePath)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(noteEditIcon)
            noteEditRichNote.isEditable = false
            noteEditRichNote.setRichList(note.richCode, note.text)
            noteEditRichNote.iRichListener = listener
        }
    }

    fun setMusicProgress(progress: Long) {
        binding.noteEditRichNote.setMusicProgress(progress)
    }

    fun setMusicDuration(duration: Long) {
        binding.noteEditRichNote.setMusicDuration(duration)
    }
}