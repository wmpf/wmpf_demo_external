package com.tencent.wmpf.demo.experience

import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.tencent.mmkv.MMKV
import com.tencent.wmpf.app.WMPFBoot
import com.tencent.wmpf.cli.api.WMPF
import com.tencent.wmpf.cli.api.WMPFApiException
import com.tencent.wmpf.cli.api.WMPFMiniProgramApi.LandscapeMode
import com.tencent.wmpf.cli.model.WMPFDevice
import com.tencent.wmpf.cli.model.WMPFStartAppParams
import com.tencent.wmpf.cli.model.WMPFStartAppParams.WMPFAppType
import com.tencent.wmpf.cli.task.TaskError
import com.tencent.wmpf.demo.BuildConfig
import com.tencent.wmpf.demo.Cgi
import com.tencent.wmpf.demo.R
import com.tencent.wmpf.demo.utils.WMPFDemoLogger
import com.tencent.wmpf.demo.utils.WMPFDemoUtil

/**
 * Created by complexzeng on 2020/6/17 2:59 PM.
 */
class ExperienceActivity : AppCompatActivity() {
    private companion object {
        private const val TAG = "ExperienceActivity"
    }

    private var landscapeMode = LandscapeMode.NORMAL
    private lateinit var logger: WMPFDemoLogger
    private var wmpfDevice: WMPFDevice? = null

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun init(
        appId: String,
        ticket: String,
    ) {
        if (appId.isEmpty() || ticket.isEmpty()) {
            throw Exception("请输入 appId 和 ticket")
        }

        var newDevice: WMPFDevice? = null

        try {
            val res = Cgi.getTestDeviceInfo(ticket, appId, BuildConfig.HOST_APPID)
            newDevice = WMPFDevice(
                BuildConfig.HOST_APPID, res.productId, res.keyVersion, res.deviceId, res.signature
            )
            logger.i("设备信息获取成功: $newDevice")
        } catch (e: Exception) {
            throw Exception("请求设备信息失败: " + e.message)
        }


        if (wmpfDevice == null) {
            wmpfDevice = newDevice
            // init 只能调用一次
            WMPFBoot.init(this.applicationContext, wmpfDevice)
        } else if (wmpfDevice != newDevice) {
            throw Exception("设备信息发生变化，请重新启动应用。")
        }

        // 从 WMPF-cli 2.2 开始，可以不显式调用 activateDevice
        try {
            logger.i("--------设备激活中--------")
            WMPF.getInstance().deviceApi.activateDevice()
        } catch (e: WMPFApiException) {
            Log.e(TAG, "error: $e")
            if (e.errCode == TaskError.DISCONNECTED.errCode) {
                throw Exception("设备激活失败，请确认 WMPF 处于运行状态")
            } else if (e.errMsg == "DEVICE_CHANGED") {
                throw Exception("deviceId 发生变化，请清除 WMPF Apk 缓存后重试")
            } else {
                throw Exception("设备激活失败: " + e.message)
            }
        }
        logger.i("设备激活成功，初始化完成")
    }

    private fun launchMiniProgram(
        appId: String, ticket: String, path: String, landscapeMode: LandscapeMode
    ) {
        this.hideKeyboard()
        logger.clear()
        try {
            init(appId, ticket)
        } catch (e: Exception) {
            logger.e("初始化失败", e)
            return
        }

        logger.i("--------开始启动小程序--------")
        try {
            WMPF.getInstance().miniProgramApi.launchMiniProgram(
                WMPFStartAppParams(appId, path, WMPFAppType.APP_TYPE_RELEASE), false, landscapeMode
            )
            logger.i("启动小程序成功")
        } catch (e: Exception) {
            logger.e("启动小程序失败", e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_experience)
        logger = WMPFDemoLogger(TAG, this, findViewById(R.id.resp_tv))

        val landscapeSwitch = findViewById<Switch>(R.id.switch_landscape)
        landscapeSwitch.setOnCheckedChangeListener { _, isClicked ->
            landscapeMode = if (isClicked) {
                LandscapeMode.LANDSCAPE_COMPAT
            } else {
                LandscapeMode.NORMAL
            }
        }

        val appIdView = findViewById<TextView>(R.id.et_launch_app_id)
        val ticketView = findViewById<TextView>(R.id.et_ticket)
        val pathView = findViewById<TextView>(R.id.et_path)

        val kv = MMKV.mmkvWithID(TAG)

        val savedAppId = kv.getString("appId", "")
        val savedTicket = kv.getString("ticket", "")
        val savedPath = kv.getString("path", "")

        if (!savedAppId.isNullOrBlank()) {
            appIdView.text = savedAppId
        }

        if (!savedTicket.isNullOrBlank()) {
            ticketView.text = savedTicket
        }

        if (!savedPath.isNullOrBlank()) {
            pathView.text = savedPath
        }

        findViewById<Button>(R.id.btn_launch_wxa_app_quickly).setOnClickListener {
            val appId = appIdView.text.toString()
            val ticket = ticketView.text.toString()
            val path = pathView.text.toString()
            kv.putString("appId", appId)
            kv.putString("ticket", ticket)
            kv.putString("path", path)

            WMPFDemoUtil.execute {
                launchMiniProgram(appId, ticket, path, landscapeMode)
            }
        }

        WMPFDemoUtil.checkWMPFVersion(this)
    }
}
