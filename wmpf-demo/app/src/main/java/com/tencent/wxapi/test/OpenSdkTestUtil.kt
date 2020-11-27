package com.tencent.wxapi.test

import android.util.Log
import io.reactivex.Single
import okhttp3.*
import org.json.JSONObject

/**
 * WARNING: This class can be used only in TESTS or DEMOS
 *
 *
 * NEVER EVER USE THE FOLLOWING DANGEROUS CODE IN YOUR CLIENT
 */
object OpenSdkTestUtil {

    private const val TAG = "TEST.OpenSdkTestUtil"

    private const val CODE_TO_SESSION_API = "https://api.weixin.qq.com/sns/jscode2session"
    private const val OAUTH_TOKEN_API = "https://api.weixin.qq.com/sns/oauth2/access_token"
    private const val RAW_TOKEN_API = "https://api.weixin.qq.com/cgi-bin/token"
    private const val SDK_TICKET_API = "https://api.weixin.qq.com/cgi-bin/ticket/getticket"
    private const val RUNTIME_CODE_API = "https://api.weixin.qq.com/sns/runtime/code/get"
    private const val USER_INFO_API = "https://api.weixin.qq.com/sns/userinfo"
    private const val REGISTER = "https://api.weixin.qq.com/wxaruntime/register"
    private var oauthToken: String? = null
    var openId: String? = null
    private val client = OkHttpClient()

    /**
     * Get SDK ticket from appId && appSecret
     *
     * @param appId
     * @param appSecret
     * @return
     */
    @JvmStatic
    fun getSDKTicket(appId: String, appSecret: String): Single<String> {
        return OpenSdkTestUtil.getRawToken(appId, appSecret)
                .flatMap { rawToken -> OpenSdkTestUtil.getSDKTicket(rawToken) }
    }

    /**
     * Get SDK ticket from existed access_token
     *
     * @param token
     * @return
     */
    private fun getSDKTicket(token: String): Single<String> {
        val builder = HttpUrl.parse(SDK_TICKET_API)!!.newBuilder()
        builder.addQueryParameter("access_token", token)
        builder.addQueryParameter("type", "2")
        val url = builder.build().toString()

        val request = Request.Builder().url(url).build()
        return Single.create { emitter ->
            val response = client.newCall(request).execute()
            val obj = JSONObject(response.body()!!.string())
            emitter.onSuccess(obj.getString("ticket"))
        }
    }

    fun getOAuthInfo(appId: String, secret: String, code: String): Single<JSONObject> {
        Log.d(TAG, String.format("get oauth info, code=%s", code))

        val urlBuilder = HttpUrl.parse(OAUTH_TOKEN_API)!!.newBuilder()
        urlBuilder.addQueryParameter("appid", appId)
        urlBuilder.addQueryParameter("secret", secret)
        urlBuilder.addQueryParameter("code", code)
        urlBuilder.addQueryParameter("grant_type", "authorization_code")

        val url = urlBuilder.build().toString()
        val request = Request.Builder().url(url).build()

        return Single.create { emitter ->
            val response = client.newCall(request).execute()
            val obj = JSONObject(response.body()!!.string())
            openId = obj.optString("openid")
            oauthToken = obj.optString("access_token")
            emitter.onSuccess(obj)
        }
    }

    fun getOAuthToken(appId: String, secret: String, code: String): Single<String> {
        val urlBuilder = HttpUrl.parse(OAUTH_TOKEN_API)!!.newBuilder()
        urlBuilder.addQueryParameter("appid", appId)
        urlBuilder.addQueryParameter("secret", secret)
        urlBuilder.addQueryParameter("code", code)
        urlBuilder.addQueryParameter("grant_type", "authorization_code")

        val url = urlBuilder.build().toString()
        val request = Request.Builder().url(url).build()

        return Single.create { emitter ->
            val response = client.newCall(request).execute()
            val obj = JSONObject(response.body()!!.string())
            openId = obj.optString("openid")
            oauthToken = obj.optString("access_token")
            emitter.onSuccess(obj.getString("access_token"))
        }
    }

    private fun getRawToken(appId: String, secret: String): Single<String> {
        val urlBuilder = HttpUrl.parse(RAW_TOKEN_API)!!.newBuilder()
        urlBuilder.addQueryParameter("appid", appId)
        urlBuilder.addQueryParameter("secret", secret)
        urlBuilder.addQueryParameter("grant_type", "client_credential")

        val url = urlBuilder.build().toString()
        val request = Request.Builder().url(url).build()

        return Single.create { emitter ->
            val response = client.newCall(request).execute()
            val obj = JSONObject(response.body()!!.string())
            emitter.onSuccess(obj.getString("access_token"))
        }
    }

    fun getRuntimeCode(token: String): Single<String> {
        val builder = HttpUrl.parse(RUNTIME_CODE_API)!!.newBuilder()
        builder.addQueryParameter("access_token", token)
        val url = builder.build().toString()

        val request = Request.Builder().url(url).build()
        return Single.create { emitter ->
            val response = client.newCall(request).execute()
            val obj = JSONObject(response.body()!!.string())
            emitter.onSuccess(obj.getString("code"))
        }
    }

    /**
     * https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID
     *
     * @param openId
     * @return
     */
    fun getUserInfo(openId: String?, accessToken: String?): Single<JSONObject> {
        Log.d(TAG, String.format("get user info, openId=%s, accessToken=%s", openId, accessToken))

        val builder = HttpUrl.parse(USER_INFO_API)!!.newBuilder()
        builder.addQueryParameter("openid", openId)
        builder.addQueryParameter("access_token", accessToken)
        val url = builder.build().toString()

        val request = Request.Builder().url(url).build()
        return Single.create { emitter ->
            val response = client.newCall(request).execute()
            val obj = JSONObject(response.body()!!.string())
            emitter.onSuccess(obj)
        }
    }

    /**
     * https://api.weixin.qq.com/wxa/runtime/register?access_token=ACCESS_TOKEN
     */
    @JvmStatic
    @Throws(Exception::class)
    fun getDeviceTicket(appId: String, appSecret: String, deviceInfo: String?): Single<String> {
        return OpenSdkTestUtil.getRawToken(appId, appSecret)
                .flatMap { rawToken -> OpenSdkTestUtil.getDeviceTicket(rawToken, deviceInfo) }
    }

    @JvmStatic
    @Throws(Exception::class)
    fun getDeviceTicket(accessToken: String?, deviceInfo: String?): Single<String> {
        val builder = HttpUrl.parse(REGISTER)!!.newBuilder()
//        builder.addQueryParameter("access_token", accessToken)
        val url = builder.build().toString()

        val reqObj = JSONObject()
        try {
            reqObj.put("accesstoken", accessToken)
            reqObj.put("device_info", deviceInfo)
        } catch (e: Exception) {
            Log.e(TAG, "parse error")
        }

        val request = Request.Builder().url(url)
                .post(RequestBody.create(MediaType.parse("application/json;"), reqObj.toString()))
                .build()

        return Single.create { emitter ->
            val response = client.newCall(request).execute()
            val obj = JSONObject(response.body()!!.string())
            emitter.onSuccess(obj.optString("device_ticket", ""))
        }
    }

    fun jscode2session(appId: String, secret: String, code: String): Single<JSONObject> {
        Log.d(TAG, String.format("jscode 2 session, code=%s", code))

        val urlBuilder = HttpUrl.parse(CODE_TO_SESSION_API)!!.newBuilder()
        urlBuilder.addQueryParameter("appid", appId)
        urlBuilder.addQueryParameter("secret", secret)
        urlBuilder.addQueryParameter("js_code", code)
        urlBuilder.addQueryParameter("grant_type", "authorization_code")

        val url = urlBuilder.build().toString()
        val request = Request.Builder().url(url).build()

        return Single.create { emitter ->
            val response = client.newCall(request).execute()
            val obj = JSONObject(response.body()!!.string())
            //obj.optString("openid")
            //obj.optString("session_key")
            emitter.onSuccess(obj)
        }
    }
}
