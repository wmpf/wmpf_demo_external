package com.tencent.wmpf.activate.util

expect object WmpfDeviceSignUpUtil {
    fun getPrivateKeyPublicKeyPair(): Pair<String, String>

    fun getSignature(productId: String, deviceId: String, privateKey: String): String

    fun verifySignature(productId: String, deviceId: String, publicKey: String, signature: String): Boolean

    fun addDevicesToWeChatServer(deviceInfo: DeviceInfo, accessToken: String): AddDeviceResp

    fun getAccessToken(appId: String, appSecret: String): AccessTokenResp
}

class AddDeviceResp {
    var errcode: Int = -1
    var errmsg: String = "not init"

    override fun toString(): String {
        return "AddDeviceResp(errcode=$errcode, errmsg='$errmsg')"
    }
}

class DeviceInfo {
    var product_id: String = ""
    var device_id_list: List<String> = ArrayList()
    var model_name: String = ""
}

class AccessTokenResp{
    var errcode = -1
    var errmsg = "not init"
    var access_token = ""
    var expires_in = ""
}

expect object Platform {
    val name: String
}

fun getPlatformName(): String = Platform.name