package com.protone.gallery.debug

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.protone.common.context.UPDATE_GALLERY
import com.protone.common.context.checkNeededPermission
import com.protone.common.context.intent
import com.protone.common.context.requestContentPermission
import com.protone.component.broadcast.workLocalBroadCast
import com.protone.gallery.activity.GalleryActivity

class OpenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkNeededPermission {
            onSucceed {
                workLocalBroadCast.sendBroadcast(Intent(UPDATE_GALLERY))
                startActivity(GalleryActivity::class.intent)
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
            workLocalBroadCast.sendBroadcast(Intent(UPDATE_GALLERY))
            startActivity(GalleryActivity::class.intent)
        } else {
            finish()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}