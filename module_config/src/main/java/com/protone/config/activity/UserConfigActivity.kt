package com.protone.config.activity

import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.config.databinding.UserConfigActivityBinding
import com.protone.common.R
import com.protone.common.baseType.getString
import com.protone.common.baseType.imageSaveToDisk
import com.protone.common.baseType.toast
import com.protone.common.context.MApplication
import com.protone.common.context.intent
import com.protone.common.context.root
import com.protone.common.entity.GalleryMedia
import com.protone.common.utils.RouterPath
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryMainWire.CHOOSE_PHOTO
import com.protone.common.utils.RouterPath.GalleryRouterPath.GalleryMainWire.GALLERY_DATA
import com.protone.common.utils.json.toEntity
import com.protone.component.activity.BaseMsgActivity
import com.protone.component.database.userConfig
import com.protone.component.databinding.UserConfigItemLayoutBinding
import com.protone.component.toGallery
import com.protone.component.view.customView.blurView.DefaultBlurController
import com.protone.component.view.customView.blurView.DefaultBlurEngine
import com.protone.component.view.dialog.checkListDialog
import com.protone.component.view.dialog.loginDialog
import com.protone.component.view.dialog.regDialog
import com.protone.component.view.dialog.titleDialog
import com.protone.config.viewModel.UserConfigViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Route(path = RouterPath.ConfigRouterPath.UserConfig)
class UserConfigActivity : BaseMsgActivity<
        UserConfigActivityBinding,
        UserConfigViewModel,
        UserConfigViewModel.UserConfigEvent>() {
    override val viewModel: UserConfigViewModel by viewModels()

    override fun createView(): UserConfigActivityBinding {
        return UserConfigActivityBinding.inflate(layoutInflater, root, false).apply {
            activity = this@UserConfigActivity
            blur.initBlurTool(DefaultBlurController(root as ViewGroup, DefaultBlurEngine())).apply {
                setBlurRadius(24f)
            }
        }
    }

    override suspend fun UserConfigViewModel.init() {
        refreshLayout()
        binding.root.viewTreeObserver.addOnPreDrawListener {
            binding.blur.renderFrame()
            true
        }
        onViewEvent {
            when (it) {
                UserConfigViewModel.UserConfigEvent.Login -> startLoginDialog()
                UserConfigViewModel.UserConfigEvent.Icon -> startIconPick()
                UserConfigViewModel.UserConfigEvent.Name -> startNameDialog()
                UserConfigViewModel.UserConfigEvent.PassWord -> startPasswordDialog()
                UserConfigViewModel.UserConfigEvent.Share -> {}
                UserConfigViewModel.UserConfigEvent.Lock -> startLockListPop()
                UserConfigViewModel.UserConfigEvent.Unlock -> startUnlockListPop()
                UserConfigViewModel.UserConfigEvent.Refresh -> refreshLayout()
                UserConfigViewModel.UserConfigEvent.ClearCache -> viewModel.clearCache()
                UserConfigViewModel.UserConfigEvent.Log -> startActivity(LogActivity::class.intent)
                UserConfigViewModel.UserConfigEvent.CombineGallery -> userConfig.combineGallery =
                    true
                UserConfigViewModel.UserConfigEvent.DispatchGallery -> userConfig.combineGallery =
                    false
            }
        }
    }

    private fun clear() {
        binding.userRoot.removeAllViews()
    }

    private fun chooseMode(
        loginMode: UserConfigViewModel.DisplayMode,
        lockMode: UserConfigViewModel.DisplayMode,
        galleryMode: UserConfigViewModel.DisplayMode
    ) {
        val logView = initModeView(R.string.log.getString()) {
            UserConfigViewModel.UserConfigEvent.Log
        }
        if (loginMode == UserConfigViewModel.DisplayMode.UnRegis) {
            UserConfigItemLayoutBinding.inflate(layoutInflater, root, false)
                .apply {
                    itemName.text = R.string.login.getString()
                    itemName.setOnClickListener { startLoginDialog() }
                }.root.run {
                    layoutParams = ConstraintLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        MApplication.screenHeight - resources.getDimensionPixelSize(R.dimen.user_icon)
                    )
                    binding.userRoot.addView(this)
                }
            binding.userRoot.addView(logView)
            return
        }
        val views = mutableListOf<View>(
            initModeView(
                R.string.change_icon.getString()
            ) { UserConfigViewModel.UserConfigEvent.Icon },
            initModeView(
                R.string.change_name.getString()
            ) { UserConfigViewModel.UserConfigEvent.Name },
            initModeView(
                R.string.change_password.getString()
            ) { UserConfigViewModel.UserConfigEvent.PassWord },
            initModeView(
                R.string.share.getString()
            ) { UserConfigViewModel.UserConfigEvent.Share },
            initModeView(
                if (lockMode != UserConfigViewModel.DisplayMode.Locked)
                    R.string.encryp_model.getString() else R.string.unlock_model.getString()
            ) {
                if (lockMode != UserConfigViewModel.DisplayMode.Locked)
                    UserConfigViewModel.UserConfigEvent.Lock else UserConfigViewModel.UserConfigEvent.Unlock
            },
            initModeView(
                if (galleryMode == UserConfigViewModel.DisplayMode.CombineGallery)
                    R.string.dispatch_gallery.getString() else R.string.combine_gallery.getString()
            ) {
                if (it.itemName.text.equals(R.string.dispatch_gallery.getString())) {
                    it.itemName.text = R.string.combine_gallery.getString()
                    UserConfigViewModel.UserConfigEvent.DispatchGallery
                } else {
                    it.itemName.text = R.string.dispatch_gallery.getString()
                    UserConfigViewModel.UserConfigEvent.CombineGallery
                }
            },
            initModeView(
                R.string.clear_cache.getString()
            ) { UserConfigViewModel.UserConfigEvent.ClearCache },
            logView
        )
        launch {
            views.forEach {
                TransitionManager.beginDelayedTransition(binding.userRoot)
                binding.userRoot.addView(it)
                delay(180)
            }
        }
    }

    private fun initModeView(
        name: String,
        onClick: (UserConfigItemLayoutBinding) -> UserConfigViewModel.UserConfigEvent
    ) = UserConfigItemLayoutBinding.inflate(layoutInflater, root, false)
        .apply {
            itemName.text = name
            itemName.setOnClickListener {
                sendViewEvent(onClick.invoke(this))
            }
        }.root

    private suspend fun startIconPick() {
        toGallery(CHOOSE_PHOTO).let { re ->
            if (re == null) {
                R.string.come_up_unknown_error.getString().toast()
                return@let
            }
            val toEntity = re.getStringExtra(GALLERY_DATA)
                ?.toEntity(GalleryMedia::class.java)
            if (toEntity == null) {
                R.string.come_up_unknown_error.getString().toast()
                return@let
            }
            toEntity.uri.imageSaveToDisk(toEntity.name).let { s ->
                if (!s.isNullOrEmpty()) {
                    if (userConfig.userIcon.isNotEmpty()) {
                        viewModel.deleteOldIcon(userConfig.userIcon)
                    }
                    userConfig.userIcon = s
                } else {
                    R.string.failed_upload_image.getString().toast()
                }
            }
        }
    }

    private fun startLoginDialog() {
        loginDialog(
            userConfig.userName != "",
            { name, password ->
                return@loginDialog when {
                    userConfig.userName == "" -> false
                    name != userConfig.userName -> false
                    password != userConfig.userPassword -> false
                    else -> {
                        refreshLayout()
                        userConfig.isLogin = true
                        true
                    }
                }
            },
            {
                var result = false
                regDialog { s, s2 ->
                    userConfig.userName = s
                    userConfig.userPassword = s2
                    userConfig.isLogin = true
                    result = true
                    result
                }
                result
            })
    }

    private fun startNameDialog() {
        titleDialog(R.string.user_name.getString(), "") {
            if (it.isNotEmpty()) { userConfig.userName = it }
        }
    }

    private fun startPasswordDialog() {
        titleDialog(R.string.password.getString(), "") {
            if (it == userConfig.userPassword) {
                userConfig.userPassword = it
                R.string.success.getString().toast()
            } else R.string.wrong_password.getString().toast()
        }
    }

    private fun refreshLayout() {
        clear()
        chooseMode(
            if (!userConfig.isLogin) {
                UserConfigViewModel.DisplayMode.UnRegis
            } else UserConfigViewModel.DisplayMode.Normal,
            if (userConfig.lockGallery != "" || userConfig.lockNote != "" || userConfig.lockMusic != "") {
                UserConfigViewModel.DisplayMode.Locked
            } else UserConfigViewModel.DisplayMode.Normal,
            if (userConfig.combineGallery) {
                UserConfigViewModel.DisplayMode.CombineGallery
            } else UserConfigViewModel.DisplayMode.Normal
        )
    }

    private fun startLockListPop() {
        checkListDialog(
            R.string.choose.getString(),
            mutableListOf(
                R.string.model_noteBook.getString(),
                R.string.model_music.getString(),
                R.string.model_gallery.getString()
            )
        ) {
            if (!it.isNullOrEmpty()) {
                titleDialog(R.string.choose.getString(), "") { lock ->
                    if (lock.isEmpty()) {
                        R.string.none.getString().toast()
                        return@titleDialog
                    }
                    when (it) {
                        R.string.model_noteBook.getString() -> userConfig.lockNote = lock
                        R.string.model_music.getString() -> userConfig.lockNote = lock
                        else -> userConfig.lockGallery = lock
                    }
                }
            } else {
                R.string.none.getString().toast()
            }
        }
    }

    private fun startUnlockListPop() {
        checkListDialog(
            R.string.choose.getString(),
            mutableListOf(
                R.string.model_noteBook.getString(),
                R.string.model_music.getString(),
                R.string.model_gallery.getString()
            )
        ) {
            if (!it.isNullOrEmpty()) {
                titleDialog(R.string.password.getString(), "") { lock ->
                    if (lock.isEmpty()) {
                        R.string.none.getString().toast()
                        return@titleDialog
                    }
                    if (when (it) {
                            R.string.model_noteBook.getString() -> {
                                if (userConfig.lockNote == lock) {
                                    userConfig.lockNote = ""
                                    true
                                } else false
                            }
                            R.string.model_music.getString() -> {
                                if (userConfig.lockNote == lock) {
                                    userConfig.lockNote = ""
                                    true
                                } else false
                            }
                            else -> {
                                if (userConfig.lockGallery == lock) {
                                    userConfig.lockGallery = ""
                                    true
                                } else false
                            }
                        }
                    ) R.string.success.getString().toast()
                    else R.string.wrong_password.getString().toast()
                }
            } else {
                R.string.none.getString().toast()
            }
        }
    }
}