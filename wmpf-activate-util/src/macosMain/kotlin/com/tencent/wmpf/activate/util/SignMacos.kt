package com.tencent.wmpf.activate.util

actual object WmpfDeviceSignUpUtil {
    actual fun getPrivateKeyPublicKeyPair(keysStorageDir: String): Pair<String, String> {
        TODO("Not yet implemented")
    }

    actual fun getSignature(
        productId: String,
        deviceId: String,
        privateKey: String
    ): String {
        TODO("Not yet implemented")
    }

    actual fun verifySignature(
        productId: String,
        deviceId: String,
        privateKey: String,
        signature: String
    ): Boolean {
        TODO("Not yet implemented")
    }

    actual fun addDevicesToWeChatServer(deviceInfo: DeviceInfo): AddDeviceResp {
        TODO("Not yet implemented")
    }

    actual fun getAccessToken(
        appId: String,
        appSecret: String
    ): AccessTokenResp {
        TODO("Not yet implemented")
    }

}

actual object Platform {
    actual val name: String = "Native"
}