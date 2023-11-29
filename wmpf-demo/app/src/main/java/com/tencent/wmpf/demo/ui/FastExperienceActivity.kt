package com.tencent.wmpf.demo.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.tencent.wmpf.cli.api.WMPF
import com.tencent.wmpf.cli.api.WMPFAccountApi
import com.tencent.wmpf.cli.api.WMPFApiException
import com.tencent.wmpf.cli.model.WMPFStartAppParams
import com.tencent.wmpf.cli.model.WMPFStartAppParams.WMPFAppType
import com.tencent.wmpf.demo.R
import com.tencent.wmpf.demo.utils.WMPFDemoLogger
import com.tencent.wmpf.demo.utils.WMPFDemoUtil
import com.tencent.wmpf.demo.utils.WMPFDemoUtil.execute

class FastExperienceActivity : AppCompatActivity() {
    private lateinit var logger: WMPFDemoLogger

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fast_experience)

        logger = WMPFDemoLogger(TAG, this, findViewById(R.id.tv_device_info_resp))

        val perf = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        val appIdView = findViewById<TextView>(R.id.et_launch_app_id)

        val savedAppId = perf.getString("appId", "")
        if (!savedAppId.isNullOrBlank()) {
            appIdView.text = savedAppId
        }

        findViewById<Button>(R.id.btn_launch_wxa_app).setOnClickListener {
            val appId = appIdView.text.toString()
            perf.edit().putString("appId", appId).apply()
            execute {
                launchMiniProgram(appId)
            }
        }

        findViewById<Button>(R.id.btn_launch_login).setOnClickListener {
            logger.clear()
            execute {
                try {
                    WMPF.getInstance().accountApi.login(WMPFAccountApi.WMPFLoginUIStyle.FULLSCREEN)
                    logger.i("扫码登录成功")
                } catch (e: WMPFApiException) {
                    logger.e("扫码登录失败", e)
                }
            }
        }

        findViewById<Button>(R.id.btn_launch_wxa_dev_app).setOnClickListener {
            val appId = appIdView.text.toString()
            perf.edit().putString("appId", appId).apply()
            execute {
                launchMiniProgram(appId, WMPFAppType.APP_TYPE_DEV)
            }
        }

        findViewById<Button>(R.id.btn_launch_wxa_pre_app).setOnClickListener {
            val appId = appIdView.text.toString()
            perf.edit().putString("appId", appId).apply()
            execute {
                launchMiniProgram(appId, WMPFAppType.APP_TYPE_EXP)
            }
        }

        findViewById<Button>(R.id.btn_launch_remote_debug).setOnClickListener {
            logger.clear()
            execute {
                try {
                    WMPF.getInstance().miniProgramApi.launchByQRScanCode()
                } catch (e: WMPFApiException) {
                    logger.e("扫码打开小程序失败", e)
                }
            }
        }
    }

    private fun launchMiniProgram(appId: String, type: WMPFAppType = WMPFAppType.APP_TYPE_RELEASE) {
        logger.clear()
        if (type == WMPFAppType.APP_TYPE_EXP || type == WMPFAppType.APP_TYPE_DEV) {
            if (!WMPF.getInstance().accountApi.isLogin) {
                logger.e("启动${if (type === WMPFAppType.APP_TYPE_EXP) "体验" else "开发"}版小程序请先登录")
                return
            }
        }
        logger.i("启动小程序：$appId")
        try {
            WMPF.getInstance().miniProgramApi.launchMiniProgram(
                WMPFStartAppParams(appId, "", type)
            )
        } catch (e: WMPFApiException) {
            logger.e("启动小程序失败", e)
        }
    }

    companion object {
        private const val TAG = "FastExperienceActivity"
    }
}
