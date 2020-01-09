package com.tencent.wmpf.demo.ui

import android.Manifest
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
import com.tencent.luggage.demo.wxapi.Constants
import com.tencent.wmpf.cli.task.*
import com.tencent.wmpf.cli.task.pb.WMPFBaseRequestHelper
import com.tencent.wmpf.demo.Api
import com.tencent.wmpf.demo.R
import com.tencent.wmpf.demo.utils.InvokeTokenHelper
import com.tencent.wmpf.proto.*
import com.tencent.wxapi.test.OpenSdkTestUtil

class FastExperienceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fast_experience)

        if (!checkPermission(this)) {
            requestPermission(this)
        }

//        findViewById<EditText>(R.id.et_launch_app_id).setText("wxe5f52902cf4de896")
        findViewById<EditText>(R.id.et_app_secret).setText(Constants.APP_SECRET)
        findViewById<EditText>(R.id.et_app_id).setText(Constants.APP_ID)

        findViewById<Button>(R.id.btn_launch_wxa_app).setOnClickListener {
            Api.activateDevice(Constants.PRODUCT_ID, Constants.KEY_VERSION, Constants.DEVICE_ID, Constants.SIGNATURE, Constants.APP_ID)
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

        findViewById<Button>(R.id.btn_launch_login).setOnClickListener {
            Api.activateDevice(Constants.PRODUCT_ID, Constants.KEY_VERSION, Constants.DEVICE_ID, Constants.SIGNATURE, Constants.APP_ID)
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

        findViewById<Button>(R.id.btn_launch_wxa_dev_app).setOnClickListener {
            /**
            启动开发版小程序必须先登录
                0   // 正式版
                1   // 测试版
                2   // 体验
            **/
            Api.launchWxaApp(optLaunchAppId(), "", 1)
                    .subscribe({
                        Log.e(TAG, "success: $it")
                    }, {
                        Log.e(TAG, "error: $it")
                    })
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
            appSecret = Constants.APP_SECRET
        }

        return appSecret
    }

    private fun optAppId(): String {
        var appId = findViewById<EditText>(R.id.et_app_id).text.toString()
        if (appId == null || appId.isEmpty()) {
            appId = Constants.APP_ID
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
