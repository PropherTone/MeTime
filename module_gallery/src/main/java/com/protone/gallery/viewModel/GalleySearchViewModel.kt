package com.protone.gallery.viewModel

import androidx.lifecycle.viewModelScope
import com.protone.common.entity.GalleryMedia
import com.protone.component.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class GallerySearchViewModel : BaseViewModel() {

    val data = mutableListOf<GalleryMedia>()

    var onQueryListener: OnQuery? = null

    var selectList: MutableList<GalleryMedia> = mutableListOf()

    fun isVideo(): Boolean {
        return if (data.size > 0) {
            data[0].isVideo
        } else {
            false
        }
    }

    fun query(input: String) {
        if (input.isEmpty()) return
        viewModelScope.launch(Dispatchers.Default) {
            launch(Dispatchers.Default) {
                data.filter {
                    it.name.contains(input, true)
                }.let { nameFilterList ->
                    onQueryListener?.onGalleryResult(nameFilterList as MutableList<GalleryMedia>)
                    cancel()
                }
            }
            launch(Dispatchers.Default) {
                data.filter {
                    it.cate?.any { name -> name.contains(input, true) } == true
                }.let { catoFilterList ->
                    onQueryListener?.onCatoResult(catoFilterList as MutableList<GalleryMedia>)
                    cancel()
                }
            }
            launch(Dispatchers.Default) {
                data.filter {
                    val notesWithGallery = galleryDAO.getNotesWithGallery(it.uri)
                        .map { note -> note.title }
                    notesWithGallery.any { name -> name.contains(input, true) }
                }.let { noteFilterList ->
                    onQueryListener?.onNoteResult(noteFilterList as MutableList<GalleryMedia>)
                    cancel()
                }
            }
        }
    }

    interface OnQuery {
        fun onGalleryResult(list: MutableList<GalleryMedia>)
        fun onCatoResult(list: MutableList<GalleryMedia>)
        fun onNoteResult(list: MutableList<GalleryMedia>)
    }
}