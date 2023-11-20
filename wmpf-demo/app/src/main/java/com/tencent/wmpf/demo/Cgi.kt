package com.tencent.wmpf.demo

import android.util.Log
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * WARNING: appSecret、access_token 需要严格保密，不应流出服务端后台环境。
 * 除测试和 DEMO 演示外，强烈不建议在客户端代码中使用。
 *
 * 以下代码存在安全风险，仅限测试或演示使用，请务必不要用于生产环境。
 */
object Cgi {
    private const val TAG = "WMPF.CGI"
    private val client = OkHttpClient()

    private const val OAUTH_TOKEN_API = "https://api.weixin.qq.com/sns/oauth2/access_token"
    private const val USER_INFO_API = "https://api.weixin.qq.com/sns/userinfo"
    private const val DEMO_DEVICE_INFO = "https://open.weixin.qq.com/wxaruntime/getdemodeviceinfo"


    private fun getJSON(url: HttpUrl): JSONObject {
        Log.d(TAG, "send request: $url")

        val req = Request.Builder().url(url).build()
        val res = client.newCall(req).execute()
        if (res.isSuccessful) {
            val body = res.body!!.string()
            val json = JSONObject(body)
            val errCode = json.optInt("ErrCode", 0)
            val errMsg = json.optString("ErrMsg", "")
            if (errCode != 0) {
                Log.e(TAG, "request failed: $body")
                throw RuntimeException("request failed: $errMsg (errCode=$errCode)")
            } else {
                Log.d(TAG, "request success: $body")
            }
            return json
        } else {
            throw RuntimeException("request failed: " + res.code)
        }
    }

    data class AuthInfo(val openId: String?, val accessToken: String?)

    fun getOAuthInfo(appId: String, secret: String, code: String): AuthInfo {
        Log.d(TAG, "get oauth info, appId=$appId, code=$code")

        val urlBuilder = OAUTH_TOKEN_API.toHttpUrl().newBuilder()
            .addQueryParameter("appid", appId)
            .addQueryParameter("secret", secret)
            .addQueryParameter("code", code)
            .addQueryParameter("grant_type", "authorization_code")

        val res = getJSON(urlBuilder.build())

        return AuthInfo(res.optString("openid"), res.optString("access_token"))
    }

    data class UserInfo(
        val openId: String,
        val nickname: String,
        val sex: Int,
        val province: String,
        val city: String,
        val country: String,
        val unionId: String,
        val avatarUrl: String,
    )

    fun getUserInfo(openId: String?, accessToken: String?): UserInfo {
        Log.d(TAG, "getUserInfo, openId=$openId, accessToken=$accessToken")

        val builder = USER_INFO_API.toHttpUrl().newBuilder()
            .addQueryParameter("openid", openId)
            .addQueryParameter("access_token", accessToken)

        val result = getJSON(builder.build())

        return UserInfo(
            result.getString("openid"),
            result.getString("nickname"),
            result.getInt("sex"),
            result.getString("province"),
            result.getString("city"),
            result.getString("country"),
            result.getString("unionid"),
            result.getString("headimgurl"),
        )
    }

    data class DeviceInfo(
        val productId: Int,
        val deviceId: String,
        val signature: String,
        val keyVersion: Int,
        val expiredTimeMs: Long,
    )

    /**
     * 仅限快速体验使用
     */
    fun getTestDeviceInfo(ticket: String, wxaAppId: String, hostAppId: String): DeviceInfo {
        Log.d(TAG, "getTestDeviceInfo, ticket=$ticket, wxaAppId=$wxaAppId, hostAppId=$hostAppId")

        val builder = DEMO_DEVICE_INFO.toHttpUrl().newBuilder()
            .addQueryParameter("ticket", ticket)
            .addQueryParameter("wxaappid", wxaAppId)
            .addQueryParameter("hostappid", hostAppId)

        val result = getJSON(builder.build())
        val appIdList = result.optJSONArray("appid_list")
        val deviceInfo = DeviceInfo(
            result.optInt("product_id", 0),
            result.optString("device_id", ""),
            result.optString("signature", ""),
            result.optInt("key_version", 0),
            result.optLong("expiredTimeMs") * 1000L + System.currentTimeMillis()
        )
        Log.d(RequestsRepo.TAG, "getDeviceInfo: $deviceInfo, appIdList = $appIdList")

        return deviceInfo
    }
}