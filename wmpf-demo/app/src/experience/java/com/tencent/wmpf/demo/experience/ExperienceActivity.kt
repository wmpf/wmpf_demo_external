package com.tencent.wmpf.demo.experience

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import com.tencent.luggage.demo.wxapi.DeviceInfo
import com.tencent.wmpf.cli.api.WMPF
import com.tencent.wmpf.cli.window.WMPFFloatWindowSpecific
import com.tencent.wmpf.demo.Api
import com.tencent.wmpf.demo.R
import com.tencent.wmpf.demo.RequestsRepo
import java.util.*

/**
 * Created by complexzeng on 2020/6/17 2:59 PM.
 */
@SuppressLint("LongLogTag", "SetTextI18n")
class ExperienceActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "MicroMsg.ExperienceActivity"
    }

    private var landscapeMode = 0

    private lateinit var pathEditText: EditText
    private lateinit var respTextView: TextView
    private lateinit var appIdEditView: EditText
    private lateinit var ticketEditView: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_experience)
        this.appIdEditView = findViewById(R.id.et_launch_app_id)
        this.ticketEditView = findViewById(R.id.et_ticket)
        this.respTextView = findViewById(R.id.resp_tv)
        val landscapeSwitch = findViewById<Switch>(R.id.switch_landscape)
        landscapeSwitch.setOnCheckedChangeListener { view, isClicked ->
            landscapeMode = if (isClicked) {
                2
            } else {
                0
            }
        }

        findViewById<Button>(R.id.btn_launch_wxa_app_quickly).setOnClickListener {
            launchWxa(0)
        }

        findViewById<Button>(R.id.btn_launch_wxa_dev_app).setOnClickListener {
            launchWxa(1)
        }

        findViewById<Button>(R.id.btn_launch_wxa_pre_app).setOnClickListener {
            launchWxa(2)
        }

        findViewById<Button>(R.id.btn_launch_float_wxa_app_quickly).setOnClickListener {
            launchWxa(2, true)
        }
        pathEditText = findViewById(R.id.et_path)
        handleIntent()
    }

    override fun onResume() {
        super.onResume()
        appIdEditView.clearFocus()
        findViewById<ViewGroup>(R.id.ll).requestFocus()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent()
    }

    private fun handleIntent() {
        // adb shell am start -n com.tencent.wmpf.demo.experience/com.tencent.wmpf.demo.experience.ExperienceActivity -e appId <appId> -e ticket <ticket> -e floatMode <true|false>
        intent?.also { intent ->
            val appId = intent.getStringExtra("appId").orEmpty().trim()
            val ticket = intent.getStringExtra("ticket").orEmpty().trim()
            if (appId.isNotEmpty() && ticket.isNotEmpty()) {
                val enterPath = intent.getStringExtra("enterPath").orEmpty().trim()
                val floatMode = intent.getStringExtra("floatMode").orEmpty().trim().let { it.toLowerCase() == "true" }
                appIdEditView.setText(appId)
                ticketEditView.setText(ticket)
                pathEditText.setText(enterPath)
                printlnToTextView(
                    String.format(
                        "start: appId=[%s] ticket=[%s] enterPath=[%s] isFlotMode=[%b]",
                        appIdEditView.text,
                        ticketEditView.text,
                        pathEditText.text,
                        floatMode
                    )
                )
                launchWxa(0, floatMode)
            }
        }
    }

    private fun printlnToTextView(log: String) {
        respTextView.post { respTextView.text = "\n" + respTextView.text + log + "\n" }
    }

    private fun launchWxa(versionType: Int, floatMode: Boolean = false) {
        val appId = appIdEditView.text.toString().trim()
        val ticket = ticketEditView.text.toString().trim()
        printlnToTextView("开始获取设备信息")
        RequestsRepo.getTestDeviceInfo(ticket, appId, DeviceInfo.APP_ID) {
            printlnToTextView("设备信息：$it")
            respTextView.post {
                printlnToTextView(it)
                val temp = it
                if (temp.toLowerCase(Locale.ROOT).contains("error")) {
                    DeviceInfo.reset()
                    return@post
                }
                printlnToTextView("--------激活设备中--------")
                Api.activateDevice(
                    DeviceInfo.productId, DeviceInfo.keyVersion,
                    DeviceInfo.deviceId, DeviceInfo.signature, DeviceInfo.APP_ID
                )
                    .subscribe({
                        Log.i(TAG, "success: $it")
                        respTextView.post {
                            printlnToTextView(String.format(
                                "init finish, err %d",
                                it?.baseResponse?.errCode
                            ))
                            if (it.invokeToken == null) {
                                printlnToTextView("activate device fail for a null token, may ticket is expired")
                            } else {
                                if (versionType == 0) {
                                    if (floatMode) {
                                        val ret = intArrayOf(0)
                                        WMPF.getInstance().startFloatWindowApp(
                                            optLaunchAppId(),
                                            optPath(),
                                            0,
                                            WMPFFloatWindowSpecific(810, 1440, 1440, 810, 16F, Gravity.CENTER, 0, 0, true),
                                            ret
                                        )
                                        printlnToTextView("float mode launch ret= ${ret[0]}")
                                    } else {
                                        Api.launchWxaApp(optLaunchAppId(), optPath(), landsapeMode = landscapeMode).subscribe({
                                            Log.i(TAG, "success: ${it.baseResponse.errCode} ${it.baseResponse.errMsg}")
                                        }, {
                                            Log.e(TAG, "error: $it")
                                        })
                                    }
                                }
                            }
                        }
                    }, {
                        Log.e(TAG, "error: $it")
                        respTextView.post {
                            var errorMsg = it.message ?: ""
                            if (errorMsg.contains("bridge not found")) {
                                errorMsg += ", 确认WMPF框架处于运行状态"
                            }
                            Toast.makeText(this, "激活设备失败, error: $errorMsg", Toast.LENGTH_SHORT).show()
                            printlnToTextView("激活设备失败, error: $errorMsg")
                        }
                    })
            }
        }
    }

    private fun optPath(): String {
        val path = pathEditText.text?.toString()
        return if (path == null || path.isBlank()) {
            ""
        } else {
            path
        }
    }

    private fun optLaunchAppId(): String {
        var launchAppId = findViewById<EditText>(R.id.et_launch_app_id).text.toString()
        if (launchAppId.isEmpty()) {
            launchAppId = "wxe5f52902cf4de896"
        }
        return launchAppId
    }
}
