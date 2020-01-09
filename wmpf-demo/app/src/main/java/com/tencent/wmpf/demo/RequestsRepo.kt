package com.tencent.wmpf.demo

import android.util.Log
import com.tencent.wmpf.cli.task.IPCInovkerTask_SetPushMsgCallback
import com.tencent.wmpf.cli.task.IPCInvokerTask_getPushToken
import com.tencent.wmpf.cli.task.pb.WMPFBaseRequestHelper
import com.tencent.wmpf.cli.task.pb.WMPFIPCInvoker
import com.tencent.wmpf.demo.ui.PushMsgQuickStartActivity
import com.tencent.wmpf.proto.WMPFPushMsgRequest
import com.tencent.wmpf.proto.WMPFPushMsgResponse
import com.tencent.wmpf.proto.WMPFPushTokenRequest
import com.tencent.wmpf.proto.WMPFPushTokenResponse
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.FormBody
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import java.nio.charset.Charset


/**
 * Created by complexzeng on 2019-10-17 16:25.
 */
class RequestsRepo {
    companion object {
        const val TAG = "RequestsRepo"
    }

    private val client = OkHttpClient()

    fun getAccessToken(callback: (Boolean, String) -> Unit) {
        Thread {
            val request = Request.Builder().url("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=wxee3e328d0211d2e8&secret=73b6e14dba526ad994e6e491c64b992f").build()
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    try {
                        val resp = response.body()!!.source().readString(Charset.defaultCharset())
                        val jsonObject= JSONObject(resp)
                        val accessToken = jsonObject.optString("access_token")
                        callback(true, accessToken)
                    } catch (e: Exception) {
                        callback(false, "获取失败...")
                    }

                } else {
                    callback(false, "获取失败...")
                }
            } catch (e: Exception) {
                Log.e(TAG, "getAccessToken: ", e)
            }
        }.start()
    }

    fun getPushToken(appId: String, callback: (Boolean, String) -> Unit) {
        val request = WMPFPushTokenRequest()
        request.baseRequest = WMPFBaseRequestHelper.checked()
        request.appId = appId
        val result = WMPFIPCInvoker.invokeAsync<
                IPCInvokerTask_getPushToken,
                WMPFPushTokenRequest,
                WMPFPushTokenResponse
                >(request, IPCInvokerTask_getPushToken::class.java) { response ->
            callback(true, response.pushToken)
        }
        if (!result) {
            callback(false, "invoke getPushToken fail")
        }
    }

    fun postMsg(accessToken: String, token: String, msg: String, delay: Int, callback: (Boolean, String) -> Unit) {
        Thread {
            val params = FormBody.Builder()
            params.add("msg", msg)
            params.add("push_token", token)
            val req = Request.Builder()
                    .url("https://api.weixin.qq.com/wxa/business/runtime/appmsg/push?access_token=$accessToken")
                    .post(params.build())
                    .build()
            try {
                val response = client.newCall(req).execute()
                callback(response.isSuccessful, response.body().toString())
            } catch (e: IOException) {

            }
        }.start()
    }

    fun setMsgCallback(ui: PushMsgQuickStartActivity) {
        Thread {
            val request = WMPFPushMsgRequest()
            request.baseRequest = WMPFBaseRequestHelper.checked()
            WMPFIPCInvoker.invokeAsync<IPCInovkerTask_SetPushMsgCallback,
                    WMPFPushMsgRequest, WMPFPushMsgResponse>(request, IPCInovkerTask_SetPushMsgCallback::class.java) { resp ->
                ui.printlnToView("receive: " + resp.msgBody)
            }
            Response(true, "")
        }.start()
    }
}

data class Response<out A, B>(
        val isSuccess: A,
        var body: B
) {
    override fun toString(): String = "($isSuccess, $body)"
}
