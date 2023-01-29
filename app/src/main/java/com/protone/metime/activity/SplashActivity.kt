package com.protone.metime.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.viewModels
import com.protone.common.context.*
import com.protone.component.activity.BaseActivity
import com.protone.component.broadcast.workLocalBroadCast
import com.protone.metime.databinding.SplashActivityBinding
import com.protone.metime.viewModel.SplashViewModel
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<SplashActivityBinding, SplashViewModel, Nothing>() {

    override val viewModel: SplashViewModel by viewModels()

    override fun createView(): SplashActivityBinding {
        return SplashActivityBinding.inflate(layoutInflater, root, false).apply {
            root.post {
                MApplication.apply {
                    screenHeight = root.measuredHeight
                    screenWidth = root.measuredWidth
                }
            }
        }
    }

    override suspend fun SplashViewModel.init() = Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkNeededPermission {
            onSucceed {
                this@SplashActivity.init()
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
        ) init() else finish()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun init() {
        workLocalBroadCast.sendBroadcast(Intent(UPDATE_GALLERY))
        workLocalBroadCast.sendBroadcast(Intent(UPDATE_MUSIC))
        launch {
            viewModel.firstBootWork()
            startActivity(MainActivity::class.intent)
            finish()
        }
    }
}