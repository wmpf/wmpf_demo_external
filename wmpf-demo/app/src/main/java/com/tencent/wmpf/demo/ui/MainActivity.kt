package com.tencent.wmpf.demo.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.telephony.TelephonyManager
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.tencent.luggage.demo.wxapi.DeviceInfo
import com.tencent.wmpf.cli.task.*
import com.tencent.wmpf.cli.task.pb.WMPFBaseRequestHelper
import com.tencent.wmpf.demo.Api
import com.tencent.wmpf.demo.R
import com.tencent.wmpf.demo.utils.InvokeTokenHelper
import com.tencent.wmpf.proto.*
import com.tencent.wxapi.test.OpenSdkTestUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    private var userInfoTextView: TextView? = null
    private var avatarImageView: ImageView? = null

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!checkPermission(this)) {
            requestPermission(this)
        }

        userInfoTextView = findViewById<TextView>(R.id.userInfoTextView)
        avatarImageView = findViewById<ImageView>(R.id.avatarImageView)

        // Step 1.1
        findViewById<Button>(R.id.btn_init_wmpf_activate_device).setOnClickListener {
            Api.activateDevice(DeviceInfo.productId, DeviceInfo.keyVersion,
                            DeviceInfo.deviceId, DeviceInfo.signature, DeviceInfo.APP_ID)
                    .subscribe({
                        Log.i(TAG, "success: token = ${it.invokeToken}")
                        if (it.invokeToken == null) {
                            Log.e(TAG, "edit your device info on com.tencent.luggage.demo.wxapi.DeviceInfo")
                            return@subscribe
                        }
                        InvokeTokenHelper.initInvokeToken(this, it.invokeToken)
                        postToMainThread(Runnable {
                            Toast.makeText(this, String.format("init finish, err %d",
                                    it?.baseResponse?.errCode), Toast.LENGTH_SHORT).show()
                        })
                    }, {
                        Log.e(TAG, "error: $it")
                    })
        }

        // Step 1.2
        findViewById<Button>(R.id.btn_init_wmpf).setOnClickListener {
            // Initialize wmpf runtime first
            Api.authorize()
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .flatMap { response ->
                        Log.e(TAG, "init response callback %d, code=%s".format(response.baseResponse.errCode, response.oauthCode))
                        if (response.baseResponse.errCode != 0) {
                            throw Throwable("err, ret:${response.baseResponse.errCode}")
                        } else {
                            OpenSdkTestUtil.getOAuthInfo(DeviceInfo.APP_ID, DeviceInfo.APP_SECRET, response.oauthCode)
                                    .flatMap { jsonObject ->
                                        val openId = jsonObject.getString("openid")
                                        val accessToken = jsonObject.getString("access_token")
                                        OpenSdkTestUtil.getUserInfo(openId, accessToken)
                                    }
                        }
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ jsonObject ->
                        Log.d(TAG, "userInfo=$jsonObject")
                        updateUserInfo(
                                jsonObject.getString("openid"),
                                jsonObject.getString("nickname"),
                                jsonObject.getInt("sex"),
                                jsonObject.getString("province"),
                                jsonObject.getString("city"),
                                jsonObject.getString("country"),
                                jsonObject.getString("unionid"),
                                jsonObject.getString("headimgurl"),
                                getIMEI()
                        )
                    }, {
                        Log.e(TAG, "fail ${it.message}")
                    })
        }

        findViewById<Button>(R.id.btn_wmpf_runtime_preload).setOnClickListener {
            Api.preloadRuntime()
                    .subscribe({
                        Log.i(TAG, "success: $it")
                    }, {
                        Log.e(TAG, "error: $it")
                    })
        }

        // Step 2.1
        findViewById<Button>(R.id.btn_launch_wxa_app).setOnClickListener {
            AlertDialog.Builder(this).setNegativeButton("normal") { _, _ ->

                // Start wxa app
                Api.launchWxaApp("wxe5f52902cf4de896", "", landsapeMode = 0)
                        .subscribe({
                            Log.i(TAG, "success: $it")
                        }, {
                            Log.e(TAG, "error: $it")
                        })
            }.setNeutralButton("landscape compat") { _, _ ->
                // Start wxa app
                Api.launchWxaApp("wxe5f52902cf4de896", "", landsapeMode = 2)
                        .subscribe({
                            Log.i(TAG, "success: $it")
                        }, {
                            Log.e(TAG, "error: $it")
                        })
            }.setPositiveButton("landscape") { _, _ ->
                // Start wxa app
                Api.launchWxaApp("wxe5f52902cf4de896", "", landsapeMode = 1)
                        .subscribe({
                            Log.i(TAG, "success: $it")
                        }, {
                            Log.e(TAG, "error: $it")
                        })
            }.show()
        }

        // Step 2.2
        findViewById<Button>(R.id.btn_launch_wxa_app_by_target_path).setOnClickListener {
            // Start wxa target path app
            Api.launchWxaApp("wxe5f52902cf4de896", "page/component/pages/view/view")
                    .subscribe({
                        Log.i(TAG, "success: $it")
                    }, {
                        Log.e(TAG, "error: $it")
                    })
        }

        // Step 2.2
        findViewById<Button>(R.id.btn_launch_wxa_app_by_scan).setOnClickListener {
            // Start wxa app by scan

            // Api.launchWxaAppByScan("")

            // U can direct send request
            val request = WMPFLaunchWxaAppByQRCodeRequest()
            request.baseRequest = WMPFBaseRequestHelper.checked()
            request.baseRequest.clientApplicationId = ""

            // U also can use scan invoker, contain scan ui
            LaunchWxaAppByScanInvoker.launchWxaByScanUI(this, request)

        }

        Api.notifyBackgroundMusic()
                .subscribe({
                    Log.e(TAG, "music state:${it.state}")
                }, {
                    Log.e(TAG, "error: $it")
                })
    }

    private fun postToMainThread(action: Runnable) {
        userInfoTextView?.post(action)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(0, 0, 0, "背景音频管理")
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        Log.i(TAG, "menuItem id" + item?.itemId)
        return when (item?.itemId) {
            0 -> {
                Api.manageBackgroundMusic(true)
                        .subscribe({
                            Log.i(TAG, "success: $it")
                        }, {
                            Log.e(TAG, "error: $it")
                        })
                true
            }
            else -> {
                false
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            0 -> if (requestCode == Activity.RESULT_OK) {
                data.getStringExtra("code")
            }
        }
    }

    private fun updateUserInfo(openId: String, nickname: String, sex: Int, province: String, city: String, country: String, unionId: String, avatarUrl: String, deviceId: String) {
        userInfoTextView!!.text = String.format("openId:%s\nnick: %s\nsex: %d\nprovince: %s\ncity: %s\ncountry: %s\nunionId: %s\ndeviceId:%s",
                openId,
                nickname,
                sex,
                province,
                city,
                country,
                unionId,
                deviceId
        )

        userInfoTextView!!.setOnLongClickListener {
            copyToClip(deviceId)
            Toast.makeText(this, "$deviceId is copy", Toast.LENGTH_LONG).show()
            true
        }

        Glide.with(this).load(avatarUrl).into(avatarImageView!!)
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

    @SuppressLint("MissingPermission", "HardwareIds")
    fun getIMEI(): String {
        val telephonyMgr = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        return telephonyMgr.deviceId ?: "error"
    }

    private fun copyToClip(content: String?) {
        val cmb = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cmb.text = content ?: ""
    }

    companion object {
        private const val TAG = "WMPF.Demo.MainActivity"
    }
}
