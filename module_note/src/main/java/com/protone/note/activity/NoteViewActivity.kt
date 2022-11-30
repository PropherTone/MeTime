package com.protone.note.activity

import android.net.Uri
import androidx.activity.viewModels
import com.alibaba.android.arouter.facade.annotation.Route
import com.protone.common.R
import com.protone.common.baseType.getString
import com.protone.common.baseType.toast
import com.protone.common.context.intent
import com.protone.common.context.putExtras
import com.protone.common.context.root
import com.protone.common.entity.Note
import com.protone.common.utils.RouterPath
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryViewWire.galleryViewPostcard
import com.protone.common.utils.displayUtils.imageLoader.Image
import com.protone.common.utils.displayUtils.imageLoader.constant.DiskCacheStrategy
import com.protone.common.utils.json.toJson
import com.protone.component.BaseMusicActivity
import com.protone.component.service.MusicBinder
import com.protone.component.view.customView.richText.RichNoteImageLoader
import com.protone.component.view.customView.richText.RichNoteView
import com.protone.note.databinding.NoteViewActivityBinding
import com.protone.note.viewModel.NoteEditViewModel
import com.protone.note.viewModel.NoteViewViewModel
import kotlinx.coroutines.launch

@Route(path = RouterPath.NoteRouterPath.NoteView)
class NoteViewActivity :
    BaseMusicActivity<NoteViewActivityBinding, NoteViewViewModel, NoteViewViewModel.NoteViewEvent>(
        true
    ) {
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

        intent.extras?.getString(RouterPath.NoteRouterPath.NoteViewWire.NOTE_NAME)?.let {
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
                            startActivity(RouterPath.GalleryRouterPath.GalleryView) {
                                galleryViewPostcard(collect[0].toJson(), isVideo)
                            }
                        } else R.string.none.getString().toast()
                    }
                }

            })
        } else R.string.come_up_unknown_error.getString().toast()
    }

    private suspend fun edit() = viewModel.apply {
        val re = startActivityForResult(
            NoteEditActivity::class.intent.putExtras {
                putString(
                    NoteEditViewModel.NOTE,
                    this@NoteViewActivity.intent.getStringExtra(RouterPath.NoteRouterPath.NoteViewWire.NOTE_NAME)
                )
            }
        )
        if (re == null) {
            R.string.none.getString().toast()
            return@apply
        }
        if (re.resultCode != RESULT_OK) return@apply
        intent.getStringExtra(RouterPath.NoteRouterPath.NoteViewWire.NOTE_NAME)?.let { name ->
            noteQueue.remove(name)
            noteQueue.offer(name)
            initSeen(noteQueue.poll())
        }
    }

    private suspend fun initNote(note: Note, listener: RichNoteView.IRichListener) {
        binding.apply {
            noteEditTitle.text = note.title
            Image.load(note.imagePath)
                .with(this@NoteViewActivity)
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