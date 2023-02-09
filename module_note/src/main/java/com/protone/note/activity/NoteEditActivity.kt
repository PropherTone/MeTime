package com.protone.note.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Animatable
import android.net.Uri
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.protone.common.baseType.*
import com.protone.common.context.isKeyBroadShow
import com.protone.common.context.marginBottom
import com.protone.common.context.root
import com.protone.common.context.setSoftInputStatusListener
import com.protone.common.entity.*
import com.protone.common.utils.RouterPath
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryMainWire.CHOOSE_PHOTO
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryMainWire.CHOOSE_VIDEO
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryMainWire.GALLERY_DATA
import com.protone.common.utils.RouterPath.MusicRouterPath.PickPostcard.PICK_MUSIC
import com.protone.common.utils.RouterPath.MusicRouterPath.PickPostcard.pickMusicPostcard
import com.protone.common.utils.RouterPath.NoteRouterPath.NoteEditWire.CONTENT_TITLE
import com.protone.common.utils.RouterPath.NoteRouterPath.NoteEditWire.NOTE
import com.protone.common.utils.RouterPath.NoteRouterPath.NoteEditWire.NOTE_DIR
import com.protone.common.utils.displayUtils.AnimationHelper
import com.protone.common.utils.json.listToJson
import com.protone.common.utils.json.toEntity
import com.protone.common.utils.json.toJson
import com.protone.common.utils.spans.ISpanForUse
import com.protone.common.utils.spans.SpanStates
import com.protone.component.R
import com.protone.component.activity.BaseMsgActivity
import com.protone.component.toGallery
import com.protone.component.view.customView.richText.RichNoteImageLoader
import com.protone.component.view.customView.richText.RichNoteView
import com.protone.component.view.dialog.imageListDialog
import com.protone.component.view.dialog.titleDialog
import com.protone.component.view.popWindows.ColorfulPopWindow
import com.protone.note.databinding.NoteEditActivityBinding
import com.protone.note.viewModel.NoteEditViewModel

@Route(path = RouterPath.NoteRouterPath.Edit)
class NoteEditActivity :
    BaseMsgActivity<NoteEditActivityBinding, NoteEditViewModel, NoteEditViewModel.NoteEvent>(),
    ISpanForUse {
    override val viewModel: NoteEditViewModel by viewModels()

    private var popWindow: ColorfulPopWindow? = null
    private fun getPopWindow(): ColorfulPopWindow? {
        return if (popWindow != null) {
            if (popWindow?.isShowing == true) {
                popWindow?.dismiss()
            }
            popWindow = null
            popWindow
        } else ColorfulPopWindow(this).also {
            popWindow = it
            it.setOnDismissListener { popWindow = null }
        }
    }

    private var title: String
        set(value) {
            binding.noteEditTitle.setText(value)
        }
        get() = binding.noteEditTitle.text.toString()

    @SuppressLint("ClickableViewAccessibility")
    override fun createView(): NoteEditActivityBinding {
        return NoteEditActivityBinding.inflate(layoutInflater, root, false).apply {
            activity = this@NoteEditActivity
            noteEditToolbar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
                toolbar.progress =
                    -verticalOffset / appBarLayout.totalScrollRange.toFloat()
            }
            setSoftInputStatusListener { height, isShow ->
                popWindow?.dismiss()
                if (isShow) {
                    root.marginBottom(height)
                } else {
                    root.marginBottom(0)
                }
            }
            noteEditRichNote.isEditable = true
            noteEditRichNote.setImageLoader(
                RichNoteImageLoader(Glide.with(this@NoteEditActivity).asDrawable())
            )
            noteEditTool.setOnTouchListener { _, _ ->
                getPopWindow()?.dismiss()
                false
            }
        }
    }

    override suspend fun NoteEditViewModel.init() {
        binding.noteEditRichNote.setRichList(listOf(RichNoteStates("")))
        val contentTitle = intent?.extras?.getString(CONTENT_TITLE)
        val noteName = intent?.extras?.getString(NOTE)
        if (contentTitle != null) {
            title = contentTitle
            initEditor(
                1,
                mutableListOf(
                    RichNoteSer(
                        try {
                            getGainData<String>()
                        } catch (e: Exception) {
                            null
                        } ?: R.string.none.getString(),
                        arrayListOf<SpanStates>().listToJson(SpanStates::class.java)
                    ).toJson()
                ).listToJson(String::class.java)
            )
        } else {
            noteByName = noteName?.let {
                getNoteByName(it)?.let { note ->
                    if (checkNoteCover(note.imagePath ?: "")) {
                        setNoteIcon(note.imagePath)
                    } else {
                        setNoteIcon(iconUri)
                    }
                    title = note.title
                    initEditor(note.richCode, note.text)
                    note
                }
            }
        }

        onViewEvent {
            when (it) {
                NoteEditViewModel.NoteEvent.Confirm -> confirm()
                NoteEditViewModel.NoteEvent.PickIcon -> pickIcon()
                NoteEditViewModel.NoteEvent.PickImage -> pickImage()
                NoteEditViewModel.NoteEvent.PickVideo -> pickVideo()
                NoteEditViewModel.NoteEvent.PickMusic -> pickMusic()
            }
        }
    }

    override suspend fun doResume() {
        binding.root.marginBottom(0)
        isKeyBroadShow = false
    }

    private suspend fun NoteEditViewModel.pickIcon() {
        startGalleryPick(true)?.let { re ->
            if (noteByName != null) {
                noteByName?.imagePath?.deleteFile()
            }
            iconUri = re.uri
            setNoteIcon(re.uri)
        }
    }

    private suspend fun pickVideo() {
        startGalleryPick(false)?.let { re ->
            insertVideo(re.uri)
        }
    }

    private suspend fun NoteEditViewModel.pickMusic() {
        startActivityForResult(RouterPath.MusicRouterPath.Pick) {
            pickMusicPostcard(PICK_MUSIC)
        }?.also { re ->
            re.data?.let { uri ->
                if (allNote == null) allNote = getAllNote()
                insertMusic(uri, allNote!!, getMusicTitle(uri))
            }
        }
    }

    private suspend fun NoteEditViewModel.pickImage() {
        startGalleryPick(true)?.let { re ->
            insertImage(re)
            medias.add(re)
        }
    }

    private suspend fun NoteEditViewModel.confirm() {
        if (title.isEmpty()) {
            R.string.enter_title.getString().toast()
            return
        }
        showProgress(true)
        val checkedTitle = checkNoteTitle(title)
        val indexedRichNote = binding.noteEditRichNote.indexRichNote(checkedTitle) {
            if (it.size <= 0) return@indexRichNote true
            return@indexRichNote imageListDialog(it)
        }
        val note = Note(
            checkedTitle,
            indexedRichNote.second,
            null,
            System.currentTimeMillis(),
            indexedRichNote.first
        )
        if (onEdit) {
            if (intent?.extras?.getString(NOTE) == null) {
                setResult(RESULT_CANCELED)
                finish()
                return
            }
            val inNote = noteByName
            if (inNote == null) {
                setResult(RESULT_CANCELED)
                finish()
                return
            }
            copyNote(
                inNote,
                note,
                binding.noteEditIcon.measuredWidth,
                binding.noteEditIcon.measuredHeight
            )
            val re = updateNote(inNote)
            if (re == -1) {
                insertNote(
                    inNote,
                    intent?.extras?.getString(NOTE_DIR)
                ).let { result ->
                    if (result) {
                        setResult(
                            RESULT_OK,
                            Intent().putExtra(
                                RouterPath.NoteRouterPath.NoteViewWire.NOTE_NAME,
                                inNote.title
                            )
                        )
                        finish()
                    } else R.string.failed_msg.getString().toast()
                }
            } else {
                showProgress(false) {
                    setResult(RESULT_OK)
                    finish()
                }
            }
        } else if (
            insertNote(
                note.apply {
                    imagePath = saveIcon(
                        checkedTitle,
                        binding.noteEditIcon.measuredWidth,
                        binding.noteEditIcon.measuredHeight
                    )
                },
                intent?.extras?.getString(NOTE_DIR)
            )
        ) {
            showProgress(false) {
                finish()
            }
        } else R.string.failed_msg.getString().toast()
    }

    private suspend fun startGalleryPick(isPhoto: Boolean) =
        toGallery(if (isPhoto) CHOOSE_PHOTO else CHOOSE_VIDEO)?.let { re ->
            re.getStringExtra(GALLERY_DATA)?.toEntity(GalleryMedia::class.java)
        }

    private suspend fun initEditor(richCode: Int, text: String) = withMainContext {
        binding.noteEditRichNote.apply {
            setRichList(richCode, text)
            iRichListener = object : RichNoteView.IRichListenerImp() {
                override fun onContentGainedFocus() {
                    binding.noteEditToolbar.setExpanded(false, false)
                }
            }
        }
    }

    private suspend fun setNoteIcon(uri: Uri?) = withMainContext {
        Glide.with(this@NoteEditActivity).load(uri).into(binding.noteEditIcon)
    }

    private suspend fun setNoteIcon(path: String?) = withMainContext {
        Glide.with(this@NoteEditActivity).load(path).into(binding.noteEditIcon)
    }

    private suspend fun showProgress(isShow: Boolean, doOnEnd: (() -> Unit)? = null) =
        withMainContext {
            binding.noteEditProgress.apply {
                drawable?.let {
                    when (it) {
                        is Animatable ->
                            if (isShow) it.start().also { isVisible = true }
                            else it.stop().also { changeIconAni(binding.noteEditProgress, doOnEnd) }
                    }
                }
            }
        }

    private suspend fun changeIconAni(view: ImageView, doOnEnd: (() -> Unit)? = null) =
        withMainContext {
            AnimationHelper.apply {
                animatorSet(scaleX(view, 0f), scaleY(view, 0f), doOnEnd = {
                    view.setImageDrawable(R.drawable.ic_baseline_check_24.getDrawable())
                    animatorSet(scaleX(view, 1f), scaleY(view, 1f), play = true, doOnEnd = {
                        alpha(view, 0f, play = true, doOnEnd = {
                            view.isVisible = false
                            doOnEnd?.invoke()
                        })
                    })
                }, play = true)
            }
        }

    override suspend fun insertImage(media: GalleryMedia) =
        binding.noteEditRichNote.insertImage(
            RichPhotoStates(
                media.uri, media.name, null,
                media.date.toDateString().toString()
            ), media.uri.toBitmap()
        )

    override fun insertVideo(uri: Uri) {
        binding.noteEditRichNote.insertVideo(RichVideoStates(uri, null, name = ""))
    }

    override fun insertMusic(uri: Uri, list: MutableList<String>, title: String) {
        if (list.isEmpty()) {
            binding.noteEditRichNote.insertMusic(RichMusicStates(uri, null, title))
            return
        }
        getPopWindow()?.startListPopup(R.string.pick_note.getString(), binding.noteEditTool, list) {
            getPopWindow()?.dismiss()
            binding.noteEditRichNote.insertMusic(RichMusicStates(uri, it, title))
        }
    }

    override fun setBold() = binding.noteEditRichNote.setBold()

    override fun setItalic() = binding.noteEditRichNote.setItalic()

    override fun setSize() {
        binding.run {
            getPopWindow()?.startNumberPickerPopup(
                noteEditTool,
                noteEditRichNote.getSelectionTextSize()
            ) { noteEditRichNote.setSize(it) }
        }
    }

    override fun setUnderlined() = binding.noteEditRichNote.setUnderlined()

    override fun setStrikethrough() = binding.noteEditRichNote.setStrikethrough()

    override fun setURL(url: String) {
        this.titleDialog(R.string.enter_url.getString(), "") {
            binding.noteEditRichNote.setURL(it)
        }
    }

    override fun setSubscript() = binding.noteEditRichNote.setSubscript()

    override fun setSuperscript() = binding.noteEditRichNote.setSuperscript()

    override fun setBullet() {
        getPopWindow()?.startBulletSpanSettingPop(binding.noteEditTool) { gapWidth, color, radius ->
            binding.noteEditRichNote.setBullet(gapWidth, color, radius)
        }
    }

    override fun setQuote() {
        getPopWindow()?.startQuoteSpanSettingPop(binding.noteEditTool) { color, stripeWidth, gapWidth ->
            binding.noteEditRichNote.setQuote(color, stripeWidth, gapWidth)
        }
    }

    override fun setParagraph() {
        binding.noteEditRichNote.apply {
            getPopWindow()?.startParagraphSpanSettingPop(
                binding.noteEditTool,
                getSelectionTextParagraphSpan()
            ) { alignment ->
                setParagraph(alignment)
            }
        }
    }

    override fun setColor(isBackGround: Boolean) {
        getPopWindow()?.startColorPickerPopup(binding.noteEditTool) {
            if (isBackGround) {
                binding.noteEditRichNote.setBackColor(it.toHexColor())
            } else {
                binding.noteEditRichNote.setColor(it.toHexColor())
            }
        }
    }
}