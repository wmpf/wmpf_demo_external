package com.tencent.wmpf.demo.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.tencent.wmpf.app.WMPFBoot
import com.tencent.wmpf.cli.api.WMPF
import com.tencent.wmpf.demo.BuildConfig
import com.tencent.wmpf.demo.Cgi
import com.tencent.wmpf.demo.R
import com.tencent.wmpf.demo.utils.WMPFDemoLogger
import com.tencent.wmpf.demo.utils.WMPFDemoUtil

class MpDeviceActivity : ApiActivity() {
    companion object {
        private const val TAG = "MpDeviceActivity"
    }

    private lateinit var logger: WMPFDemoLogger
    private val consoleView: TextView by lazy {
        findViewById(R.id.console)
    }

    private var accessToken = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mp_device)
        logger = WMPFDemoLogger(TAG, this, consoleView)

        findViewById<Button>(R.id.btn_device_register).setOnClickListener {
            WMPFDemoUtil.execute {
                logger.clear()
                val sn = WMPFBoot.getWMPFDevice()!!.deviceId
                if (accessToken.isBlank()) {
                    logger.i("**Test Only: 该示例没有维护状态, 不应该多次获取token**")
                    logger.i("1. 获取access_token...")
                    accessToken =
                        Cgi.getAccessToken(BuildConfig.WXA_APPID, BuildConfig.WXA_APPSECRET)
                }
                logger.i("2. 获取snTicket...")
                val snTicket = Cgi.getSnTicket(
                    accessToken, sn, BuildConfig.WXA_MODEL_ID
                )
                logger.i("snTicket=$snTicket")
                runOnUiThread {
                    AlertDialog.Builder(this).setTitle("设备注册过程不可逆，请务必确认你已知悉后果！")
                        .setNegativeButton("我确认") { _, _ ->
                            try {
                                invokeWMPFApi("registerMiniProgramDevice") {
                                    val res =
                                        WMPF.getInstance().miniProgramDeviceApi.registerMiniProgramDevice(
                                            BuildConfig.WXA_APPID,
                                            BuildConfig.WXA_MODEL_ID,
                                            sn,
                                            snTicket
                                        )
                                    if (res.code == 0) {
                                        logger.i("注册设备成功")
                                    } else {
                                        logger.e("注册设备失败, code=${res.code}")
                                    }
                                }
                            } catch (e: Exception) {
                                logger.e("注册设备失败", e)
                            }

                        }.show()
                }
            }
        }

        findViewById<Button>(R.id.btn_device_prefetch).setOnClickListener {
            invokeWMPFApi("prefetchDeviceToken") {
                val result = WMPF.getInstance().miniProgramDeviceApi.prefetchDeviceToken()
                if (result.errMsg == null) {
                    showOk("预拉取成功")
                } else {
                    showOk("预拉取失败: ${result.errMsg}")
                }
            }
        }
        findViewById<Button>(R.id.btn_device_get_info).setOnClickListener {
            if (WMPFDemoUtil.isLessThanWMPF22(application)) {
                showNotSupported("getMiniProgramDeviceInfo")
                return@setOnClickListener
            }
            invokeWMPFApi("") {
                val info = WMPF.getInstance().miniProgramDeviceApi.miniProgramDeviceInfo
                showOk("获取成功：sn=${info.sn}, modelId=${info.modelId}, isRegistered=${info.isRegistered}")
            }
        }
    }
}