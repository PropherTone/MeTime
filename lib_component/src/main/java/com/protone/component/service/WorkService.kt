package com.protone.component.service

import android.content.BroadcastReceiver
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import com.protone.common.baseType.*
import com.protone.common.context.workIntentFilter
import com.protone.common.entity.GalleryMedia
import com.protone.common.entity.Music
import com.protone.common.media.*
import com.protone.common.utils.ActiveTimer
import com.protone.common.utils.TAG
import com.protone.component.broadcast.MediaContentObserver
import com.protone.component.broadcast.WorkReceiver
import com.protone.component.broadcast.workLocalBroadCast
import com.protone.component.database.dao.DatabaseBridge
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlin.coroutines.CoroutineContext

class WorkService : LifecycleService() {

    companion object {
        private const val UPDATE_MUSIC = 1
        private const val UPDATE_GALLERY = 2

        private const val CALL_MUSIC_UPDATE = "MUSIC"
        private const val CALL_GALLERY_UPDATE = "GALLERY"
    }

    private val jobStressChannel = Channel<String>(onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val activeTimer = ActiveTimer(this, 2048L).apply {
        addFunction(UPDATE_MUSIC) { jobStressChannel.trySend(CALL_MUSIC_UPDATE) }
        addFunction(UPDATE_GALLERY) { jobStressChannel.trySend(CALL_GALLERY_UPDATE) }
    }

    private val workReceiver: BroadcastReceiver = object : WorkReceiver() {

        override fun updateMusic(data: Uri?) {
            if (data != null) {
                this@WorkService.updateMusic(data)
            } else {
                activeTimer.active(UPDATE_MUSIC)
            }
        }

        override fun updateGallery(data: Uri?) {
            if (data != null) {
                this@WorkService.updateGallery(data)
            } else {
                activeTimer.active(UPDATE_GALLERY)
            }
        }
    }

    private val mediaContentObserver = MediaContentObserver(Handler(Looper.getMainLooper()))

    override fun onCreate() {
        super.onCreate()
        registerBroadcast()
        collectJobEvent()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        activeTimer.destroy()
        cancel()
        workLocalBroadCast.unregisterReceiver(workReceiver)
        contentResolver.unregisterContentObserver(mediaContentObserver)
    }

    private fun collectJobEvent() {
        launchDefault {
            jobStressChannel.receiveAsFlow().collectLatest {
                when (it) {
                    CALL_GALLERY_UPDATE -> this.coroutineContext.updateGallery()
                    CALL_MUSIC_UPDATE -> updateMusic()
                }
            }
        }
    }

    private fun registerBroadcast() {
        contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            mediaContentObserver
        )
        contentResolver.registerContentObserver(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            true,
            mediaContentObserver
        )
        contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            mediaContentObserver
        )
        workLocalBroadCast.registerReceiver(workReceiver, workIntentFilter)
    }

    private fun updateMusic(uri: Uri) {
        launchDefault {
            scanAudioWithUri(uri) {
                DatabaseBridge.instance.musicDAOBridge.insertMusicCheck(it)
            }
            makeToast("音乐更新完毕")
        }
    }

    private fun updateGallery(uri: Uri) {
        launchDefault {
            if (!isUriExist(uri)) {
                DatabaseBridge.instance.galleryDAOBridge.deleteSignedMediaByUri(uri)
                Log.d(TAG, "updateGallery(uri: Uri):!isUriExist 相册更新完毕")
                makeToast("相册更新完毕")
            } else scanGalleryWithUri(uri) {
                val checkedMedia = DatabaseBridge
                    .instance
                    .galleryDAOBridge
                    .insertSignedMediaChecked(it)
                if (checkedMedia != null) {
                    Log.d(TAG, "updateGallery(uri: Uri): 相册更新完毕")
                    makeToast("相册更新完毕")
                }
            }
        }
    }

    private suspend fun updateMusic(): Unit = coroutineScope {
        fun sortMusic(allMusic: MutableList<Music>, music: Music): Boolean {
            val index = allMusic.indexOf(music)
            return if (index != -1) {
                allMusic.removeAt(index)
                true
            } else false
        }
        DatabaseBridge.instance.musicDAOBridge.run {
            val allMusic = mutableListOf<Music>()
            launchDefault {
                getAllMusic()?.let { allMusic.addAll(it) }
                flow {
                    scanAudio { _, music ->
                        if (!sortMusic(allMusic, music)) {
                            emit(music)
                        }
                    }
                }.bufferCollect {
                    insertMusic(it)
                }
                deleteMusicMulti(allMusic)
                if (allMusic.size != 0) {
                    makeToast("音乐更新完毕")
                }
            }
        }
    }


    private suspend fun CoroutineContext.updateGallery(): Unit = withContext(this) {
        DatabaseBridge.instance.galleryDAOBridge.run {
            Log.d(TAG, "updateGallery: ")
            val allSignedMedia = getAllSignedMedia() as MutableList?
            val scanPicture = async(Dispatchers.Default) {
                scanPicture { _, _ -> }
            }
            val scanVideo = async(Dispatchers.Default) {
                scanVideo { _, _ -> }
            }

            val medias = scanPicture.await().apply {
                addAll(scanVideo.await())
            }

            val sortMedias = async(Dispatchers.Default) {
                val uncheckedGalleries = getAllGallery() as MutableList<String>
                sortGalleries(uncheckedGalleries)
                uncheckedGalleries.forEach {
                    deleteSignedMediasByGalleryAsync(it)
                }
                allSignedMedia?.filter {
                    uncheckedGalleries.contains(it.bucket) || medias.find { gm -> gm.uri == it.uri } == null
                }?.apply {
                    deleteSignedMediaMultiAsync(this)
                }
            }

            sortMedias.await()?.let { allSignedMedia?.removeAll(it) }

            val updateList = mutableListOf<GalleryMedia>()
            val insertList = mutableListOf<GalleryMedia>()

            medias.forEach { media ->
                if (allSignedMedia?.isNotEmpty() == true) {
                    allSignedMedia.find { it.uri == media.uri }.also {
                        if (it == null) insertList.add(media)
                        else if (it != media) updateList.add(media)
                    }
                } else {
                    insertList.add(media)
                }
            }

            insertSignedMediaMulti(insertList)
            updateSignedMediaMulti(updateList)
        }
    }

    private fun makeToast(msg: String) {
        msg.toast()
    }
}