package com.tencent.wmpf.demo.wxfacepay

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import com.tencent.mm.opensdk.utils.Log
import com.tencent.wmpf.demo.contentprovider.ContentProvideConstants.InvokeChannelConstants
import okhttp3.Response
import org.json.JSONObject

/**
 * 商户移动应用端跟商户小程序通过InvokeChannel方式通信类
 *
 * created by javayhu on 2020/11/26.
 */
class WxFacePayContentProvider : ContentProvider() {

    companion object {
        private const val TAG = "WxFacePayInvokeChannelContentProvider"

        private val sURIMatcher = UriMatcher(UriMatcher.NO_MATCH)

        private const val CODE_CALLBACK_INVOKE_CHANNEL = 1
        private const val CODE_NOTIFY_INVOKE_CHANNEL_EVENT = 2

        init {
            sURIMatcher.apply {
                addURI(InvokeChannelConstants.ContentProvider.WMPF2Cli.AUTHORITY, InvokeChannelConstants.ContentProvider.WMPF2Cli.PATH_INVOKE_CHANNEL, CODE_CALLBACK_INVOKE_CHANNEL)
                addURI(InvokeChannelConstants.ContentProvider.WMPF2Cli.AUTHORITY, InvokeChannelConstants.ContentProvider.WMPF2Cli.PATH_INVOKE_CHANNEL_EVENT, CODE_NOTIFY_INVOKE_CHANNEL_EVENT)
            }
        }
    }

    private val mHandler by lazy {
        Handler(context?.mainLooper)
    }

    private val mEventIdList = hashSetOf<String?>()

    override fun insert(p0: Uri, p1: ContentValues?): Uri? {
        when (sURIMatcher.match(p0)) {
            // 异步方法调用
            CODE_CALLBACK_INVOKE_CHANNEL -> {
                val invokeId = p1?.getAsString(InvokeChannelConstants.Key.INVOKE_ID)
                val command = p1?.getAsString(InvokeChannelConstants.Key.COMMAND)
                val sourceData = p1?.getAsString(InvokeChannelConstants.Key.DATA)
                Log.i(TAG, "invokeId: $invokeId, command: $command, sourceData: $sourceData")

                handleInvokeFromWmpf(invokeId, command, sourceData)
            }
            // 回调事件注册
            CODE_NOTIFY_INVOKE_CHANNEL_EVENT -> {
                val eventId = p1?.getAsString(InvokeChannelConstants.Key.EVENT_ID)
                val event = p1?.getAsString(InvokeChannelConstants.Key.EVENT)
                Log.i(TAG, "register, eventId: $eventId, event: $event")

                mEventIdList.add(eventId)
                notifyEvent(0, eventId, event)
            }
        }
        return null
    }

    private fun handleInvokeFromWmpf(invokeId: String?, command: String?, sourceData: String?) {
        if (command.equals("passPhoneNumber")) {//小程序侧传来用户手机号
            val dataObject = JSONObject(sourceData)
            val countryCode = dataObject.optString("countryCode", "") //用于相对于小程序appId的openid
            val phoneNumber = dataObject.optString("phoneNumber", "")
            val purePhoneNumber = dataObject.optString("purePhoneNumber", "")
            Log.i(TAG, "[handleInvokeFromWmpf] passPhoneNumber, countryCode:${countryCode}, phoneNumber:${phoneNumber}, purePhoneNumber:$purePhoneNumber")

            if (!phoneNumber.isNullOrEmpty()) {
                Log.i(TAG, "[handleInvokeFromWmpf] get phone number:$phoneNumber")
                invokeChannelCallback(invokeId, command, "invoke success")
            } else {
                invokeChannelCallback(invokeId, command, "invalid data")
            }
        } else if (command.equals("passFaceCode")) {//小程序侧传来用户付款码
            val dataObject = JSONObject(sourceData)
            val openid = dataObject.optString("openid", "")
            val faceCode = dataObject.optString("faceCode", "")
            Log.i(TAG, "[handleInvokeFromWmpf] passFaceCode, openid:${openid}, faceCode:${faceCode}")

            if (!openid.isNullOrEmpty() && !faceCode.isNullOrEmpty()) {
                val outTradeNo = System.currentTimeMillis().toString()
                val params = hashMapOf(
                        "mch_id" to "1900007081" as Any, //"商户号"  1900007081 / 1900008001
                        "total_fee" to "1" as Any, // "订单金额(数字)"，单位分. 该字段在在face_code_type为"1"时可不填，为"0"时必填
                        "out_trade_no" to outTradeNo as Any, //"商户订单号"，须与调用支付接口时字段一致，该字段在在face_code_type为"1"时可不填，为"0"时必填
                        "openid" to openid as Any,
                        "face_code" to faceCode as Any
                )
                Log.i(TAG, "[handleInvokeFromWmpf] params: $params")

                OkhttpUtils.getInstance().request(params, OkhttpUtils.URL_PAY, object : OkhttpUtils.CallBack{
                    override fun onSuccess(response: Response?) {
                        response?.body()?.string().let {
                            val jsonObject = JSONObject(it)
                            val returnCode = jsonObject.optString("return_code", "")
                            val errorCode = jsonObject.optString("err_code", "")
                            Log.i(TAG, "[handleInvokeFromWmpf] success, returnCode:$returnCode, errorCode:$errorCode")
                            if (returnCode == "SUCCESS" && errorCode.isNullOrEmpty()) {
                                invokeChannelCallback(invokeId, command, "支付成功")
                            } else {
                                invokeChannelCallback(invokeId, command, "支付失败")
                            }
                        }
                    }

                    override fun onFailed(e: java.lang.Exception?) {
                        Log.i(TAG, "[handleInvokeFromWmpf] fail")
                        invokeChannelCallback(invokeId, command, "支付失败")
                    }

                })
            } else {
                invokeChannelCallback(invokeId, command, "invalid data")
            }
        } else if (command.equals("invokeTest")) {
            val dataObject = JSONObject(sourceData)
            val value = dataObject.optString("value", "")
            Log.i(TAG, "[handleInvokeFromWmpf] invokeTest, value:${value}")

            if (!value.isNullOrEmpty()) {
                invokeChannelCallback(invokeId, command, "hello $value from invoke")
            } else {
                invokeChannelCallback(invokeId, command, "invalid data")
            }
        }
    }

    private fun invokeChannelCallback(invokeId: String?, command: String?, message: String?) {
        mHandler.post({
            val cv = ContentValues()
            cv.apply {
                put(InvokeChannelConstants.Key.INVOKE_ID, invokeId)
                put(InvokeChannelConstants.Key.COMMAND, command)
                put(InvokeChannelConstants.Key.DATA, message)
            }
            try {
                context?.contentResolver?.insert(InvokeChannelConstants.ContentProvider.Cli2WMPF.URI_CALLBACK_INVOKE_CHANNEL, cv)
            } catch (e: Exception) {
                Log.e(TAG, "[handleInvokeFromWmpf] callback invoke channel error")
            }
        })
    }

    // 同步方法调用
    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        Log.i(TAG, "method: $method, arg: $arg, extras: $extras")
        val bundle = Bundle()
        if (!method.isNullOrEmpty() && method == "invokeTest") {
            val dataObject = JSONObject(arg)
            val value = dataObject.optString("value", "")
            bundle.putString("data", "hello $value from invokeSync")
        }
        return bundle
    }

    private fun notifyEvent(counter: Int, eventId: String?, event: String?) {
        val cv = ContentValues()
        mHandler.postDelayed({
            if (!mEventIdList.contains(eventId)) {
                return@postDelayed
            }
            cv.apply {
                put(InvokeChannelConstants.Key.EVENT_ID, eventId)
                put(InvokeChannelConstants.Key.EVENT, event)
                put(InvokeChannelConstants.Key.DATA, "event count $counter")
            }
            try {
                context.contentResolver.insert(InvokeChannelConstants.ContentProvider.Cli2WMPF.URI_NOTIFY_INVOKE_CHANNEL_EVENT, cv)
                Log.i(TAG, "[notifyEvent] success, content: event count $counter")
            } catch (e: Exception) {
                Log.e(TAG, "[notifyEvent] callback invoke channel error")
            }
            if (counter < 99) {
                val i = counter + 1
                notifyEvent(i, eventId, event)
            }
        }, 2000)
    }

    override fun query(p0: Uri, p1: Array<String>?, p2: String?, p3: Array<String>?, p4: String?): Cursor? {
        return null
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<String>?): Int {
        return 0
    }

    // 回调事件反注册
    override fun delete(p0: Uri, p1: String?, p2: Array<String>?): Int {
        if (p2?.size == 2) {
            val eventId = p2[0]
            val event = p2[1]
            mEventIdList.remove(eventId)
            Log.i(TAG, "unregister success, eventId: $eventId, event: $event")
        }
        return 0
    }

    override fun getType(p0: Uri): String? {
        return null
    }
}

