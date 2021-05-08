package com.tencent.wmpf.demo.wxfacepay

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.tencent.luggage.demo.wxapi.DeviceInfo
import com.tencent.wmpf.cli.task.IPCInvokerTask_InitGlobalConfig
import com.tencent.wmpf.demo.Api
import com.tencent.wmpf.demo.R
import com.tencent.wxpayface.IWxPayfaceCallback
import com.tencent.wxpayface.WxPayFace
import com.tencent.wxpayface.WxPayFace.RETURN_CODE
import com.tencent.wxpayface.WxPayFace.RETURN_MSG
import okhttp3.Response
import org.json.JSONObject

/**
 * 商户应用演示界面
 *
 * Created by javayhu on 2020/10/27.
 */
@SuppressLint("LongLogTag", "SetTextI18n", "CheckResult")
class IotActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "IotActivity"
    }

    private lateinit var debugTxt: TextView

    private fun getContext() = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iot)

        debugTxt = findViewById(R.id.iot_debug_tv)

        findViewById<Button>(R.id.btn_active_status).setOnClickListener { view ->
            doActivateStatus(view)
        }

        findViewById<Button>(R.id.btn_activate_device_by_iot).setOnClickListener { view ->
            doActivateDeviceByIoT(view)
        }

        findViewById<Button>(R.id.btn_warm_launch).setOnClickListener { view ->
            doWarmLaunch(view)
        }

        findViewById<Button>(R.id.btn_preload_time).setOnClickListener { view ->
            doPreloadRuntime(view)
        }

        findViewById<Button>(R.id.btn_init_wxfacepay).setOnClickListener { view ->
            doInitWxFacePay(view)
        }

        findViewById<Button>(R.id.btn_get_rawdata).setOnClickListener { view ->
            doGetRawdata(view)
        }

        findViewById<Button>(R.id.btn_get_facecode).setOnClickListener { view ->
            doFacePay(view)
        }

        findViewById<Button>(R.id.btn_authorize_face).setOnClickListener { view ->
            doAuthorizeByWxFacePay(view)
        }

        findViewById<Button>(R.id.btn_authorize_init_auth_info).setOnClickListener { view ->
            doInitWxPayInfo(view)
        }

        findViewById<Button>(R.id.btn_launch_wxa_app).setOnClickListener { view ->
            doLaunchWxaApp(view, 0)
        }

        findViewById<Button>(R.id.btn_launch_wxa_app_dev).setOnClickListener { view ->
            doLaunchWxaApp(view, 1)
        }

        findViewById<Button>(R.id.btn_launch_wxa_app_trail).setOnClickListener { view ->
            doLaunchWxaApp(view, 2)
        }

        findViewById<Button>(R.id.btn_close_wxa_app).setOnClickListener { view ->
            doCloseWxaApp(view)
        }

        findViewById<Button>(R.id.btn_ui_zoom).setOnClickListener { view ->
            doInitGlobalConfig(view)
        }

        findViewById<Button>(R.id.btn_de_authorize).setOnClickListener { view ->
            doDeauthorize(view)
        }

        findViewById<Button>(R.id.btn_launch_wxa_app_by_scan).setOnClickListener { view ->
            doLaunchWxaAppByScan(view)
        }

        findViewById<TextView>(R.id.iot_debug_tv)?.apply {
            movementMethod = ScrollingMovementMethod.getInstance()
            isScrollbarFadingEnabled = false
        }

    }
    
    private var TextView.safeText: CharSequence?
        get() = text
        set(value) {
            post {
                text = value
                scrollTo(0, 0)
            }
        }

    private fun doInitWxFacePay(view: View) {
        WxPayFace.getInstance().initWxpayface(this, object : IWxPayfaceCallback() {
            override fun response(info: MutableMap<Any?, Any?>?) {
                Log.i(TAG, "[doInitWxFacePay] success: ${info?.get(RETURN_CODE)} ${info?.get(RETURN_MSG)}")
                Toast.makeText(getContext(), "[doInitWxFacePay] success: ${info?.get(RETURN_CODE)} ${info?.get(RETURN_MSG)}", Toast.LENGTH_SHORT).show()
                debugTxt.safeText = info.toString()
            }
        })
    }

    private fun doGetRawdata(view: View) {
        WxPayFace.getInstance().getWxpayfaceRawdata(object : IWxPayfaceCallback() {
            override fun response(info: MutableMap<Any?, Any?>?) {
                Log.i(TAG, "[doGetRawdata] success: ${info?.get(RETURN_CODE)} ${info?.get(RETURN_MSG)}, rawdata: ${info?.get("rawdata")}")
                Toast.makeText(getContext(), "[doGetRawdata] success: ${info?.get(RETURN_CODE)} ${info?.get(RETURN_MSG)}", Toast.LENGTH_SHORT).show()

                (info?.get("rawdata") as? String).let {
                    if (!it.isNullOrEmpty()) {
                        debugTxt.safeText = it
                        getAuthInfo(it)
                    }
                }
            }
        })
    }

    private var authInfo: String? = null
    private val URL_AUTHINFO = "https://miniprog.pay.weixin.qq.com/xphp/cfacepaydemo/get_wxpayface_authinfo"

    private fun getAuthInfo(rawdata: String) {
        OkhttpUtils.getInstance().request(hashMapOf("rawdata" to rawdata), URL_AUTHINFO, object : OkhttpUtils.CallBack {
            override fun onSuccess(response: Response?) {
                response?.body()?.string().let {
                    Log.i(TAG, "[getAuthInfo] onSuccess, response body string: ${it}")
                    authInfo = JSONObject(it).optString("authinfo", null)
                }
            }

            override fun onFailed(e: Exception?) {
                Log.e(TAG, "[getAuthInfo] onFailed, error: $e")
            }
        })
    }

    private fun doFacePay(view: View) {
        WxPayFace.getInstance().getWxpayfaceCode(getFacePayInfo(), object : IWxPayfaceCallback() {
            override fun response(result: MutableMap<Any?, Any?>?) {
                if (null == result) {
                    Log.w(TAG, "[doFacePay] request fail, result is null")
                    return
                }
                debugTxt.safeText = result.toString()
                if ((result["return_code"] as? String).equals("SUCCESS")) {
                    val openid = result["openid"]
                    val faceCode = result["face_code"]

                    // javayhu 注意这里的mch_id一定要传，不能支付可能就失败了，虽然这个参数在前面的info中已经有了，但是pay的时候还是要传过去才行
                    val params = hashMapOf(
                            "mch_id" to "1900007081" as Any, //"商户号"  1900007081 / 1900008081
                            "total_fee" to "10" as Any, // "订单金额(数字)"，单位分. 该字段在在face_code_type为"1"时可不填，为"0"时必填
                            "out_trade_no" to out_trade_no as Any, //"商户订单号"，须与调用支付接口时字段一致，该字段在在face_code_type为"1"时可不填，为"0"时必填
                            "openid" to openid as Any,
                            "face_code" to faceCode as Any
                    )
                    Log.i(TAG, "[doFacePay] params: $params")

                    OkhttpUtils.getInstance().request(params, OkhttpUtils.URL_PAY, object : OkhttpUtils.CallBack{
                        override fun onSuccess(response: Response?) {
                            response?.body()?.string().let {
                                val jsonObject = JSONObject(it)
                                val returnCode = jsonObject.optString("return_code", "")
                                val errorCode = jsonObject.optString("err_code", "")
                                Log.i(TAG, "[doFacePay] success, response:${jsonObject}")
                                // javayhu 这里注意要判断errorCode，因为可能是请求SUCCESS，但是有其他的错误
                                if (returnCode == "SUCCESS" && errorCode.isNullOrEmpty()) {
                                    Log.i(TAG, "[doFacePay] success, face pay success")
                                } else {
                                    Log.i(TAG, "[doFacePay] fail, face pay failed")
                                }
                            }
                        }

                        override fun onFailed(e: Exception?) {
                            Log.e(TAG, "[doFacePay] fail", e)
                        }
                    })
                } else {
                    Log.w(TAG, "[doFacePay] request fail, result not success")
                }
            }
        })
    }

    private var out_trade_no: String? = null

    private fun getFacePayInfo(): Map<String, Object> {
        out_trade_no = System.currentTimeMillis().toString()
        return hashMapOf(
                "face_authtype" to "FACEPAY" as Object,
                "authinfo" to authInfo as Object,
                "appid" to "wx64b7714cf1f64585" as Object, //"商户号绑定的公众号/小程序"  wx2b029c08a6232582 / wx64b7714cf1f64585
                "mch_id" to "1900008081" as Object, //"商户号"  1900007081 / 1900008081
                "store_id" to "12345" as Object, //"门店编号"
                "out_trade_no" to out_trade_no as Object, //"商户订单号"，须与调用支付接口时字段一致，该字段在在face_code_type为"1"时可不填，为"0"时必填
                "total_fee" to "10" as Object, // "订单金额(数字)"，单位分. 该字段在在face_code_type为"1"时可不填，为"0"时必填
                "ignore_update_pay_result" to "1" as Object //不需要商户App更新支付结果
        )
    }

    private fun doActivateStatus(view: View) {
        Api.activeStatus()
                .subscribe({
                    val activateStatusResult = "[doActivateStatus] success: ${it.baseResponse.errCode} ${it.baseResponse.errMsg}"
                    view.post {
                        Toast.makeText(this, activateStatusResult, Toast.LENGTH_SHORT).show()
                    }
                    Log.i(TAG, "$activateStatusResult ${it.isActive}")
                    debugTxt.safeText = "$activateStatusResult ${it.isActive}"
                }, {
                    val activateStatusResult = "[doActivateStatus] error: ${Log.getStackTraceString(it)}"
                    view.post {
                        Toast.makeText(this, activateStatusResult, Toast.LENGTH_SHORT).show()
                    }
                    Log.e(TAG, activateStatusResult)
                    debugTxt.safeText = activateStatusResult
                })
    }

    private fun doActivateDeviceByIoT(view: View) {
        Api.activateDeviceByIoT(DeviceInfo.APP_ID)
                .subscribe({
                    val activateDeviceByIoTResult = "[doActivateDeviceByIoT] success: ${it.baseResponse.errCode} ${it.baseResponse.errMsg}"
                    view.post {
                        Toast.makeText(this, activateDeviceByIoTResult, Toast.LENGTH_SHORT).show()
                    }
                    Log.i(TAG, "$activateDeviceByIoTResult ${it.invokeToken}")
                    debugTxt.safeText = "$activateDeviceByIoTResult ${it.invokeToken}"
                }, {
                    val activateDeviceByIoTResult = "[doActivateDeviceByIoT] error: ${Log.getStackTraceString(it)}"
                    view.post {
                        Toast.makeText(this, activateDeviceByIoTResult, Toast.LENGTH_SHORT).show()
                    }
                    Log.e(TAG, activateDeviceByIoTResult)
                    debugTxt.safeText = activateDeviceByIoTResult
                })
    }

    // 调整小程序显示大小，zoom可以设置为任意值，这里给出一组建议值
    private val zoomArr = arrayOf(0.5, 0.75, 1.0, 1.25, 1.5, 1.75, 2.0)
    private var zoomIndex = 0

    private fun doInitGlobalConfig(view: View) {
        val jsonConfig = JSONObject()
        val size = zoomArr[zoomIndex % zoomArr.size]
        jsonConfig.put(IPCInvokerTask_InitGlobalConfig.UI_ZOOM, size)
        Api.initGlobalConfig(jsonConfig.toString()).subscribe({
            val initGlobalConfigResult = "[doInitGlobalConfig] success: ${it.baseResponse.errCode} ${it.baseResponse.errMsg}"
            view.post {
                Toast.makeText(this, "[doInitGlobalConfig] success: zoom set to ${size}, 下次启动小程序生效", Toast.LENGTH_SHORT).show()
                zoomIndex++
            }
            Log.i(TAG, initGlobalConfigResult)
            debugTxt.safeText = initGlobalConfigResult
        }, {
            val initGlobalConfigResult = "[doInitGlobalConfig] error: ${Log.getStackTraceString(it)}"
            view.post {
                Toast.makeText(this, initGlobalConfigResult, Toast.LENGTH_SHORT).show()
            }
            Log.e(TAG, initGlobalConfigResult)
            debugTxt.safeText = initGlobalConfigResult
        })
    }

    private fun doPreloadRuntime(view: View) {
        Api.preloadRuntime()
                .subscribe({
                    val preloadRuntimeResult = "[doPreloadRuntime] success: ${it.baseResponse.errCode} ${it.baseResponse.errMsg}"
                    view.post {
                        Toast.makeText(this, preloadRuntimeResult, Toast.LENGTH_SHORT).show()
                    }
                    Log.i(TAG, preloadRuntimeResult)
                    debugTxt.safeText = preloadRuntimeResult
                }, {
                    val preloadRuntimeResult = "[doPreloadRuntime] error: ${Log.getStackTraceString(it)}"
                    view.post {
                        Toast.makeText(this, preloadRuntimeResult, Toast.LENGTH_SHORT).show()
                    }
                    Log.e(TAG, preloadRuntimeResult)
                    debugTxt.safeText = preloadRuntimeResult
                })
    }

    /**
     * 小程序示例：wxe5f52902cf4de896
     * 腾讯视频：wxa75efa648b60994b
     * 儿童故事：wxdde587a4aebfe4f3
     * 青蛙示例：wxe48cfb127e3c4302
     * 诗词助手：wxf6a6a5d6654fd14c
     */
    private val APPID_MP_DEMO = "wxe5f52902cf4de896"
    private val PATH_MP_DEMO = ""
    private val APPID_LPOS_DEMO = "wxe48cfb127e3c4302"
    private val PATH_LPOS_DEMO = "pages/wmpf/wmpf"
    private val MP_APPID = APPID_MP_DEMO
    private val MP_PATH = PATH_MP_DEMO

    private fun doLaunchWxaApp(view: View, appType: Int = 0) {
        Api.launchWxaApp(MP_APPID, path = MP_PATH, appType = appType)
                .subscribe({
                    val launchWxaResult = "[doLaunchWxaApp] success: ${it.baseResponse.errCode} ${it.baseResponse.errMsg}"
                    view.post {
                        Toast.makeText(this, launchWxaResult, Toast.LENGTH_SHORT).show()
                    }
                    Log.i(TAG, launchWxaResult)
                    debugTxt.safeText = launchWxaResult
                }, {
                    val launchWxaResult = "[doLaunchWxaApp] error: ${Log.getStackTraceString(it)}"
                    view.post {
                        Toast.makeText(this, launchWxaResult, Toast.LENGTH_SHORT).show()
                    }
                    Log.e(TAG, launchWxaResult)
                    debugTxt.safeText = launchWxaResult
                })
    }

    private fun doCloseWxaApp(view: View) {
        Api.closeWxaApp(MP_APPID)
                .subscribe({
                    val closeWxaResult = "[doCloseWxaApp] success: ${it.baseResponse.errCode} ${it.baseResponse.errMsg}"
                    view.post {
                        Toast.makeText(this, closeWxaResult, Toast.LENGTH_SHORT).show()
                    }
                    Log.i(TAG, closeWxaResult)
                    debugTxt.safeText = closeWxaResult
                }, {
                    val closeWxaResult = "[doCloseWxaApp] error: ${Log.getStackTraceString(it)}"
                    view.post {
                        Toast.makeText(this, closeWxaResult, Toast.LENGTH_SHORT).show()
                    }
                    Log.e(TAG, closeWxaResult)
                    debugTxt.safeText = closeWxaResult
                })
    }

    //https://pay.weixin.qq.com/wiki/doc/wxfacepay/develop/sdk-android.html#%E4%BA%BA%E8%84%B8%E6%94%AF%E4%BB%98%E5%87%AD%E8%AF%81-getwxpayfacecode
    private fun doInitWxPayInfo(view: View) {
        if (authInfo.isNullOrEmpty()) {
            Toast.makeText(this, "[doInitWxPayInfo] error: no authinfo", Toast.LENGTH_SHORT).show()
            return
        }
        Api.initWxPayInfo(getFacePayInfo()).subscribe({
            val initWxPayInfoResult = "[doInitWxPayInfo] success: ${it.baseResponse.errCode} ${it.baseResponse.errMsg}"
            view.post {
                Toast.makeText(this, initWxPayInfoResult, Toast.LENGTH_SHORT).show()
            }
            Log.i(TAG, initWxPayInfoResult)
            debugTxt.safeText = initWxPayInfoResult
        }, {
            val initWxPayInfoResult = "[doInitWxPayInfo] error: ${Log.getStackTraceString(it)}"
            view.post {
                Toast.makeText(this, initWxPayInfoResult, Toast.LENGTH_SHORT).show()
            }
            Log.e(TAG, initWxPayInfoResult)
            debugTxt.safeText = initWxPayInfoResult
        })
    }

    private fun doAuthorizeByWxFacePay(view: View) {
        Api.authorizeByWxFacePay()
                .subscribe({
                    val authorizeResult = "[doAuthorizeByWxFacePay] success: ${it.baseResponse.errCode} ${it.baseResponse.errMsg}"
                    view.post {
                        Toast.makeText(this, authorizeResult, Toast.LENGTH_SHORT).show()
                    }
                    Log.i(TAG, "$authorizeResult ${it.resultJson}")
                    debugTxt.safeText = "$authorizeResult ${it.resultJson}"
                }, {
                    val authorizeResult = "[doAuthorizeByWxFacePay] error: ${Log.getStackTraceString(it)}"
                    view.post {
                        Toast.makeText(this, authorizeResult, Toast.LENGTH_SHORT).show()
                    }
                    Log.e(TAG, authorizeResult)
                    debugTxt.safeText = authorizeResult
                })
    }

    private fun doDeauthorize(view: View) {
        Api.deauthorize()
                .subscribe({
                    val deauthorizeResult = "[doDeauthorize] success: ${it.baseResponse.errCode} ${it.baseResponse.errMsg}"
                    view.post {
                        Toast.makeText(this, deauthorizeResult, Toast.LENGTH_SHORT).show()
                    }
                    Log.i(TAG, deauthorizeResult)
                    debugTxt.safeText = deauthorizeResult
                }, {
                    val deauthorizeResult = "[doDeauthorize] error: ${Log.getStackTraceString(it)}"
                    view.post {
                        Toast.makeText(this, deauthorizeResult, Toast.LENGTH_SHORT).show()
                    }
                    Log.e(TAG, deauthorizeResult)
                    debugTxt.safeText = deauthorizeResult
                })
    }

    //可以将开发版小程序的二维码解码出来输入到这里，就可以打开开发版小程序
    private val URL_SCAN = "https://mp.weixin.qq.com/a/~~K7E0CacQQCY~iiUSi0qsucahcKeD1qeCrQ~~"

    private fun doLaunchWxaAppByScan(view: View) {
        Api.launchWxaAppByScan(URL_SCAN)
                .subscribe({
                    val launchResult = "[doLaunchWxaAppByScan] success: ${it.baseResponse.errCode} ${it.baseResponse.errMsg}"
                    view.post {
                        Toast.makeText(this, launchResult, Toast.LENGTH_SHORT).show()
                    }
                    Log.i(TAG, launchResult)
                    debugTxt.safeText = launchResult
                }, {
                    val launchResult = "[doLaunchWxaAppByScan] error: ${Log.getStackTraceString(it)}"
                    view.post {
                        Toast.makeText(this, launchResult, Toast.LENGTH_SHORT).show()
                    }
                    Log.e(TAG, launchResult)
                    debugTxt.safeText = launchResult
                })
    }

    //预热启动小程序
    private fun doWarmLaunch(view: View) {
        Log.i(TAG, "[doWarmLaunch] appId: $MP_APPID")
        Api.warmLaunch(MP_APPID)
                .subscribe({ it ->
                    val warmLaunchResult = "[doWarmLaunch] success: 预热小程序 ${it.baseResponse.errCode} ${it.baseResponse.errMsg}"
                    debugTxt.safeText = warmLaunchResult
                    view.post {
                        Toast.makeText(this, warmLaunchResult, Toast.LENGTH_SHORT).show()
                        AlertDialog.Builder(this).setTitle("预热完成")
                                .setPositiveButton("启动小程序") { _, _ ->
                                    Api.launchWxaApp(MP_APPID, MP_PATH, appType = 0).subscribe({
                                        val launchResult = "[doLaunch] success: 启动小程序 ${it.baseResponse.errCode} ${it.baseResponse.errMsg}"
                                        Log.i(TAG, launchResult)
                                        debugTxt.safeText = launchResult
                                    }, {
                                        val launchResult = "[doLaunch] error: 启动小程序 ${Log.getStackTraceString(it)}"
                                        Log.e(TAG, launchResult)
                                        debugTxt.safeText = launchResult
                                    })
                                }
                            .setNegativeButton("取消") { _, _ ->
                                Log.e(TAG, "[doLaunch] cancel")
                                debugTxt.safeText = "[doLaunch] cancel"
                            }.show()
                    }
                    Log.i(TAG, warmLaunchResult)
                }, {
                    val warmLaunchResult = "[doWarmLaunch] error: 预热小程序 ${Log.getStackTraceString(it)}"
                    view.post {
                        Toast.makeText(this, warmLaunchResult, Toast.LENGTH_SHORT).show()
                    }
                    Log.e(TAG, warmLaunchResult)
                    debugTxt.safeText = warmLaunchResult
                })
    }

}
