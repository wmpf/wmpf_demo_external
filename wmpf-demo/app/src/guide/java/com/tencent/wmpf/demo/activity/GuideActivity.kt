package com.tencent.wmpf.demo.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.tencent.wmpf.app.WMPFBoot
import com.tencent.wmpf.cli.api.WMPFDeviceApi
import com.tencent.wmpf.cli.model.WMPFDevice
import com.tencent.wmpf.demo.BuildConfig
import com.tencent.wmpf.demo.R
import com.tencent.wmpf.demo.ui.AgentActivity
import com.tencent.wmpf.demo.ui.DocumentActivity
import com.tencent.wmpf.demo.ui.FastExperienceActivity
import com.tencent.wmpf.demo.ui.MpDeviceActivity
import com.tencent.wmpf.demo.ui.PushMsgQuickStartActivity
import com.tencent.wmpf.demo.ui.VoipActivity
import com.tencent.wmpf.demo.utils.WMPFDemoUtil

class GuideActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_guide)

        findViewById<Button>(R.id.btn_fast_experience).setOnClickListener {
            startActivity(Intent(this, FastExperienceActivity::class.java))
        }


        findViewById<Button>(R.id.btn_document).setOnClickListener {
            startActivity(Intent(this, DocumentActivity::class.java))
        }

        findViewById<Button>(R.id.btn_push).setOnClickListener {
            startActivity(Intent(this, PushMsgQuickStartActivity::class.java))
        }

        findViewById<Button>(R.id.btn_mp_device).setOnClickListener {
            startActivity(Intent(this, MpDeviceActivity::class.java))
        }

        findViewById<Button>(R.id.btn_voip).setOnClickListener {
            startActivity(Intent(this, VoipActivity::class.java))
        }

        findViewById<Button>(R.id.btn_agent).setOnClickListener {
            startActivity(Intent(this, AgentActivity::class.java))
        }

        WMPFDemoUtil.checkWMPFVersion(this)
        initWMPFCli()
    }

    private fun initWMPFCli() {
        val deviceInfo = getDeviceInfo()
        if (deviceInfo.productId == 0) {
            AlertDialog.Builder(this).setTitle("请先在 GuideActivity.kt 设置 DEMO 使用的设备信息")
                .setCancelable(false).setNegativeButton("退出") { _, _ ->
                    this.finish()
                }.show()
            return
        }
        WMPFBoot.init(this.application, getDeviceInfo())
        WMPFDeviceApi.setEnableWakeUp(true)
    }

    private fun getDeviceInfo() = WMPFDevice(
        // TODO: 需要替换成正式的设备信息
        BuildConfig.HOST_APPID, BuildConfig.WMPF_PRODUCT_ID, BuildConfig.WMPF_KEY_VERSION, BuildConfig.WMPF_DEVICE_ID, BuildConfig.WMPF_SIGNATURE
    )
}