package com.tencent.wmpf.demo.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.tencent.luggage.demo.wxapi.DeviceInfo
import com.tencent.wmpf.demo.Api
import com.tencent.wmpf.demo.R
import com.tencent.wmpf.demo.utils.InvokeTokenHelper
import com.tencent.wxapi.test.OpenSdkTestUtil
import io.reactivex.schedulers.Schedulers

class DocumentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document)

        findViewById<Button>(R.id.btn_activate_device).setOnClickListener {
            Api.activateDevice(DeviceInfo.productId, DeviceInfo.keyVersion,
                    DeviceInfo.deviceId, DeviceInfo.signature, DeviceInfo.APP_ID)
                    .subscribe({
                        Log.i(TAG, "success: ${it.baseResponse.ret} ${it.baseResponse.errMsg} ")
                        if (it.invokeToken != null && it.invokeToken.isNotEmpty()) {
                            Log.i(TAG, "success: ${it.invokeToken} ")
                            InvokeTokenHelper.initInvokeToken(this, it.invokeToken)
                        }
                    }, {
                        Log.e(TAG, "error: $it")
                    })
        }

        findViewById<Button>(R.id.btn_activate_device_by_iot).setOnClickListener {
            Api.activateDeviceByIoT(DeviceInfo.APP_ID)
                    .subscribe({
                        Log.i(TAG, "success: ${it.baseResponse.ret} ${it.baseResponse.errMsg} ")
                        Log.i(TAG, "success: ${it.invokeToken} ")
                        InvokeTokenHelper.initInvokeToken(this, it.invokeToken)
                    }, {
                        Log.e(TAG, "error: $it")
                    })
        }

        findViewById<Button>(R.id.btn_preload_time).setOnClickListener {
            Api.preloadRuntime()
                    .subscribe({
                        Log.i(TAG, "success: ${it.baseResponse.ret} ${it.baseResponse.errMsg}")
                    }, {
                        Log.e(TAG, "error: $it")
                    })
        }

        findViewById<Button>(R.id.btn_authorize).setOnClickListener {
            OpenSdkTestUtil.getSDKTicket(DeviceInfo.APP_ID, DeviceInfo.APP_SECRET)
                    .subscribeOn(Schedulers.io())
                    .flatMap {
                        Api.authorize(DeviceInfo.APP_ID, it, "snsapi_userinfo,snsapi_runtime_apk")
                    }
                    .subscribe({
                        Log.i(TAG, "success: ${it.baseResponse.ret} ${it.baseResponse.errMsg}")
                    }, {
                        Log.e(TAG, "error: $it")
                    })
        }

        findViewById<Button>(R.id.btn_authorize_face).setOnClickListener {
            Api.authorizeFaceLogin()
                    .subscribe({
                        Log.i(TAG, "success: ${it.baseResponse.ret} ${it.baseResponse.errMsg} ")
                    }, {
                        Log.e(TAG, "error: $it")
                    })
        }

        //https://pay.weixin.qq.com/wiki/doc/wxfacepay/develop/sdk-android.html#%E4%BA%BA%E8%84%B8%E6%94%AF%E4%BB%98%E5%87%AD%E8%AF%81-getwxpayfacecode
        findViewById<Button>(R.id.btn_authorize_init_auth_info).setOnClickListener {
            Api.initWxPayInfoAuthInfo(mapOf(
                    "face_authtype" to "FACEPAY" as Object,
                    "appid" to "商户号绑定的公众号/小程序" as Object,
                    "mch_id" to "商户号" as Object,
                    "store_id" to "门店编号" as Object,
                    "out_trade_no" to "商户订单号" as Object,//须与调用支付接口时字段一致，该字段在在face_code_type为"1"时可不填，为"0"时必填
                    "total_fee" to "订单金额(数字)" as Object, // 单位分. 该字段在在face_code_type为"1"时可不填，为"0"时必填
                    "authinfo" to "调用凭证" as Object,
                    //获取方式参见: get_wxpayface_authinfo[https://pay.weixin.qq.com/wiki/doc/wxfacepay/develop/sdk-android.html#%E8%8E%B7%E5%8F%96%E8%B0%83%E7%94%A8%E5%87%AD%E8%AF%81-get-wxpayface-authinfo]
                    "ignore_update_pay_result" to "1" as Object //不需要商户App更新支付结果

            ))
                    .subscribe({
                        Log.i(TAG, "success: ${it.baseResponse.ret} ${it.baseResponse.errMsg} ")
                    }, {
                        Log.e(TAG, "error: $it")
                    })
        }

        findViewById<Button>(R.id.btn_authorize_no_login).setOnClickListener {
            OpenSdkTestUtil.getSDKTicket(DeviceInfo.APP_ID, DeviceInfo.APP_SECRET)
                    .subscribeOn(Schedulers.io())
                    .flatMap {
                        Api.authorizeNoLogin(DeviceInfo.APP_ID, it, "snsapi_userinfo,snsapi_runtime_apk")
                    }
                    .subscribe({
                        Log.i(TAG, "success: ${it.baseResponse.ret} ${it.baseResponse.errMsg}")
                    }, {
                        Log.e(TAG, "error: $it")
                    })
        }

        findViewById<Button>(R.id.btn_launch_wxa_app).setOnClickListener {
            Api.launchWxaApp("wxe5f52902cf4de896", "")
                    .subscribe({
                        Log.i(TAG, "success: ${it.baseResponse.ret} ${it.baseResponse.errMsg}")
                    }, {
                        Log.e(TAG, "error: $it")
                    })
        }

        findViewById<Button>(R.id.btn_launch_wxa_app_by_scan).setOnClickListener {
            Api.launchWxaAppByScan("xxx")
                    .subscribe({
                        Log.i(TAG, "success: ${it.baseResponse.ret} ${it.baseResponse.errMsg}")
                    }, {
                        Log.e(TAG, "error: $it")
                    })
        }

        findViewById<Button>(R.id.btn_close_wxa_app).setOnClickListener {
            Api.closeWxaApp("wxe5f52902cf4de896")
                    .subscribe({
                        Log.i(TAG, "success: ${it.baseResponse.ret} ${it.baseResponse.errMsg}")
                    }, {
                        Log.e(TAG, "error: $it")
                    })
        }

        findViewById<Button>(R.id.btn_manage_music).setOnClickListener {
            Api.manageBackgroundMusic()
                    .subscribe({
                        Log.i(TAG, "success: ${it.baseResponse.ret} ${it.baseResponse.errMsg}")
                    }, {
                        Log.e(TAG, "error: $it")
                    })
        }

        findViewById<Button>(R.id.btn_notify_manage_music).setOnClickListener {
            Api.notifyBackgroundMusic()
                    .subscribe({
                        /**
                         * {@see com.tencent.wmpf.cli.task.IPCInvokerTask_NotifyBackgroundMusic}
                         * val START = 1
                         * val RESUME = 2
                         * val PAUSE = 3
                         * val STOP = 4
                         * val COMPLETE = 5
                         * val ERROR = 6
                         **/
                        Log.i(TAG, "success: ${it.baseResponse.ret} ${it.baseResponse.errMsg} state:${it.state}")
                    }, {
                        Log.e(TAG, "error: $it")
                    })
        }

        findViewById<Button>(R.id.btn_de_authorize).setOnClickListener {
            Api.deauthorize()
                    .subscribe({
                        Log.i(TAG, "success: ${it.baseResponse.ret} ${it.baseResponse.errMsg}")
                    }, {
                        Log.e(TAG, "error: $it")
                    })
        }

        findViewById<Button>(R.id.btn_push_msg_quick_start).setOnClickListener { _ ->
            val intent = Intent(this, PushMsgQuickStartActivity::class.java)
            startActivity(intent)

            Api.listeningPushMsg().subscribe({
                Log.i(TAG, "success: ${it.baseResponse.ret} ${it.baseResponse.errMsg}")
                Log.d(TAG, "push msg body: ${it.msgBody}")
            }, {
                Log.e(TAG, "error: $it")
            })
        }

        findViewById<Button>(R.id.btn_authorize_status).setOnClickListener {
            Api.authorizeStatus()
                    .subscribe({
                        Log.i(TAG, "success: ${it.baseResponse.ret} ${it.baseResponse.errMsg} ")
                        Log.i(TAG, "success: ${it.isAuthorize} ${it.openId} ")
                    }, {
                        Log.e(TAG, "error: $it")
                    })
        }

        findViewById<Button>(R.id.btn_active_status).setOnClickListener { view ->
            Api.activeStatus()
                    .subscribe({
                        view.post {
                            Toast.makeText(this, "isActive = ${it.isActive}", Toast.LENGTH_SHORT).show()
                        }
                        Log.i(TAG, "success: ${it.baseResponse.ret} ${it.baseResponse.errMsg} ")
                        Log.i(TAG, "success: ${it.isActive} ")
                    }, {
                        view.post {
                            Toast.makeText(this, "error: $it", Toast.LENGTH_SHORT).show()
                        }
                        Log.e(TAG, "error: $it")
                    })
        }
    }

    companion object {
        const val TAG = "DocumentActivity"
    }

}