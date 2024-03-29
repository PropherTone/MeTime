package com.protone.music.activity

import android.content.Intent
import android.net.Uri
import androidx.activity.viewModels
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.protone.common.baseType.withDefaultContext
import com.protone.common.baseType.withMainContext
import com.protone.common.context.marginBottom
import com.protone.common.context.root
import com.protone.common.context.setSoftInputStatusListener
import com.protone.common.context.showFailedToast
import com.protone.common.entity.MusicBucket
import com.protone.common.utils.RouterPath
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryMainWire.CHOOSE_PHOTO
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryMainWire.URI
import com.protone.common.utils.json.toUri
import com.protone.component.activity.BaseMsgActivity
import com.protone.component.toGallery
import com.protone.music.databinding.AddBucketActivityBinding
import com.protone.music.viewModel.AddBucketViewModel

@Route(path = RouterPath.MusicRouterPath.AddBucket)
class AddBucketActivity : BaseMsgActivity<
        AddBucketActivityBinding,
        AddBucketViewModel,
        AddBucketViewModel.AddBucketEvent>() {
    override val viewModel: AddBucketViewModel by viewModels()

    private var name: String
        set(value) {
            binding.musicBucketEnterName.setText(value)
        }
        get() = binding.musicBucketEnterName.text.toString()

    private var detail: String
        set(value) {
            binding.musicBucketEnterDetail.setText(value)
        }
        get() = binding.musicBucketEnterDetail.text.toString()

    private var uri: Uri? = null
        set(value) {
            Glide.with(this).load(value).into(binding.musicBucketIcon)
            field = value
        }

    private var onConfirm = false

    override fun createView(): AddBucketActivityBinding {
        return AddBucketActivityBinding.inflate(layoutInflater, root, false).apply {
            root.fitStatuesBarUsePadding()
            activity = this@AddBucketActivity
            setSoftInputStatusListener { i, b ->
                if (b) {
                    root.marginBottom(i)
                } else {
                    root.marginBottom(0)
                }
            }
        }
    }

    override suspend fun AddBucketViewModel.init() {
        editName = intent?.extras?.getString(AddBucketViewModel.BUCKET_NAME)
        editName?.let { eName ->
            musicBucket = getMusicBucketByName(eName)
            if (musicBucket == null) {
                showFailedToast()
                finish()
                return@let
            } else {
                musicBucket?.let { refresh(it) }
            }
        }

        onViewEvent {
            when (it) {
                AddBucketViewModel.AddBucketEvent.ChooseIcon -> chooseIcon()
                AddBucketViewModel.AddBucketEvent.Confirm -> confirm()
                AddBucketViewModel.AddBucketEvent.Cancel -> cancelAdd()
            }
        }
    }

    private suspend fun chooseIcon() = withMainContext {
        toGallery(CHOOSE_PHOTO).let { result ->
            uri = result?.getStringExtra(URI)?.toUri()
        }
    }

    private suspend fun cancelAdd() = withDefaultContext {
        setResult(RESULT_CANCELED)
        finish()
    }

    private suspend fun AddBucketViewModel.confirm() = withMainContext {
        if (onConfirm) return@withMainContext
        onConfirm = true
        var intent: Intent?
        if (editName != null) {
            val re = musicBucket?.let {
                updateMusicBucket(it, name, uri, detail)
            } ?: -1
            if (re != -1) {
                intent = Intent().putExtra(AddBucketViewModel.BUCKET_NAME, name)
                setResult(RESULT_OK, intent)
                finish()
            } else {
                setResult(RESULT_CANCELED)
                finish()
            }
        } else addMusicBucket(name, uri, detail) { re, name ->
            if (re) {
                intent = Intent().putExtra(AddBucketViewModel.BUCKET_NAME, name)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }

    private suspend fun refresh(musicBucket: MusicBucket) = withMainContext {
        this@AddBucketActivity.name = musicBucket.name
        detail = musicBucket.detail.toString()
        Glide.with(this@AddBucketActivity)
            .load(musicBucket.icon)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(binding.musicBucketIcon)
    }
}