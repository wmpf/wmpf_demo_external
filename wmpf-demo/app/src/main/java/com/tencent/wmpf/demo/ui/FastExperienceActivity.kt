package com.tencent.wmpf.demo.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.tencent.luggage.demo.wxapi.DeviceInfo
import com.tencent.mmkv.MMKV
import com.tencent.wmpf.cli.task.*
import com.tencent.wmpf.cli.task.pb.WMPFBaseRequestHelper
import com.tencent.wmpf.demo.Api
import com.tencent.wmpf.demo.R
import com.tencent.wmpf.demo.RequestsRepo
import com.tencent.wmpf.demo.utils.InvokeTokenHelper
import com.tencent.wmpf.proto.*
import com.tencent.wxapi.test.OpenSdkTestUtil

class FastExperienceActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fast_experience)

        if (!checkPermission(this)) {
            requestPermission(this)
        }

//        findViewById<EditText>(R.id.et_launch_app_id).setText("wxe5f52902cf4de896")
        findViewById<EditText>(R.id.et_app_secret).setText(DeviceInfo.APP_SECRET)
        findViewById<EditText>(R.id.et_app_id).setText(DeviceInfo.APP_ID)

        findViewById<Button>(R.id.btn_launch_wxa_app).setOnClickListener {
            launchWxa()
        }

        findViewById<Button>(R.id.btn_launch_login).setOnClickListener {
            Api.activateDevice(DeviceInfo.productId, DeviceInfo.keyVersion, DeviceInfo.deviceId, DeviceInfo.signature, DeviceInfo.APP_ID)
                    .flatMap {
                        InvokeTokenHelper.initInvokeToken(this, it.invokeToken)
                        OpenSdkTestUtil.getSDKTicket(optAppId(), optAppSecret())
                                .flatMap { res ->
                                    Api.authorize(optAppId(), res, "snsapi_userinfo,snsapi_runtime_apk")
                                }
                    }
                    .subscribe({
                        Log.e(TAG, "success: ${it.baseResponse.ret}")
                    }, {
                        Log.e(TAG, "error: $it")
                    })
        }

        val appIdEditView = findViewById<EditText>(R.id.et_launch_app_id)
        val ticketEditView = findViewById<EditText>(R.id.et_ticket)
        val kv = MMKV.mmkvWithID(TAG)
        if (!kv.getString("appId", "").isNullOrBlank() && !kv.getString("ticket", "").isNullOrBlank()) {
            appIdEditView.setText(kv.getString("appId", ""))
            ticketEditView.setText(kv.getString("ticket", ""))
        }
        val respTextView = findViewById<TextView>(R.id.tv_device_info_resp)

        findViewById<Button>(R.id.btn_launch_wxa_app_quickly).setOnClickListener {
            respTextView.text = ""
            kv.putString("appId", appIdEditView.text.toString())
            kv.putString("ticket", ticketEditView.text.toString())
            RequestsRepo.getTestDeviceInfo(ticketEditView.text.toString(), appIdEditView.text.toString(), DeviceInfo.APP_ID) {
                respTextView.post {
                    respTextView.text = it
                    val temp = it
                    if (temp.toLowerCase().contains("error")) {
                        DeviceInfo.reset()
                        return@post
                    }
                    var consoleText = respTextView.text.toString() + "\n" + "--------激活设备中--------\n"
                    respTextView.text = consoleText
                    Api.activateDevice(DeviceInfo.productId, DeviceInfo.keyVersion,
                            DeviceInfo.deviceId, DeviceInfo.signature, DeviceInfo.APP_ID)
                            .subscribe({
                                Log.i(TAG, "success: $it")
                                respTextView.post {
                                    consoleText += String.format("init finish, err %d",
                                            it?.baseResponse?.ret)
                                    respTextView.text = "$consoleText\n--------启动小程序--------\n"
                                    InvokeTokenHelper.initInvokeToken(this, it.invokeToken)
                                    Api.launchWxaApp(optLaunchAppId(), "").subscribe({},{})
                                }

                            }, {
                                Log.e(TAG, "error: $it")
                                respTextView.post {
                                    consoleText += "激活设备失败, error: " + it.message
                                    respTextView.text = consoleText
                                }
                            })
                }
            }
        }

        findViewById<Button>(R.id.btn_launch_wxa_dev_app).setOnClickListener {
            launchDevWxaApp()
        }

        findViewById<Button>(R.id.btn_launch_wxa_pre_app).setOnClickListener {
            /**
            启动开发版小程序必须先登录
            0   // 正式版
            1   // 测试版
            2   // 体验
             **/
            Api.launchWxaApp(optLaunchAppId(), "", 2)
                    .subscribe({
                        Log.e(TAG, "success: $it")
                    }, {
                        Log.e(TAG, "error: $it")
                    })
        }

        findViewById<Button>(R.id.btn_launch_remote_debug).setOnClickListener {
            val request = WMPFLaunchWxaAppByQRCodeRequest()
            request.baseRequest = WMPFBaseRequestHelper.checked()
            request.baseRequest.clientApplicationId = ""
            LaunchWxaAppByScanInvoker.launchWxaByScanUI(this, request)
        }

    }

    @SuppressLint("CheckResult")
    private fun launchWxa() {
        Api.activateDevice(DeviceInfo.productId, DeviceInfo.keyVersion, DeviceInfo.deviceId, DeviceInfo.signature, DeviceInfo.APP_ID)
                .flatMap {
                    InvokeTokenHelper.initInvokeToken(this, it.invokeToken)
                    Api.launchWxaApp(optLaunchAppId(), "")
                }
                .subscribe({
                    Log.e(TAG, "success: $it")
                }, {
                    Log.e(TAG, "error: $it")
                })
    }

    /**
    启动开发版小程序必须先登录
    0   // 正式版
    1   // 测试版
    2   // 体验
     **/
    @SuppressLint("CheckResult")
    private fun launchDevWxaApp() {
        Api.launchWxaApp(optLaunchAppId(), "", 1)
                .subscribe({
                    Log.e(TAG, "success: $it")
                }, {
                    Log.e(TAG, "error: $it")
                })
    }

    private fun optLaunchAppId(): String {
        var launchAppId = findViewById<EditText>(R.id.et_launch_app_id).text.toString()
        if (launchAppId == null || launchAppId.isEmpty()) {
            launchAppId = "wxe5f52902cf4de896"
        }
        return launchAppId
    }

    private fun optAppSecret(): String {
        var appSecret = findViewById<EditText>(R.id.et_app_secret).text.toString()
        if (appSecret == null || appSecret.isEmpty()) {
            appSecret = DeviceInfo.APP_SECRET
        }

        return appSecret
    }

    private fun optAppId(): String {
        var appId = findViewById<EditText>(R.id.et_app_id).text.toString()
        if (appId == null || appId.isEmpty()) {
            appId = DeviceInfo.APP_ID
        }
        return appId
    }

    private fun checkPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val ret0 = context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            val ret1 = context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val ret2 = context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
            val ret3 = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            val ret4 = context.checkSelfPermission(Manifest.permission.CAMERA)
            val ret5 = context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
            return ret0 == PackageManager.PERMISSION_GRANTED &&
                    ret1 == PackageManager.PERMISSION_GRANTED &&
                    ret2 == PackageManager.PERMISSION_GRANTED &&
                    ret3 == PackageManager.PERMISSION_GRANTED &&
                    ret4 == PackageManager.PERMISSION_GRANTED &&
                    ret5 == PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    private fun requestPermission(context: Activity) {
        try {
            ActivityCompat.requestPermissions(context, arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_PHONE_STATE),
                    0)
        } catch (e: Exception) {

        }

    }

    companion object {
        private const val TAG = "FastExperienceActivity"
    }
}
