package com.protone.music.activity

import android.content.Intent
import android.net.Uri
import androidx.activity.viewModels
import com.alibaba.android.arouter.facade.annotation.Route
import com.protone.common.baseType.toast
import com.protone.common.baseType.withDefaultContext
import com.protone.common.baseType.withMainContext
import com.protone.common.context.marginBottom
import com.protone.common.context.root
import com.protone.common.context.setSoftInputStatusListener
import com.protone.common.entity.MusicBucket
import com.protone.common.utils.RouterPath
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryMainWire.CHOOSE_PHOTO
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryMainWire.URI
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryMainWire.galleryMainPostcard
import com.protone.common.utils.displayUtils.imageLoader.Image
import com.protone.common.utils.displayUtils.imageLoader.constant.DiskCacheStrategy
import com.protone.common.utils.json.toUri
import com.protone.component.BaseActivity
import com.protone.music.databinding.AddBucketActivityBinding
import com.protone.music.viewModel.AddBucketViewModel

@Route(path = RouterPath.MusicRouterPath.AddBucket)
class AddBucketActivity : BaseActivity<
        AddBucketActivityBinding,
        AddBucketViewModel,
        AddBucketViewModel.AddBucketEvent>(true) {
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
            Image.load(value).with(this).into(binding.musicBucketIcon)
            field = value
        }

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
                com.protone.common.R.string.come_up_unknown_error.toString().toast()
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
        startActivityForResult(RouterPath.GalleryRouterPath.Main) {
            galleryMainPostcard(CHOOSE_PHOTO)
        }.let { result ->
            uri = result?.getStringExtra(URI)?.toUri()
        }
    }

    private suspend fun cancelAdd() = withDefaultContext {
        setResult(RESULT_CANCELED)
        finish()
    }

    private suspend fun AddBucketViewModel.confirm() = withMainContext {
        var intent: Intent?
        if (editName != null) {
            val re = musicBucket?.let {
                updateMusicBucket(it, name, uri, detail)
            }
            if (re != 0 || re != -1) {
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
        Image.load(musicBucket.icon)
            .with(this@AddBucketActivity)
            .skipMemoryCache()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(binding.musicBucketIcon)
    }
}