package com.tencent.wmpf.demo.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.tencent.wmpf.cli.api.WMPF
import com.tencent.wmpf.cli.api.WMPFAccountApi
import com.tencent.wmpf.cli.api.WMPFApiException
import com.tencent.wmpf.cli.api.WMPFMiniProgramApi.LandscapeMode
import com.tencent.wmpf.cli.model.WMPFStartAppParams
import com.tencent.wmpf.cli.model.WMPFStartAppParams.WMPFAppType
import com.tencent.wmpf.demo.R
import com.tencent.wmpf.demo.utils.WMPFDemoLogger
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

        var appType = WMPFAppType.APP_TYPE_RELEASE
        findViewById<RadioGroup>(R.id.rg_app_type).setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.app_type_release -> {
                    appType = WMPFAppType.APP_TYPE_RELEASE
                }

                R.id.app_type_dev -> {
                    appType = WMPFAppType.APP_TYPE_DEV
                }

                R.id.app_type_trial -> {
                    appType = WMPFAppType.APP_TYPE_EXP
                }
            }
        }

        var landscapeMode = LandscapeMode.NORMAL
        findViewById<Spinner>(R.id.choose_landscape).apply {
            this.adapter = ArrayAdapter.createFromResource(
                this@FastExperienceActivity,
                R.array.landscapes,
                R.layout.support_simple_spinner_dropdown_item
            )
            this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, view: View?, p2: Int, p3: Long) {
                    landscapeMode = LandscapeMode.values()[p2]
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    landscapeMode = LandscapeMode.NORMAL
                }

            }
        }

        findViewById<Button>(R.id.btn_launch_wxa_app).setOnClickListener {
            val appId = appIdView.text.toString()
            perf.edit().putString("appId", appId).apply()
            execute {
                launchMiniProgram(appId, appType, landscapeMode)
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

        findViewById<Button>(R.id.btn_close_wxa_app).setOnClickListener {
            val appId = appIdView.text.toString()
            execute {
                try {
                    WMPF.getInstance().miniProgramApi.closeWxaApp(appId, false)
                    logger.i("关闭小程序成功")
                } catch (e: WMPFApiException) {
                    logger.e("关闭小程序失败", e)
                }
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

    private fun launchMiniProgram(
        appId: String,
        type: WMPFAppType = WMPFAppType.APP_TYPE_RELEASE,
        landscapeMode: LandscapeMode = LandscapeMode.NORMAL
    ) {
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
                WMPFStartAppParams(appId, "", type), false, landscapeMode
            )
        } catch (e: WMPFApiException) {
            logger.e("启动小程序失败", e)
        }
    }

    companion object {
        private const val TAG = "FastExperienceActivity"
    }
}
