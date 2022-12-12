package com.protone.metime.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.viewModels
import com.protone.common.context.*
import com.protone.metime.databinding.SplashActivityBinding
import com.protone.metime.viewModel.SplashViewModel
import com.protone.component.BaseActivity
import com.protone.component.broadcast.workLocalBroadCast

@SuppressLint("CustomSplashScreen")
class SplashActivity :
    BaseActivity<SplashActivityBinding, SplashViewModel, SplashViewModel.SplashEvent>(true) {

    override val viewModel: SplashViewModel by viewModels()

    override fun createView(): SplashActivityBinding {
        return SplashActivityBinding.inflate(layoutInflater, root, false).apply {
            root.onGlobalLayout {
                MApplication.apply {
                    screenHeight = measuredHeight
                    screenWidth = measuredWidth
                }
            }
        }
    }

    override suspend fun SplashViewModel.init() {
        onViewEvent {
            when (it) {
                SplashViewModel.SplashEvent.InitConfig -> {
                    viewModel.firstBootWork()
                    startActivity(MainActivity::class.intent)
                    finish()
                }
                SplashViewModel.SplashEvent.UpdateMedia -> updateMedia()
            }
        }
    }

    override suspend fun doStart() {
        checkNeededPermission {
            onSucceed {
                sendViewEvent(SplashViewModel.SplashEvent.UpdateMedia)
            }
            onFailed {
                requestContentPermission()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
            && grantResults[1] == PackageManager.PERMISSION_GRANTED
        ) {
            sendViewEvent(SplashViewModel.SplashEvent.UpdateMedia)
        } else {
            finish()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun updateMedia() {
        workLocalBroadCast.sendBroadcast(Intent(UPDATE_GALLERY))
        workLocalBroadCast.sendBroadcast(Intent(UPDATE_MUSIC))
        sendViewEvent(SplashViewModel.SplashEvent.InitConfig)
    }
}