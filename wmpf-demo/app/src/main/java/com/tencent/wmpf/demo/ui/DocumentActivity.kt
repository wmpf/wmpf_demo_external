package com.tencent.wmpf.demo.ui

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.tencent.mm.ipcinvoker.type.IPCVoid
import com.tencent.wmpf.cli.api.WMPF
import com.tencent.wmpf.cli.api.WMPFAccountApi
import com.tencent.wmpf.cli.api.WMPFLifecycleListener
import com.tencent.wmpf.cli.api.WMPFMiniProgramApi
import com.tencent.wmpf.cli.api.WMPFMusicController
import com.tencent.wmpf.cli.event.AbstractOnDeviceActivationOutdatedEventListener
import com.tencent.wmpf.cli.event.AbstractOnMusicStatusEventListener
import com.tencent.wmpf.cli.event.WMPFMusicStatusData
import com.tencent.wmpf.cli.model.WMPFStartAppParams
import com.tencent.wmpf.demo.BuildConfig
import com.tencent.wmpf.demo.Cgi
import com.tencent.wmpf.demo.R
import com.tencent.wmpf.demo.V1api
import com.tencent.wmpf.demo.utils.WMPFDemoUtil

class DocumentActivity : ApiActivity() {
    private var oauthCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document)

        findViewById<Button>(R.id.btn_activate_device).setOnClickListener {
            invokeWMPFApi("activateDevice") {
                WMPF.getInstance().deviceApi.activateDevice()
                showOk("激活成功")
            }
        }

        findViewById<Button>(R.id.btn_active_status).setOnClickListener {
            invokeWMPFApi("isDeviceActivated") {
                showOk("激活状态：" + WMPF.getInstance().deviceApi.isDeviceActivated)
            }
        }

        findViewById<Button>(R.id.btn_active_status_outdated).setOnClickListener {
            invokeWMPFApi("registerDeviceActivationOutdatedEventListener") {
                if (setupActivationOutdatedListener()) {
                    showOk("监听成功")
                }
            }
        }

        findViewById<Button>(R.id.btn_preload_time).setOnClickListener {
            invokeWMPFApi("preload") {
                WMPF.getInstance().miniProgramApi.preload(null)
                showOk("预加载成功")
            }
        }

        findViewById<Button>(R.id.btn_launch_wxa_app).setOnClickListener {
            val startParams = WMPFStartAppParams(
                DEMO_APP_ID, "", WMPFStartAppParams.WMPFAppType.APP_TYPE_RELEASE
            )
            invokeWMPFApi("launchMiniProgram") {
                WMPF.getInstance().miniProgramApi.launchMiniProgram(
                    startParams, false, WMPFMiniProgramApi.LandscapeMode.NORMAL
                )
                showOk("启动成功")
            }
        }

        findViewById<Button>(R.id.btn_launch_wxa_app_by_scan).setOnClickListener {
            invokeWMPFApi("launchByQRScanCode") {
                WMPF.getInstance().miniProgramApi.launchByQRScanCode()
                showOk("启动成功")
            }
        }

        findViewById<Button>(R.id.btn_launch_wxa_app_by_scan_custom).setOnClickListener {
            invokeWMPFApi("launchWxaByScanUI") {
                // Start wxa app by scan
                // U also can use scan invoker, contain scan ui
                LaunchWxaAppByScanInvoker.launchWxaByScanUI(this)
            }
        }

        findViewById<Button>(R.id.btn_close_wxa_app).setOnClickListener {
            invokeWMPFApi("closeWxaApp") {
                WMPF.getInstance().miniProgramApi.closeWxaApp(
                    DEMO_APP_ID, false
                )
                showOk("关闭成功")
            }
        }

        findViewById<Button>(R.id.btn_authorize).setOnClickListener {
            invokeWMPFApi("login") {
                oauthCode =
                    WMPF.getInstance().accountApi.login(WMPFAccountApi.WMPFLoginUIStyle.FULLSCREEN)
                showOk("登录成功，oauthCode=${oauthCode}")
            }
        }

        findViewById<Button>(R.id.btn_user_info).setOnClickListener {
            invokeWMPFApi("getUserInfo") {
                if (oauthCode == null) {
                    showFail("请重新扫码登录", null)
                    return@invokeWMPFApi
                } else {
                    val authInfo = Cgi.getOAuthInfo(
                        BuildConfig.HOST_APPID, BuildConfig.HOST_APPSECRET, oauthCode!!
                    )
                    showOk("获取 OpenId 成功，OpenId=${authInfo.openId}")

                    val userInfo = Cgi.getUserInfo(BuildConfig.HOST_APPID, authInfo.accessToken)
                    Log.i(TAG, "获取用户信息成功，userInfo=${userInfo}")

                    runOnUiThread {
                        AlertDialog.Builder(this).apply {
                            setTitle("用户信息")
                            setMessage(userInfo.toString())
                        }.show()
                    }
                }
            }
        }

        findViewById<Button>(R.id.btn_authorize_status).setOnClickListener {
            invokeWMPFApi("login") {
                showOk("登录状态：" + WMPF.getInstance().accountApi.isLogin)
            }
        }

        findViewById<Button>(R.id.btn_authorize_status_v1).setOnClickListener {
            // 仅作为混用 v1/v2 API 的示例
            V1api.authorizeStatus().whenComplete { res, e ->
                if (e == null) {
                    showOk("登录状态：${res.isAuthorize}")
                } else {
                    showFail("authorizeStatus 失败", e)
                }
            }
        }


        findViewById<Button>(R.id.btn_de_authorize).setOnClickListener {
            invokeWMPFApi("login") {
                WMPF.getInstance().accountApi.logout()
                showOk("退出登录成功")
            }
        }

        findViewById<Button>(R.id.btn_manage_music).setOnClickListener {
            if (WMPFDemoUtil.isLessThanWMPF22(application)) {
                showNotSupported("showManageUI")
                return@setOnClickListener
            }
            invokeWMPFApi("showManageUI") {
                WMPF.getInstance().musicApi.showManageUI()
            }
        }

        findViewById<Button>(R.id.btn_notify_manage_music).setOnClickListener {
            invokeWMPFApi("addMusicPlayStatusListener") {
                setupMusicListener()
                showOk("绑定监听成功")
            }
        }

        // 调整小程序显示大小，zoom可以设置为任意值，这里给出一组建议值
        val zoomArr = arrayOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
        var zoomIndex = 0
        findViewById<Button>(R.id.btn_ui_zoom).setOnClickListener {
            invokeWMPFApi("setZoom") {
                val zoom = zoomArr[zoomIndex % zoomArr.size]
                WMPF.getInstance().uiApi.setZoom(zoom)
                zoomIndex++
                showOk("设置缩放比例成功: $zoom")
            }
        }
    }

    private var musicController = WMPFMusicController()
    private val musicListener =
        WMPFMusicController.WMPFMusicStatusChangedListener { status -> showOk("music play state changed: $status") }
    private var hasMusicListener = false

    private fun setupMusicListener() {
        if (hasMusicListener) return
        if (WMPFDemoUtil.isLessThanWMPF22(application)) {
            // 2.1 版本使用旧接口
            musicController.addMusicPlayStatusListener(musicListener)
            WMPF.getInstance().addWMPFLifecycleListener(object : WMPFLifecycleListener {
                override fun onWMPFFinish() {
                    // 由于旧接口实现问题，这里需要手动 remove
                    musicController.removeMusicPlayStatusListener(musicListener)
                }

                override fun onWMPFRestart() {
                    // WMPF 重启后需要重新绑定
                    musicController.addMusicPlayStatusListener(musicListener)
                }
            })
        } else {
            // 高版本使用新接口
            WMPF.getInstance().musicApi.registerMusicPlayStatusListener(object :
                AbstractOnMusicStatusEventListener() {
                override fun onInvoke(data: WMPFMusicStatusData) {
                    showOk("music play state changed: ${data.status}")
                }
            })
            // 新接口 WMPF 重启后会自动重新绑定
        }

        hasMusicListener = true
    }

    private var hasOutdatedListener = false

    private fun setupActivationOutdatedListener(): Boolean {
        if (WMPFDemoUtil.isLessThanWMPF22(application)) {
            showNotSupported("registerDeviceActivationOutdatedEventListener")
            return false
        } else {
            if (hasOutdatedListener) return true
            WMPF.getInstance().deviceApi.registerDeviceActivationOutdatedEventListener(object :
                AbstractOnDeviceActivationOutdatedEventListener() {
                override fun onInvoke(p0: IPCVoid) {
                    Log.e(TAG, "设备激活已失效，可能超时或被其他设备占用")
                    runOnUiThread {
                        Toast.makeText(
                            this@DocumentActivity,
                            "设备激活已失效，可能超时或被其他设备占用",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    // 尝试重新激活
                    WMPF.getInstance().deviceApi.activateDevice()
                }
            })
            hasOutdatedListener = true
            return true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (!WMPFDemoUtil.isLessThanWMPF22(application)) {
            menu?.add(0, 0, 0, "背景音频管理")
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.i(TAG, "menuItem id " + item.itemId)
        return when (item.itemId) {
            0 -> {
                invokeWMPFApi("showManageUI") {
                    WMPF.getInstance().musicApi.showManageUI()
                }
                true
            }

            else -> {
                false
            }
        }
    }


    companion object {
        private const val TAG = "DocumentActivity"
        private const val DEMO_APP_ID = "wxe5f52902cf4de896"
    }

}